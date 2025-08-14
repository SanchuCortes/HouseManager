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

        // Ir a Mi equipo (solo navegación).
        btnMyTeam.setOnClickListener(v -> {
            Intent i = new Intent(this, MyTeamActivity.class);
            i.putExtra("EXTRA_LEAGUE_NAME", leagueName);
            startActivity(i);
        });

        // Placeholders.
        btnClass.setOnClickListener(v ->
                android.widget.Toast.makeText(this, "Clasificación - Próximamente", android.widget.Toast.LENGTH_SHORT).show());
        btnTransfers.setOnClickListener(v ->
                android.widget.Toast.makeText(this, "Transferencias - Próximamente", android.widget.Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Back de la toolbar.
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
