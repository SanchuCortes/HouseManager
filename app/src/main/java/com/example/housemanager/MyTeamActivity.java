package com.example.housemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class MyTeamActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvLeagueName, tvTeamValue, tvRemainingBudget, tvPoints;
    private RecyclerView rvPlayers;
    private MaterialButton btnTransfers, btnSetCaptain;

    private String leagueName;
    private List<Player> playersList;
    private PlayersAdapter playersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_team);

        initViews();
        setupToolbar();
        getDataFromIntent();
        setupRecyclerView();
        loadPlayerData();
        setupButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvLeagueName = findViewById(R.id.tv_league_name);
        tvTeamValue = findViewById(R.id.tv_team_value);
        tvRemainingBudget = findViewById(R.id.tv_remaining_budget);
        tvPoints = findViewById(R.id.tv_points);
        rvPlayers = findViewById(R.id.rv_players);
        btnTransfers = findViewById(R.id.btn_transfers);
        btnSetCaptain = findViewById(R.id.btn_set_captain);
    }

    // Configuramos el toolbar con el botón de volver
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Equipo");
        }
    }

    // Recogemos el nombre de la liga de la activity anterior
    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            leagueName = intent.getStringExtra("league_name");
            if (leagueName == null) {
                leagueName = "Mi Liga Fantasy";
            }
        }
        tvLeagueName.setText(leagueName);
    }

    // Configuramos la lista de jugadores
    private void setupRecyclerView() {
        playersList = new ArrayList<>();
        playersAdapter = new PlayersAdapter(playersList, this);
        rvPlayers.setLayoutManager(new LinearLayoutManager(this));
        rvPlayers.setAdapter(playersAdapter);
    }

    // Cargamos los datos de ejemplo del equipo (después vendrá de BD/API)
    private void loadPlayerData() {
        // Estadísticas generales del equipo
        tvTeamValue.setText("147.5M €");
        tvRemainingBudget.setText("2.5M €");
        tvPoints.setText("156 pts");

        // Lista de jugadores del equipo (datos de ejemplo)
        playersList.clear();

        // Porteros
        playersList.add(new Player("Ter Stegen", "FC Barcelona", "POR", 15.0, true, false));
        playersList.add(new Player("Dmitrović", "Sevilla FC", "POR", 8.5, false, false));

        // Defensas
        playersList.add(new Player("Carvajal", "Real Madrid", "DEF", 18.0, false, false));
        playersList.add(new Player("Alaba", "Real Madrid", "DEF", 16.5, false, true)); // Es el capitán
        playersList.add(new Player("Koundé", "FC Barcelona", "DEF", 15.0, false, false));
        playersList.add(new Player("Gayà", "Valencia CF", "DEF", 12.0, false, false));
        playersList.add(new Player("Nacho", "Real Madrid", "DEF", 8.0, false, false));

        // Centrocampistas
        playersList.add(new Player("Modric", "Real Madrid", "CEN", 20.0, false, false));
        playersList.add(new Player("Pedri", "FC Barcelona", "CEN", 18.5, false, false));
        playersList.add(new Player("Canales", "Real Betis", "CEN", 14.0, false, false));
        playersList.add(new Player("Soler", "Valencia CF", "CEN", 12.5, false, false));

        // Delanteros
        playersList.add(new Player("Benzema", "Real Madrid", "DEL", 25.0, false, false));
        playersList.add(new Player("Lewandowski", "FC Barcelona", "DEL", 22.0, false, false));
        playersList.add(new Player("Morata", "Atlético Madrid", "DEL", 16.0, false, false));
        playersList.add(new Player("Isak", "Real Sociedad", "DEL", 14.5, false, false));

        playersAdapter.notifyDataSetChanged();
    }

    // Configuramos los botones de acciones rápidas
    private void setupButtons() {
        btnTransfers.setOnClickListener(v -> {
            // Aquí iría la pantalla de transferencias (de momento solo un mensaje)
            showMessage("Transferencias - Próximamente");
        });

        btnSetCaptain.setOnClickListener(v -> {
            showMessage("Cambiar Capitán - Próximamente");
        });
    }

    private void showMessage(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Clase para representar un jugador del equipo
    public static class Player {
        private String name;
        private String team;
        private String position;
        private double price;
        private boolean isPlaying; // Si está en el once titular
        private boolean isCaptain;

        public Player(String name, String team, String position, double price,
                      boolean isPlaying, boolean isCaptain) {
            this.name = name;
            this.team = team;
            this.position = position;
            this.price = price;
            this.isPlaying = isPlaying;
            this.isCaptain = isCaptain;
        }

        // Getters básicos
        public String getName() { return name; }
        public String getTeam() { return team; }
        public String getPosition() { return position; }
        public double getPrice() { return price; }
        public boolean isPlaying() { return isPlaying; }
        public boolean isCaptain() { return isCaptain; }

        public void setPlaying(boolean playing) { isPlaying = playing; }
        public void setCaptain(boolean captain) { isCaptain = captain; }
    }
}