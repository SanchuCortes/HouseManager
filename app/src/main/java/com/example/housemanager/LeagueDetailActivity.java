package com.example.housemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

public class LeagueDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvLeagueName, tvLeagueType, tvParticipants, tvBudget;
    private TextView tvMarketUpdate, tvTeamType, tvTransferRules, tvGameConfig;
    private ImageView ivLeagueIcon;
    private Button btnMyTeam, btnClassification, btnTransfers;
    private Chip chipActive, chipPrivate;

    private String leagueName;
    private String leagueType;
    private boolean isPrivate;
    private String budget;
    private String participants;
    private String marketHour;
    private String teamType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league_detail);

        initViews();
        setupToolbar();
        getDataFromIntent();
        displayLeagueInfo();
        setupButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvLeagueName = findViewById(R.id.tv_league_name);
        tvLeagueType = findViewById(R.id.tv_league_type);
        tvParticipants = findViewById(R.id.tv_participants);
        tvBudget = findViewById(R.id.tv_budget);
        tvMarketUpdate = findViewById(R.id.tv_market_update);
        tvTeamType = findViewById(R.id.tv_team_type);
        tvTransferRules = findViewById(R.id.tv_transfer_rules);
        tvGameConfig = findViewById(R.id.tv_game_config);
        ivLeagueIcon = findViewById(R.id.iv_league_icon);
        btnMyTeam = findViewById(R.id.btn_my_team);
        btnClassification = findViewById(R.id.btn_classification);
        btnTransfers = findViewById(R.id.btn_transfers);
        chipActive = findViewById(R.id.chip_active);
        chipPrivate = findViewById(R.id.chip_private);
    }

    // Configuramos el toolbar con botón de volver
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle de Liga");
        }
    }

    // Recogemos los datos que nos pasan desde LeaguesActivity
    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            leagueName = intent.getStringExtra("league_name");
            leagueType = intent.getStringExtra("league_type");
            isPrivate = intent.getBooleanExtra("is_private", true);

            // Datos adicionales
            budget = intent.getStringExtra("budget");
            participants = intent.getStringExtra("participants");
            marketHour = intent.getStringExtra("market_hour");
            teamType = intent.getStringExtra("team_type");

            // Valores por defecto si no se pasan datos
            if (leagueName == null) {
                leagueName = "Mi Liga Fantasy";
                leagueType = "Liga Privada";
                isPrivate = true;
            }

            // Asignar valores por defecto para datos adicionales
            if (budget == null) budget = "150M €";
            if (participants == null) participants = "1/12 jugadores";
            if (marketHour == null) marketHour = "14:00";
            if (teamType == null) teamType = "Equipo Vacío";
        }
    }

    // Mostramos toda la info de la liga
    private void displayLeagueInfo() {
        tvLeagueName.setText(leagueName);
        tvLeagueType.setText(leagueType);

        // Icono según el tipo de liga
        if (isPrivate) {
            ivLeagueIcon.setImageResource(R.drawable.ic_person);
            chipPrivate.setVisibility(View.VISIBLE);
        } else {
            ivLeagueIcon.setImageResource(R.drawable.ic_group_add);
            chipPrivate.setVisibility(View.GONE);
        }

        chipActive.setVisibility(View.VISIBLE);

        // Mostrar datos reales en lugar de mock
        loadRealData();
    }

    // Cargamos los datos reales recibidos desde la liga
    private void loadRealData() {
        // Usar datos reales en lugar de mock
        tvParticipants.setText("👥 " + participants);
        tvBudget.setText("💰 Presupuesto: " + budget);
        tvMarketUpdate.setText("🕐 Actualización: " + marketHour + "h diaria");
        tvTeamType.setText("Equipo inicial: " + teamType);

        // Estas configuraciones se mantienen como mock por ahora
        String transferRules = "• Robos por cláusula: Activado\n" +
                "• Cesiones: Desactivado\n" +
                "• Días de bloqueo: 14 días";
        tvTransferRules.setText(transferRules);

        String gameConfig = "• Capitán (x2): Activado\n" +
                "• Banquillo: Activado\n" +
                "• Entrenador: Desactivado";
        tvGameConfig.setText(gameConfig);
    }

    // Configuramos los botones de acciones
    private void setupButtons() {
        btnMyTeam.setOnClickListener(v -> {
            Intent intent = new Intent(LeagueDetailActivity.this, MyTeamActivity.class);
            intent.putExtra("league_name", leagueName);
            startActivity(intent);
        });

        btnClassification.setOnClickListener(v -> {
            showMessage("Clasificación - Próximamente");
        });

        btnTransfers.setOnClickListener(v -> {
            showMessage("Transferencias - Próximamente");
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Animación suave al volver (opcional)
        // overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}