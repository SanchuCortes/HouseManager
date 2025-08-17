package com.example.housemanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.housemanager.market.Player;
import com.example.housemanager.market.Team;
import com.example.housemanager.repository.FootballRepository; // <- puente
import com.example.housemanager.ui.PlayerDetailActivity;
import com.example.housemanager.ui.adapters.TransferMarketAdapter;
import com.example.housemanager.databinding.ActivityTransferMarketBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class TransferMarketActivity extends AppCompatActivity implements TransferMarketAdapter.OnPlayerClickListener {
    private static final String TAG = "TransferMarketActivity";

    private ActivityTransferMarketBinding binding;
    private FootballRepository repository;
    private TransferMarketAdapter adapter;
    private List<Player> allPlayers = new ArrayList<>();
    private List<Player> filteredPlayers = new ArrayList<>();
    private List<Team> allTeams = new ArrayList<>();

    // Filtros actuales
    private String currentPositionFilter = "ALL";
    private int currentTeamFilter = -1; // -1 = Todos los equipos
    private String currentSortOrder = "NAME"; // NAME, PRICE_ASC, PRICE_DESC, POINTS
    private double maxBudget = 100.0; // Presupuesto máximo por defecto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransferMarketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();
        setupUI();
        loadData();
    }

    private void initializeComponents() {
        repository = FootballRepository.getInstance(this);

        // Configurar RecyclerView
        adapter = new TransferMarketAdapter(this);
        binding.recyclerViewPlayers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewPlayers.setAdapter(adapter);
    }

    private void setupUI() {
        // Configurar Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mercado de Fichajes");
        }

        // Configurar búsqueda
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPlayers();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Configurar chips de posición
        setupPositionChips();

        // Configurar spinner de equipos
        setupTeamSpinner();

        // Configurar spinner de ordenación
        setupSortSpinner();

        // Configurar botones de acción
        binding.btnClearFilters.setOnClickListener(v -> clearAllFilters());
        binding.btnRefreshData.setOnClickListener(v -> refreshData());

        // Mostrar/ocultar filtros avanzados
        binding.btnToggleFilters.setOnClickListener(v -> toggleAdvancedFilters());
    }

    private void setupPositionChips() {
        // Configurar listeners para chips de posición
        binding.chipAll.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                currentPositionFilter = "ALL";
                uncheckOtherPositionChips("ALL");
                filterPlayers();
            }
        });

        binding.chipGoalkeepers.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                currentPositionFilter = "GK";
                uncheckOtherPositionChips("GK");
                filterPlayers();
            }
        });

        binding.chipDefenders.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                currentPositionFilter = "DEF";
                uncheckOtherPositionChips("DEF");
                filterPlayers();
            }
        });

        binding.chipMidfielders.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                currentPositionFilter = "MID";
                uncheckOtherPositionChips("MID");
                filterPlayers();
            }
        });

        binding.chipForwards.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                currentPositionFilter = "FWD";
                uncheckOtherPositionChips("FWD");
                filterPlayers();
            }
        });

        // Seleccionar "Todos" por defecto
        binding.chipAll.setChecked(true);
    }

    private void uncheckOtherPositionChips(String selectedPosition) {
        if (!selectedPosition.equals("ALL")) binding.chipAll.setChecked(false);
        if (!selectedPosition.equals("GK")) binding.chipGoalkeepers.setChecked(false);
        if (!selectedPosition.equals("DEF")) binding.chipDefenders.setChecked(false);
        if (!selectedPosition.equals("MID")) binding.chipMidfielders.setChecked(false);
        if (!selectedPosition.equals("FWD")) binding.chipForwards.setChecked(false);
    }

    private void setupTeamSpinner() {
        // Se configurará cuando se carguen los equipos
        binding.spinnerTeams.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentTeamFilter = -1; // Todos los equipos
                } else {
                    Team selectedTeam = allTeams.get(position - 1);
                    currentTeamFilter = selectedTeam.getTeamId();
                }
                filterPlayers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSortSpinner() {
        String[] sortOptions = {"Nombre A-Z", "Precio (menor)", "Precio (mayor)", "Puntos (mayor)"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSort.setAdapter(sortAdapter);

        binding.spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: currentSortOrder = "NAME"; break;
                    case 1: currentSortOrder = "PRICE_ASC"; break;
                    case 2: currentSortOrder = "PRICE_DESC"; break;
                    case 3: currentSortOrder = "POINTS"; break;
                }
                filterPlayers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void toggleAdvancedFilters() {
        boolean isVisible = binding.advancedFiltersLayout.getVisibility() == View.VISIBLE;
        binding.advancedFiltersLayout.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        binding.btnToggleFilters.setText(isVisible ? "Mostrar Filtros" : "Ocultar Filtros");
    }

    private void loadData() {
        showLoading(true);

        // Cargar jugadores
        repository.getAllPlayers().observe(this, new Observer<List<Player>>() {
            @Override
            public void onChanged(List<Player> players) {
                if (players != null && !players.isEmpty()) {
                    allPlayers.clear();
                    allPlayers.addAll(players);
                    filterPlayers();

                    binding.textTotalPlayers.setText("Total: " + players.size() + " jugadores");
                    Log.d(TAG, "✅ Cargados " + players.size() + " jugadores");
                } else {
                    Log.w(TAG, "⚠️ No hay jugadores en la base de datos");
                    Toast.makeText(TransferMarketActivity.this,
                            "No hay jugadores disponibles. Sincroniza los datos primero.",
                            Toast.LENGTH_LONG).show();
                }
                showLoading(false);
            }
        });

        // Cargar equipos para el spinner
        repository.getAllTeams().observe(this, new Observer<List<Team>>() {
            @Override
            public void onChanged(List<Team> teams) {
                if (teams != null) {
                    allTeams.clear();
                    allTeams.addAll(teams);
                    setupTeamSpinnerData();
                    Log.d(TAG, "✅ Cargados " + teams.size() + " equipos");
                }
            }
        });
    }

    private void setupTeamSpinnerData() {
        List<String> teamNames = new ArrayList<>();
        teamNames.add("Todos los equipos");

        for (Team team : allTeams) {
            teamNames.add(team.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, teamNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTeams.setAdapter(adapter);
    }

    private void filterPlayers() {
        filteredPlayers.clear();
        String searchQuery = binding.searchEditText.getText().toString().toLowerCase().trim();

        for (Player player : allPlayers) {
            // Filtro por búsqueda
            if (!searchQuery.isEmpty() &&
                    !player.getName().toLowerCase().contains(searchQuery) &&
                    !player.getTeamName().toLowerCase().contains(searchQuery) &&
                    !player.getNationality().toLowerCase().contains(searchQuery)) {
                continue;
            }

            // Filtro por posición
            if (!currentPositionFilter.equals("ALL") &&
                    !player.getPosition().equals(currentPositionFilter)) {
                continue;
            }

            // Filtro por equipo
            if (currentTeamFilter != -1 && player.getTeamId() != currentTeamFilter) {
                continue;
            }

            // Filtro por presupuesto
            if (player.getCurrentPrice() > maxBudget) {
                continue;
            }

            // Filtro por disponibilidad
            if (!player.isAvailable()) {
                continue;
            }

            filteredPlayers.add(player);
        }

        // Ordenar resultados
        sortPlayers();

        // Actualizar UI
        adapter.updatePlayers(filteredPlayers);
        binding.textFilteredCount.setText("Mostrando: " + filteredPlayers.size() + " jugadores");

        // Mostrar mensaje si no hay resultados
        if (filteredPlayers.isEmpty() && !allPlayers.isEmpty()) {
            binding.textNoResults.setVisibility(View.VISIBLE);
            binding.recyclerViewPlayers.setVisibility(View.GONE);
        } else {
            binding.textNoResults.setVisibility(View.GONE);
            binding.recyclerViewPlayers.setVisibility(View.VISIBLE);
        }
    }

    private void sortPlayers() {
        switch (currentSortOrder) {
            case "NAME":
                Collections.sort(filteredPlayers, new Comparator<Player>() {
                    @Override
                    public int compare(Player p1, Player p2) {
                        return p1.getName().compareToIgnoreCase(p2.getName());
                    }
                });
                break;

            case "PRICE_ASC":
                Collections.sort(filteredPlayers, new Comparator<Player>() {
                    @Override
                    public int compare(Player p1, Player p2) {
                        return Double.compare(p1.getCurrentPrice(), p2.getCurrentPrice());
                    }
                });
                break;

            case "PRICE_DESC":
                Collections.sort(filteredPlayers, new Comparator<Player>() {
                    @Override
                    public int compare(Player p1, Player p2) {
                        return Double.compare(p2.getCurrentPrice(), p1.getCurrentPrice());
                    }
                });
                break;

            case "POINTS":
                Collections.sort(filteredPlayers, new Comparator<Player>() {
                    @Override
                    public int compare(Player p1, Player p2) {
                        return Integer.compare(p2.getTotalPoints(), p1.getTotalPoints());
                    }
                });
                break;
        }
    }

    private void clearAllFilters() {
        // Limpiar búsqueda
        binding.searchEditText.setText("");

        // Resetear posición a "Todos"
        binding.chipAll.setChecked(true);
        currentPositionFilter = "ALL";

        // Resetear equipo a "Todos"
        binding.spinnerTeams.setSelection(0);
        currentTeamFilter = -1;

        // Resetear ordenación a "Nombre"
        binding.spinnerSort.setSelection(0);
        currentSortOrder = "NAME";

        // Aplicar filtros
        filterPlayers();

        Toast.makeText(this, "Filtros limpiados", Toast.LENGTH_SHORT).show();
    }

    private void refreshData() {
        showLoading(true);

        repository.syncLaLigaTeams(new FootballRepository.SyncCallback() {
            @Override
            public void onProgress(String message) {
                runOnUiThread(() -> {
                    binding.textLoadingMessage.setText(message);
                });
            }

            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(TransferMarketActivity.this,
                            "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                    // Los datos se recargarán automáticamente por LiveData
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(TransferMarketActivity.this,
                            "Error actualizando datos: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.textLoadingMessage.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.recyclerViewPlayers.setVisibility(show ? View.GONE : View.VISIBLE);

        if (show) {
            binding.textLoadingMessage.setText("Cargando jugadores...");
        }
    }

    // Implementación de OnPlayerClickListener
    @Override
    public void onPlayerClick(Player player) {
        // Abrir detalles del jugador
        Intent intent = new Intent(this, PlayerDetailActivity.class);
        intent.putExtra("player_id", player.getPlayerId());
        intent.putExtra("player_name", player.getName());
        startActivity(intent);
    }

    @Override
    public void onBuyPlayerClick(Player player) {
        // Lógica de compra (a implementar según tu sistema de equipos)
        showBuyPlayerDialog(player);
    }

    private void showBuyPlayerDialog(Player player) {
        // Crear diálogo de confirmación de compra
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Fichar Jugador")
                .setMessage(String.format("¿Quieres fichar a %s por %.1f M€?",
                        player.getName(), player.getCurrentPrice()))
                .setPositiveButton("Fichar", (dialog, which) -> {
                    // TODO: Implementar lógica de compra real
                    // - Verificar presupuesto disponible
                    // - Añadir al equipo del usuario
                    // - Actualizar base de datos
                    // - Mostrar confirmación

                    Toast.makeText(this,
                            "¡" + player.getName() + " fichado correctamente!",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}