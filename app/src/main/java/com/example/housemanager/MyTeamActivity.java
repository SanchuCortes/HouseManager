package com.example.housemanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

    private FootballViewModel vm;
    private PlayersSimpleAdapter adapter;

    private TextView tvHeader;
    private TextView tvPoints;          // <-- CAMBIA a tu id real si no es éste
    private MaterialButton btnTransfers;
    private MaterialButton btnCaptain;  // se mantiene por si quieres usarlo para otra acción

    private int teamId = -1;
    private int currentCaptainId = -1;
    private final List<PlayerAPI> currentSquad = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_team);

        tvHeader     = findViewById(R.id.tv_league_header);
        tvPoints     = findViewById(R.id.tv_points);
        btnTransfers = findViewById(R.id.btn_transfers);
     //   btnCaptain   = findViewById(R.id.btn_captain);

        tvHeader.setText("Mi Liga Fantasy");

        RecyclerView rv = findViewById(R.id.recycler_players);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlayersSimpleAdapter(player -> {
            // CLICK EN JUGADOR → asignar capitán
            currentCaptainId = player.getId();
            CaptainManager.setCaptain(this, teamId, currentCaptainId);
            adapter.setCaptainId(currentCaptainId);
            recalcTotals();
        });
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(FootballViewModel.class);
        vm.getSquad().observe(this, players -> {
            currentSquad.clear();
            if (players != null) currentSquad.addAll(players);
            adapter.submit(players);

            // Cargar capitán guardado
            currentCaptainId = CaptainManager.getCaptain(this, teamId);
            adapter.setCaptainId(currentCaptainId);
            recalcTotals();
        });

        teamId = getIntent().getIntExtra(EXTRA_TEAM_ID, -1);
        if (teamId != -1) {
            vm.loadSquad(teamId);
        }

        btnTransfers.setOnClickListener(v ->
                startActivity(new Intent(this, TransferMarketActivity.class))
        );

        // btnCaptain se puede mantener para otra acción (p.ej. abrir mercado o mostrar quién es el capitán)
        btnCaptain.setOnClickListener(v -> {
            // opcional: mostrar un dialog con el capitán actual
        });
    }

    private void recalcTotals() {
        int total = 0;
        for (PlayerAPI p : currentSquad) {
            int pts = p.getPoints(); // asegúrate de que PlayerAPI tiene getPoints()
            if (p.getId() == currentCaptainId) pts *= 2;
            total += pts;
        }
        if (tvPoints != null) {
            tvPoints.setText(String.valueOf(total));
        }
    }
}
