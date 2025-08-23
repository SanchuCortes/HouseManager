package com.example.housemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.housemanager.api.models.PlayerAPI;
import com.example.housemanager.viewmodel.FootballViewModel;

public class TeamDetailActivity extends AppCompatActivity implements PlayersSimpleAdapter.OnPlayerClick {

    public static final String EXTRA_TEAM_ID = "team_id";
    public static final String EXTRA_TEAM_NAME = "team_name";
    public static final String EXTRA_TEAM_CREST = "team_crest";

    private FootballViewModel viewModel;
    private PlayersSimpleAdapter adapter;

    private int teamId = -1;
    private String teamName = "Equipo";
    private String teamCrest = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);

        getIntentData();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        loadTeamData();
    }

    private void getIntentData() {
        teamId = getIntent().getIntExtra(EXTRA_TEAM_ID, -1);
        teamName = getIntent().getStringExtra(EXTRA_TEAM_NAME);
        teamCrest = getIntent().getStringExtra(EXTRA_TEAM_CREST);

        if (teamName == null) teamName = "Equipo";
        if (teamCrest == null) teamCrest = "";

        // Compatibilidad con otros extras
        if (teamId == -1) {
            teamId = getIntent().getIntExtra("TEAM_ID", -1);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            try {
                setSupportActionBar(toolbar);
            } catch (Exception e) {
                android.util.Log.w("TeamDetailActivity", "Error al configurar toolbar", e);
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(teamName);
            }
        } else {
            android.util.Log.w("TeamDetailActivity", "Toolbar no encontrada en el layout");
        }
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_squad);
        if (recyclerView == null) {
            // Si no existe recycler_squad, buscar recycler_players
            recyclerView = findViewById(R.id.recycler_players);
        }

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new PlayersSimpleAdapter(this);
            recyclerView.setAdapter(adapter);
        } else {
            android.util.Log.e("TeamDetail", "Error: RecyclerView no encontrado");
            finish();
            return;
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(FootballViewModel.class);

        // Observer para la plantilla del equipo
        viewModel.getSquad().observe(this, players -> {
            if (players != null && !players.isEmpty()) {
                adapter.submit(players);
                android.util.Log.d("TeamDetail", "Plantilla cargada: " + players.size() + " jugadores");
            } else {
                android.util.Log.d("TeamDetail", "No se encontraron jugadores para este equipo");
            }
        });
    }

    private void loadTeamData() {
        // Configurar header del equipo si existen las vistas
        TextView tvTitle = findViewById(R.id.tv_title);
        ImageView ivCrest = findViewById(R.id.iv_crest);

        if (tvTitle != null) {
            tvTitle.setText(teamName);
        }

        if (ivCrest != null && !teamCrest.isEmpty()) {
            Glide.with(this)
                    .load(teamCrest)
                    .placeholder(R.drawable.ic_group_add)
                    .error(R.drawable.ic_group_add)
                    .into(ivCrest);
        }

        // Cargar plantilla del equipo
        if (teamId != -1) {
            viewModel.loadSquad(teamId);
            android.util.Log.d("TeamDetail", "Cargando plantilla del equipo: " + teamId);
        } else {
            android.util.Log.e("TeamDetail", "ID de equipo no válido");
        }
    }

    @Override
    public void onPlayerClick(PlayerAPI player) {
        // Abrir detalles del jugador
        Intent intent = new Intent(this, com.example.housemanager.ui.PlayerDetailActivity.class);
        intent.putExtra("player_id", player.getId());
        intent.putExtra("player_name", player.getName());
        intent.putExtra("player_position", player.getPosition());
        intent.putExtra("player_nationality", player.getNationality());
        intent.putExtra("player_points", player.getPoints());
        intent.putExtra("team_name", teamName);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}