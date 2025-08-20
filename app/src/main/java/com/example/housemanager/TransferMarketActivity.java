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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_market);

        repository = FootballRepository.getInstance(this);
        viewModel = new ViewModelProvider(this).get(FootballViewModel.class);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();
        setupObservers();

        // Configurar el timer para el countdown
        updateHandler = new Handler();
        updateRunnable = this::updateCountdown;

        // Programar próxima actualización (24 horas)
        scheduleNextUpdate();
        startCountdownUpdates();

        // Generar mercado inicial
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
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mercado de Fichajes");
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

        // Botón para generar nuevo mercado manualmente
        btnRefresh.setOnClickListener(v -> {
            generateNewMarket();
            Toast.makeText(this, "¡Nuevo mercado generado!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupObservers() {
        // Escuchar cuando lleguen jugadores del repositorio
        repository.getAllPlayers().observe(this, players -> {
            if (players != null && !players.isEmpty() && allMarketPlayers.isEmpty()) {
                // Solo generar si no tenemos mercado aún
                generateMarketFromPlayers(players);
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
    }

    private void generateMarketFromPlayers(List<Player> allPlayers) {
        List<Player> availablePlayers = new ArrayList<>();

        // Solo jugadores disponibles
        for (Player player : allPlayers) {
            if (player.isAvailable()) {
                availablePlayers.add(player);
            }
        }

        if (availablePlayers.size() < MARKET_PLAYERS_COUNT) {
            Toast.makeText(this, "No hay suficientes jugadores", Toast.LENGTH_SHORT).show();
            return;
        }

        // Elegir 10 al azar
        Collections.shuffle(availablePlayers);
        allMarketPlayers.clear();
        allMarketPlayers.addAll(availablePlayers.subList(0, MARKET_PLAYERS_COUNT));

        applyFilters();
        updateMarketStatus();

        android.util.Log.d(TAG, "Mercado generado con " + allMarketPlayers.size() + " jugadores");
    }

    private void generateNewMarket() {
        // Obtener nuevos jugadores
        repository.getAllPlayers().observe(this, new androidx.lifecycle.Observer<List<Player>>() {
            @Override
            public void onChanged(List<Player> players) {
                if (players != null && !players.isEmpty()) {
                    generateMarketFromPlayers(players);
                    // Quitar observer para no duplicar
                    repository.getAllPlayers().removeObserver(this);
                }
            }
        });

        scheduleNextUpdate(); // Reprogramar siguiente
    }

    private void scheduleNextUpdate() {
        // Actualización cada 24 horas
        nextUpdateTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);

        // Programar actualización automática
        updateHandler.postDelayed(() -> {
            generateNewMarket();
            Toast.makeText(this, "¡Mercado actualizado automáticamente!", Toast.LENGTH_LONG).show();
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

        adapter.updatePlayers(filteredPlayers);
        updatePlayersCount();
    }

    private void updatePlayersCount() {
        if (tvPlayersCount != null) {
            tvPlayersCount.setText("Mostrando: " + filteredPlayers.size() + " de " + allMarketPlayers.size() + " jugadores");
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
            Toast.makeText(this, "El mercado está cerrado", Toast.LENGTH_SHORT).show();
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
        // Marcar como no disponible en la BD
        repository.buyPlayer(player.getPlayerId(), new FootballRepository.SyncCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(TransferMarketActivity.this,
                            "¡Has fichado a " + player.getName() + "! 🎉",
                            Toast.LENGTH_SHORT).show();

                    // Quitar de la lista actual
                    allMarketPlayers.remove(player);
                    applyFilters();
                    updateMarketStatus();
                });
            }

            @Override
            public void onError(Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(TransferMarketActivity.this,
                            "Error al fichar: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
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