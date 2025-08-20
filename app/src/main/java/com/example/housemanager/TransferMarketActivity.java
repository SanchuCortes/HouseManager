package com.example.housemanager;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.example.housemanager.market.Team;
import com.example.housemanager.repository.FootballRepository;
import com.example.housemanager.ui.adapters.TransferMarketAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransferMarketActivity extends AppCompatActivity implements TransferMarketAdapter.OnPlayerClickListener {

    private static final String TAG = "TransferMarketActivity";

    private FootballRepository repository;
    private TransferMarketAdapter adapter;

    private RecyclerView recyclerView;
    private EditText etSearch;
    private Spinner spinnerTeam;
    private Spinner spinnerPosition;

    private final List<Player> allPlayers = new ArrayList<>();
    private final List<Team> allTeams = new ArrayList<>();
    private final List<Player> filteredPlayers = new ArrayList<>();

    private final String[] positions = {"Todas", "Portero", "Defensa", "Medio", "Delantero"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_market);

        repository = FootballRepository.getInstance(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();
        setupObservers();

        // Iniciamos la sincronización si no hay datos
        repository.loadTeams();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewPlayers);
        etSearch = findViewById(R.id.searchEditText);
        spinnerTeam = findViewById(R.id.spinnerTeams);
        spinnerPosition = findViewById(R.id.spinnerSort); // Reutilizamos este spinner para posiciones
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
        // Spinner de posiciones
        ArrayAdapter<String> positionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, positions);
        spinnerPosition.setAdapter(positionAdapter);

        // Spinner de equipos (se llenará cuando se carguen los datos)
        ArrayAdapter<String> teamAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spinnerTeam.setAdapter(teamAdapter);

        // Listener para búsqueda
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

        // Listeners para spinners
        spinnerTeam.setOnItemSelectedListener(new SimpleItemSelectedListener(() -> applyFilters()));
        spinnerPosition.setOnItemSelectedListener(new SimpleItemSelectedListener(() -> applyFilters()));
    }

    private void setupObservers() {
        // Observer para jugadores
        repository.getAllPlayers().observe(this, players -> {
            allPlayers.clear();
            if (players != null) {
                allPlayers.addAll(players);
                applyFilters();
            }
        });

        // Observer para equipos
        repository.getAllTeams().observe(this, teams -> {
            allTeams.clear();
            if (teams != null) {
                allTeams.addAll(teams);
                updateTeamSpinner();
            }
        });

        // Observer para estado de sincronización
        repository.getIsSyncing().observe(this, isSyncing -> {
            // Aquí podrías mostrar/ocultar un ProgressBar
            if (isSyncing != null && isSyncing) {
                // Mostrar loading
            } else {
                // Ocultar loading
            }
        });
    }

    private void updateTeamSpinner() {
        List<String> teamNames = new ArrayList<>();
        teamNames.add("Todos los equipos");

        for (Team team : allTeams) {
            teamNames.add(team.getName());
        }

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerTeam.getAdapter();
        adapter.clear();
        adapter.addAll(teamNames);
        adapter.notifyDataSetChanged();
    }

    private void applyFilters() {
        String searchQuery = etSearch.getText() != null ? etSearch.getText().toString().trim().toLowerCase(Locale.ROOT) : "";
        String selectedPosition = (String) spinnerPosition.getSelectedItem();
        String selectedTeam = (String) spinnerTeam.getSelectedItem();

        filteredPlayers.clear();

        for (Player player : allPlayers) {
            boolean matches = true;

            // Filtro por búsqueda (nombre o equipo)
            if (!searchQuery.isEmpty()) {
                String playerData = (player.getName() + " " + player.getTeamName()).toLowerCase(Locale.ROOT);
                matches = playerData.contains(searchQuery);
            }

            // Filtro por posición
            if (matches && selectedPosition != null && !"Todas".equals(selectedPosition)) {
                matches = selectedPosition.equals(player.getPosition());
            }

            // Filtro por equipo
            if (matches && selectedTeam != null && !"Todos los equipos".equals(selectedTeam)) {
                matches = selectedTeam.equals(player.getTeamName());
            }

            // Solo jugadores disponibles
            if (matches && player.isAvailable()) {
                filteredPlayers.add(player);
            }
        }

        adapter.updatePlayers(filteredPlayers);
        updateResultsCount();
    }

    private void updateResultsCount() {
        // Aquí puedes actualizar contadores en la UI
        // Por ejemplo: textTotalPlayers.setText("Mostrando: " + filteredPlayers.size() + " jugadores");
    }

    @Override
    public void onPlayerClick(Player player) {
        // Mostrar detalles del jugador
        Toast.makeText(this, "Jugador: " + player.getName() +
                "\nEquipo: " + player.getTeamName() +
                "\nPrecio: " + player.getFormattedPrice() +
                "\nPuntos: " + player.getTotalPoints(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBuyPlayerClick(Player player) {
        showBuyConfirmationDialog(player);
    }

    private void showBuyConfirmationDialog(Player player) {
        new AlertDialog.Builder(this)
                .setTitle("Fichar Jugador")
                .setMessage("¿Quieres fichar a " + player.getName() + " por " + player.getFormattedPrice() + "?")
                .setPositiveButton("Fichar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        buyPlayer(player);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void buyPlayer(Player player) {
        // Aquí implementarías la lógica de compra real
        // Por ahora solo simulamos la compra
        player.setAvailable(false);

        Toast.makeText(this, "¡Has fichado a " + player.getName() + "! 🎉", Toast.LENGTH_SHORT).show();

        // Actualizamos la lista
        applyFilters();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Clase auxiliar para los listeners de los spinners
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