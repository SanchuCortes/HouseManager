package com.example.housemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.housemanager.api.models.PlayerAPI;
import com.example.housemanager.viewmodel.FootballViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class MyTeamActivity extends AppCompatActivity {

    public static final String EXTRA_TEAM_ID = "team_id";
    public static final String EXTRA_LEAGUE_NAME = "EXTRA_LEAGUE_NAME";

    private FootballViewModel vm;
    private PlayersSimpleAdapter adapter;

    // Referencias a las vistas
    private TextView tvHeader;
    private TextView tvPoints;
    private TextView tvTeamValue;
    private TextView tvRemainingBudget;
    private MaterialButton btnTransfers;
    private MaterialButton btnSetCaptain;

    // Datos del equipo
    private int teamId = -1;
    private String leagueName = "Mi Liga Fantasy";
    private int currentCaptainId = -1;
    private final List<PlayerAPI> currentSquad = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_team);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupButtons();
        setupViewModel();

        getIntentData();
        loadTeamData();
    }

    private void initViews() {
        tvHeader = findViewById(R.id.tv_league_header);
        tvPoints = findViewById(R.id.tv_points);
        tvTeamValue = findViewById(R.id.tv_team_value);
        tvRemainingBudget = findViewById(R.id.tv_remaining_budget);
        btnTransfers = findViewById(R.id.btn_transfers);
        btnSetCaptain = findViewById(R.id.btn_set_captain);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Equipo");
        }
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_players);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // El adapter maneja el click para asignar capitán
        adapter = new PlayersSimpleAdapter(player -> {
            currentCaptainId = player.getId();
            CaptainManager.setCaptain(this, teamId, currentCaptainId);
            adapter.setCaptainId(currentCaptainId);
            recalculatePoints();

            android.widget.Toast.makeText(this,
                    "Capitán asignado: " + player.getName(),
                    android.widget.Toast.LENGTH_SHORT).show();
        });

        recyclerView.setAdapter(adapter);
    }

    private void setupButtons() {
        // Botón para ir al mercado de fichajes
        btnTransfers.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransferMarketActivity.class);
            startActivity(intent);
        });

        // Botón para mostrar info del capitán actual
        if (btnSetCaptain != null) {
            btnSetCaptain.setOnClickListener(v -> showCaptainInfo());
        }
    }

    private void setupViewModel() {
        vm = new ViewModelProvider(this).get(FootballViewModel.class);

        // Observo cambios en la plantilla
        vm.getSquad().observe(this, players -> {
            currentSquad.clear();
            if (players != null && !players.isEmpty()) {
                currentSquad.addAll(players);
                adapter.submit(players);

                // Cargo el capitán que tenía guardado
                currentCaptainId = CaptainManager.getCaptain(this, teamId);
                adapter.setCaptainId(currentCaptainId);

                recalculatePoints();
                updateTeamStats();
            } else {
                android.widget.Toast.makeText(this,
                        "Aún no tienes jugadores. Ve al mercado de fichajes.",
                        android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getIntentData() {
        teamId = getIntent().getIntExtra(EXTRA_TEAM_ID, 1);
        leagueName = getIntent().getStringExtra(EXTRA_LEAGUE_NAME);

        if (leagueName != null) {
            tvHeader.setText(leagueName);
        }
    }

    private void loadTeamData() {
        if (teamId != -1) {
            vm.loadSquad(teamId);
        } else {
            // Si no hay team ID, muestro datos de ejemplo
            loadMockTeamData();
        }
    }

    private void loadMockTeamData() {
        // Datos de prueba mientras no tengamos datos reales
        List<PlayerAPI> mockPlayers = createMockPlayers();
        currentSquad.clear();
        currentSquad.addAll(mockPlayers);
        adapter.submit(mockPlayers);

        currentCaptainId = CaptainManager.getCaptain(this, 1);
        adapter.setCaptainId(currentCaptainId);

        recalculatePoints();
        updateTeamStats();
    }

    private List<PlayerAPI> createMockPlayers() {
        List<PlayerAPI> mockPlayers = new ArrayList<>();

        // Algunos jugadores de ejemplo
        mockPlayers.add(new PlayerAPI(1, "Ter Stegen", "Portero", "Alemania", 75));
        mockPlayers.add(new PlayerAPI(2, "Piqué", "Defensa", "España", 65));
        mockPlayers.add(new PlayerAPI(3, "Busquets", "Medio", "España", 80));
        mockPlayers.add(new PlayerAPI(4, "Lewandowski", "Delantero", "Polonia", 95));
        mockPlayers.add(new PlayerAPI(5, "Pedri", "Medio", "España", 70));

        return mockPlayers;
    }

    private void recalculatePoints() {
        int totalPoints = 0;

        for (PlayerAPI player : currentSquad) {
            int playerPoints = player.getPoints();

            // El capitán puntúa doble
            if (player.getId() == currentCaptainId) {
                playerPoints *= 2;
            }

            totalPoints += playerPoints;
        }

        if (tvPoints != null) {
            tvPoints.setText(String.valueOf(totalPoints));
        }
    }

    private void updateTeamStats() {
        // Calculo aproximado del valor del equipo
        double teamValue = currentSquad.size() * 12.5; // 12.5M por jugador de media
        if (tvTeamValue != null) {
            tvTeamValue.setText(String.format("%.1fM €", teamValue));
        }

        // Presupuesto restante (150M inicial menos valor actual)
        double remainingBudget = 150.0 - teamValue;
        if (tvRemainingBudget != null) {
            tvRemainingBudget.setText(String.format("%.1fM €", Math.max(0, remainingBudget)));
        }
    }

    private void showCaptainInfo() {
        String captainName = "Ninguno";

        // Busco el nombre del capitán actual
        for (PlayerAPI player : currentSquad) {
            if (player.getId() == currentCaptainId) {
                captainName = player.getName();
                break;
            }
        }

        android.widget.Toast.makeText(this,
                "Capitán actual: " + captainName +
                        "\n\nToca un jugador para asignarlo como capitán",
                android.widget.Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}