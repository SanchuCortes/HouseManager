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
    public static final String EXTRA_LEAGUE_ID = "EXTRA_LEAGUE_ID";
    public static final String EXTRA_LEAGUE_NAME = "EXTRA_LEAGUE_NAME";

    private FootballViewModel vm;
    private PlayersSimpleAdapter adapter;

    private TextView tvHeader;
    private TextView tvPoints;
    private TextView tvTeamValue;
    private TextView tvRemainingBudget;
    private MaterialButton btnTransfers;
    private MaterialButton btnSetCaptain;

    private int teamId = -1;
    private long leagueId = 1L;
    private String leagueName = "Mi Liga Fantasy";
    private int currentCaptainId = -1;
    private final List<PlayerAPI> currentSquad = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_team);

        // Inicializar vistas
        initViews();
        setupToolbar();
        setupRecyclerView();

        // Obtener datos del intent (antes de configurar observers)
        getIntentData();

        setupButtons();
        setupViewModel();

        // Cargar datos (observados desde ViewModel)
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

        // Adapter con callback para asignar capitán
        adapter = new PlayersSimpleAdapter(player -> {
            // CLICK EN JUGADOR → asignar capitán
            currentCaptainId = player.getId();
            CaptainManager.setCaptain(this, teamId, currentCaptainId);
            adapter.setCaptainId(currentCaptainId);
            recalculatePoints();

            // Mostrar feedback al usuario
            com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content),
                    "Capitán asignado: " + player.getName(),
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
        });

        recyclerView.setAdapter(adapter);
    }

    private void setupButtons() {
        // Botón Transferencias
        btnTransfers.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransferMarketActivity.class);
            // TODO: Reemplazar 1L con el id real de la liga actual del usuario
            intent.putExtra("EXTRA_LEAGUE_ID", 1L);
            startActivity(intent);
        });

        // Botón Capitán (mostrar info del capitán actual)
        if (btnSetCaptain != null) {
            btnSetCaptain.setOnClickListener(v -> {
                showCaptainInfo();
            });
        }
    }

    private void setupViewModel() {
        vm = new ViewModelProvider(this).get(FootballViewModel.class);

        // Observer para la plantilla (propiedad en liga)
        vm.getMyTeamApiPlayers(leagueId, 1L).observe(this, players -> {
            currentSquad.clear();
            if (players != null && !players.isEmpty()) {
                currentSquad.addAll(players);
                adapter.submit(players);

                // Cargar capitán guardado
                currentCaptainId = CaptainManager.getCaptain(this, teamId);
                adapter.setCaptainId(currentCaptainId);

                recalculatePoints();
                updateTeamStats();
            } else {
                android.util.Log.d("MyTeamActivity", "Aún no tienes jugadores. Ve al mercado de fichajes.");
                adapter.submit(new java.util.ArrayList<>());
                recalculatePoints();
                updateTeamStats();
            }
        });
    }

    private void getIntentData() {
        // Obtener datos del intent
        teamId = getIntent().getIntExtra(EXTRA_TEAM_ID, 1); // Default team ID = 1
        leagueId = getIntent().getLongExtra(EXTRA_LEAGUE_ID, 1L);
        leagueName = getIntent().getStringExtra(EXTRA_LEAGUE_NAME);

        if (leagueName != null) {
            tvHeader.setText(leagueName);
        }
    }

    private void loadTeamData() {
        // La lista se actualizará automáticamente por el observer de ownership
        // aquí solo recalculamos/actualizamos el header si fuese necesario
        recalculatePoints();
        updateTeamStats();
    }

    private void recalculatePoints() {
        int totalPoints = 0;

        for (PlayerAPI player : currentSquad) {
            int playerPoints = player.getPoints();

            // Multiplicar por 2 si es el capitán
            if (player.getId() == currentCaptainId) {
                playerPoints *= 2;
            }

            totalPoints += playerPoints;
        }

        // Actualizar UI
        if (tvPoints != null) {
            tvPoints.setText(String.valueOf(totalPoints));
        }
    }

    private void updateTeamStats() {
        // Calcular valor del equipo (mock)
        double teamValue = currentSquad.size() * 12.5; // Promedio de 12.5M por jugador
        if (tvTeamValue != null) {
            tvTeamValue.setText(String.format("%.1fM €", teamValue));
        }

        // Presupuesto restante (mock)
        double remainingBudget = 150.0 - teamValue; // 150M presupuesto inicial
        if (tvRemainingBudget != null) {
            tvRemainingBudget.setText(String.format("%.1fM €", Math.max(0, remainingBudget)));
        }
    }

    private void showCaptainInfo() {
        String captainName = "Ninguno";

        // Buscar el nombre del capitán actual
        for (PlayerAPI player : currentSquad) {
            if (player.getId() == currentCaptainId) {
                captainName = player.getName();
                break;
            }
        }

        com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content),
                "Capitán actual: " + captainName +
                        "\n\nToca un jugador para asignarlo como capitán",
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
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