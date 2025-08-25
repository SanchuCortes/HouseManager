package com.example.housemanager;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.housemanager.market.Player;
import com.example.housemanager.repository.FootballRepository;
import com.example.housemanager.ui.adapters.TransferMarketAdapter;
import com.example.housemanager.viewmodel.FootballViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TransferMarketActivity extends AppCompatActivity implements TransferMarketAdapter.OnPlayerClickListener {

    private static final String TAG = "TransferMarketActivity";
    private static final int MARKET_PLAYERS_COUNT = 10;

    private FootballRepository repository;
    private FootballViewModel viewModel;
    private TransferMarketAdapter adapter;
    private Handler updateHandler;
    private Runnable updateRunnable;

    // Liga actual (por defecto 1 hasta tener selección real de liga)
    private long leagueId = 1L;

    // Elementos de la pantalla
    private RecyclerView recyclerView;
    private EditText etSearch;
    private Spinner spinnerPosition;
    private TextView tvMarketStatus;
    private TextView tvPlayersCount;
    private TextView tvNextUpdate;
    private ProgressBar progressBar;
    private MaterialButton btnRefresh;

    // Datos del mercado
    private final List<Player> allMarketPlayers = new ArrayList<>();
    private final List<Player> filteredPlayers = new ArrayList<>();
    private final String[] positions = {"Todas", "Portero", "Defensa", "Medio", "Delantero"};

    // Estado del mercado
    private boolean isMarketOpen = true;
    private long nextUpdateTime = 0;
    private int currentPlayersCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_market);

        repository = FootballRepository.getInstance(this);
        viewModel = new ViewModelProvider(this).get(FootballViewModel.class);

        // Leer leagueId del intent (por ahora default=1 si no se envía)
        leagueId = getIntent().getLongExtra("EXTRA_LEAGUE_ID", 1L);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();
        setupObservers();

        // Configurar el timer para el countdown (solo UI)
        updateHandler = new Handler();
        updateRunnable = this::updateCountdown;
        startCountdownUpdates();

        // Lanzar sincronización de datos base si es necesario (equipos/jugadores)
        generateInitialMarket();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewPlayers);
        etSearch = findViewById(R.id.searchEditText);
        spinnerPosition = findViewById(R.id.spinnerSort);
        tvMarketStatus = findViewById(R.id.textTotalPlayers);
        tvPlayersCount = findViewById(R.id.textFilteredCount);
        tvNextUpdate = findViewById(R.id.textLoadingMessage);
        progressBar = findViewById(R.id.progressBar);
        btnRefresh = findViewById(R.id.btnRefreshData);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            try {
                setSupportActionBar(toolbar);
            } catch (Exception e) {
                android.util.Log.w("TransferMarketActivity", "Error al configurar toolbar", e);
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Mercado de Fichajes");
            }
        } else {
            android.util.Log.w("TransferMarketActivity", "Toolbar no encontrada en el layout");
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new TransferMarketAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupFilters() {
        // Dropdown de posiciones
        ArrayAdapter<String> positionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, positions);
        spinnerPosition.setAdapter(positionAdapter);

        // Buscador en tiempo real
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filtro por posición
        spinnerPosition.setOnItemSelectedListener(new SimpleItemSelectedListener(() -> applyFilters()));

        // Botón "Actualizar": si no hay jugadores -> FULL_SYNC; si hay -> solo reconsultar Room (LiveData ya observa)
        btnRefresh.setOnClickListener(v -> {
            if (currentPlayersCount <= 0) {
                repository.forceSyncFromAPI(new FootballRepository.SyncCallback() {
                    @Override public void onSuccess() {
                        com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Datos sincronizados desde la API", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                    }
                    @Override public void onError(Throwable t) {
                        com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Error al sincronizar: " + t.getMessage(), com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                    }
                    @Override public void onProgress(String message, int current, int total) { }
                });
            } else {
                com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Actualizado desde Room", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                // No hacemos nada más: LiveData ya reconsulta automáticamente la DB
            }
        });
    }

    private void setupObservers() {
        // Listado del mercado de la liga actual (no repone al comprar; máximo 10 iniciales)
        repository.getLeagueMarketPlayers(leagueId).observe(this, players -> {
            allMarketPlayers.clear();
            if (players != null) {
                allMarketPlayers.addAll(players);
            }
            applyFilters();
            updateMarketStatus();
        });

        // Contador de jugadores disponibles para decidir si refrescar hace FULL_SYNC o no
        repository.getAvailablePlayersCount().observe(this, count -> {
            currentPlayersCount = count != null ? count : 0;
        });

        // Countdown: observar estado del mercado (expiración)
        repository.getMarketStateLive(leagueId).observe(this, state -> {
            if (state != null) {
                nextUpdateTime = state.getMarketExpiresAtMillis();
                updateCountdown();
            }
        });

        // Mostrar/ocultar loading
        repository.getIsSyncing().observe(this, isSyncing -> {
            if (isSyncing != null && isSyncing) {
                showLoading("Cargando jugadores...");
            } else {
                hideLoading();
            }
        });
    }

    private void generateInitialMarket() {
        // Cargar los datos si no están
        viewModel.loadTeams();
        // Asegurar que el mercado de la liga esté generado según la hora configurada
        repository.ensureLeagueMarketGenerated(leagueId, null);
        // Idempotente: recalcular puntos acumulados de todas las jornadas FINISHED en background,
        // de forma que los totales aparezcan correctos en el mercado aunque el usuario entre pronto.
        repository.recalcAllFinishedPoints(new FootballRepository.SyncCallback() {
            @Override public void onSuccess() {
                android.util.Log.d(TAG, "Recalc puntos OK (mercado)");
            }
            @Override public void onError(Throwable t) {
                android.util.Log.w(TAG, "Recalc puntos fallo (mercado): " + t.getMessage());
            }
            @Override public void onProgress(String message, int current, int total) { }
        });
    }

    private void generateMarketFromPlayers(List<Player> players) {
        // Ya vienen aleatorios y disponibles desde Room
        allMarketPlayers.clear();
        if (players != null) {
            allMarketPlayers.addAll(players);
        }
        applyFilters();
        updateMarketStatus();
        android.util.Log.d(TAG, "Mercado generado con " + allMarketPlayers.size() + " jugadores (random desde DB)");
    }

    private void generateNewMarket() {
        // Ya no se genera un mercado nuevo manualmente; se verifica estado por ensureLeagueMarketGenerated
        repository.ensureLeagueMarketGenerated(leagueId, null);
    }

    private void scheduleNextUpdate() {
        // Actualización cada 24 horas
        nextUpdateTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);

        // Programar actualización automática
        updateHandler.postDelayed(() -> {
            generateNewMarket();
            android.util.Log.d(TAG, "¡Mercado actualizado automáticamente!");
        }, 24 * 60 * 60 * 1000); // 24 horas
    }

    private void applyFilters() {
        String searchQuery = etSearch.getText() != null ? etSearch.getText().toString().trim().toLowerCase(Locale.ROOT) : "";
        String selectedPosition = (String) spinnerPosition.getSelectedItem();

        filteredPlayers.clear();

        for (Player player : allMarketPlayers) {
            boolean matches = true;

            // Filtro por nombre o equipo
            if (!searchQuery.isEmpty()) {
                String playerData = (player.getName() + " " + player.getTeamName()).toLowerCase(Locale.ROOT);
                matches = playerData.contains(searchQuery);
            }

            // Filtro por posición
            if (matches && selectedPosition != null && !"Todas".equals(selectedPosition)) {
                matches = selectedPosition.equals(player.getPosition());
            }

            if (matches) {
                filteredPlayers.add(player);
            }
        }

        // Ordenar por puntos descendente para dar visibilidad a quienes ya han puntuado
        Collections.sort(filteredPlayers, (a, b) -> Integer.compare(b.getTotalPoints(), a.getTotalPoints()));

        // Limitar a 10 jugadores visibles como máximo
        if (filteredPlayers.size() > MARKET_PLAYERS_COUNT) {
            java.util.List<Player> firstTen = new java.util.ArrayList<>(filteredPlayers.subList(0, MARKET_PLAYERS_COUNT));
            adapter.updatePlayers(firstTen);
        } else {
            adapter.updatePlayers(filteredPlayers);
        }
        updatePlayersCount();
    }

    private void updatePlayersCount() {
        if (tvPlayersCount != null) {
            int shown = Math.min(filteredPlayers.size(), MARKET_PLAYERS_COUNT);
            tvPlayersCount.setText("Mostrando: " + shown + " jugadores");
        }
    }

    private void updateMarketStatus() {
        if (tvMarketStatus != null) {
            if (isMarketOpen) {
                tvMarketStatus.setText("🟢 Mercado ABIERTO - Puedes fichar jugadores");
                tvMarketStatus.setTextColor(getResources().getColor(R.color.secondary_green));
            } else {
                tvMarketStatus.setText("🔴 Mercado CERRADO - Próximamente nuevos jugadores");
                tvMarketStatus.setTextColor(getResources().getColor(R.color.error_red));
            }
        }
    }

    private void startCountdownUpdates() {
        updateCountdown();
        updateHandler.postDelayed(updateRunnable, 1000);
    }

    private void updateCountdown() {
        if (nextUpdateTime > 0) {
            long timeUntilUpdate = nextUpdateTime - System.currentTimeMillis();

            if (timeUntilUpdate > 0) {
                long hours = timeUntilUpdate / (1000 * 60 * 60);
                long minutes = (timeUntilUpdate % (1000 * 60 * 60)) / (1000 * 60);
                long seconds = (timeUntilUpdate % (1000 * 60)) / 1000;

                String countdown = String.format(Locale.getDefault(),
                        "⏰ Próxima actualización: %02d:%02d:%02d", hours, minutes, seconds);

                if (tvNextUpdate != null) {
                    tvNextUpdate.setText(countdown);
                    tvNextUpdate.setVisibility(View.VISIBLE);
                }
            } else {
                if (tvNextUpdate != null) {
                    tvNextUpdate.setText("⏰ Generando nuevo mercado...");
                }
            }
        }

        // Siguiente update del timer
        updateHandler.postDelayed(updateRunnable, 1000);
    }

    private void showLoading(String message) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPlayerClick(Player player) {
        // Mostrar detalles completos
        String details = String.format("Jugador: %s\n" +
                        "Equipo: %s\n" +
                        "Posición: %s\n" +
                        "Precio: %.1fM €\n" +
                        "Puntos totales: %d\n" +
                        "Nacionalidad: %s",
                player.getName(),
                player.getTeamName(),
                player.getPosition(),
                player.getCurrentPrice(),
                player.getTotalPoints(),
                player.getNationality());

        new AlertDialog.Builder(this)
                .setTitle("Detalles del Jugador")
                .setMessage(details)
                .setPositiveButton("Fichar", (dialog, which) -> showBuyDialog(player))
                .setNegativeButton("Cerrar", null)
                .show();
    }

    @Override
    public void onBuyPlayerClick(Player player) {
        showBuyDialog(player);
    }

    private void showBuyDialog(Player player) {
        if (!isMarketOpen) {
            com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "El mercado está cerrado", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
            return;
        }

        String message = String.format("¿Quieres fichar a %s por %.1fM €?\n\n" +
                        "Equipo: %s\n" +
                        "Posición: %s\n" +
                        "Puntos: %d",
                player.getName(),
                player.getCurrentPrice(),
                player.getTeamName(),
                player.getPosition(),
                player.getTotalPoints());

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Fichaje")
                .setMessage(message)
                .setPositiveButton("Fichar", (dialog, which) -> buyPlayer(player))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void buyPlayer(Player player) {
        // Marcar como no disponible en la BD (sin reponer automáticamente)
        repository.buyPlayer(leagueId, player.getPlayerId(), 1L, new FootballRepository.SyncCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content),
                            "¡Has fichado a " + player.getName() + "! 🎉",
                            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                    // No manipulamos la lista manualmente; el observer de Room actualizará el RecyclerView
                });
            }

            @Override
            public void onError(Throwable t) {
                runOnUiThread(() -> {
                    android.util.Log.e(TAG, "Error al fichar: " + t.getMessage(), t);
                    com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content),
                            "Error al fichar: " + t.getMessage(),
                            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onProgress(String message, int current, int total) {
                // No necesario aquí
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateHandler != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Helper para el spinner
    private static class SimpleItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {
        private final Runnable onSelected;

        SimpleItemSelectedListener(Runnable onSelected) {
            this.onSelected = onSelected;
        }

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            onSelected.run();
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {}
    }
}