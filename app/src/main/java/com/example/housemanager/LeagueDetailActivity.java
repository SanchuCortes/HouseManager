package com.example.housemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

// Muestra los datos de la liga y accesos rápidos.
public class LeagueDetailActivity extends AppCompatActivity {

    private String leagueName;
    private long leagueId = 1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league_detail);

        // Toolbar con back.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Referencias de UI (todo texto).
        TextView tvName = findViewById(R.id.tv_league_name);
        TextView tvType = findViewById(R.id.tv_league_type);
        TextView tvBudget = findViewById(R.id.tv_budget);
        TextView tvMarket = findViewById(R.id.tv_market_update);
        TextView tvTeamType = findViewById(R.id.tv_team_type);
        TextView tvParticipants = findViewById(R.id.tv_participants);

        Button btnMyTeam = findViewById(R.id.btn_my_team);
        Button btnClass = findViewById(R.id.btn_classification);
        Button btnTransfers = findViewById(R.id.btn_transfers);

        // Cojo datos del intent.
        leagueName = getIntent().getStringExtra("EXTRA_NAME");
        String type = getIntent().getStringExtra("EXTRA_TYPE");
        int budget = getIntent().getIntExtra("EXTRA_BUDGET", 150);
        String market = getIntent().getStringExtra("EXTRA_MARKET_HOUR");
        String teamType = getIntent().getStringExtra("EXTRA_TEAM_TYPE");
        int participants = getIntent().getIntExtra("EXTRA_PARTICIPANTS", 0);

        // Pinto textos básicos.
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(leagueName);
        tvName.setText(leagueName);
        tvType.setText(type);
        tvBudget.setText(budget + "M €");
        tvMarket.setText(market != null ? market : "14:00");
        tvTeamType.setText(teamType != null ? teamType : "Equipo Vacío");
        tvParticipants.setText(participants + " participantes");

        // Ocultar card de configuración (dejamos visibles solo las acciones rápidas)
        android.view.View vRules = findViewById(R.id.tv_transfer_rules);
        android.view.View vGameCfg = findViewById(R.id.tv_game_config);
        if (vRules != null) vRules.setVisibility(android.view.View.GONE);
        if (vGameCfg != null) vGameCfg.setVisibility(android.view.View.GONE);

        // Ir a Mi equipo (solo navegación).
        btnMyTeam.setOnClickListener(v -> {
            Intent i = new Intent(this, MyTeamActivity.class);
            i.putExtra("EXTRA_LEAGUE_NAME", leagueName);
            // TODO: Reemplazar 1L con el id real de la liga cuando esté disponible
            i.putExtra(MyTeamActivity.EXTRA_LEAGUE_ID, 1L);
            startActivity(i);
        });

        // Placeholders.
        btnClass.setOnClickListener(v -> {
                Intent intent = new Intent(this, ClassificationActivity.class);
                long lid = getIntent().getLongExtra("EXTRA_LEAGUE_ID", 1L);
                intent.putExtra(ClassificationActivity.EXTRA_LEAGUE_ID, lid);
                startActivity(intent);
        });
        btnTransfers.setOnClickListener(v -> {
                Intent intent = new Intent(this, TransferMarketActivity.class);
                // Pasar id de liga si llegó en el intent
                long lid = getIntent().getLongExtra("EXTRA_LEAGUE_ID", 1L);
                intent.putExtra("EXTRA_LEAGUE_ID", lid);
                startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Back de la toolbar.
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
