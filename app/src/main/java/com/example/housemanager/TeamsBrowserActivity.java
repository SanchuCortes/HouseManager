package com.example.housemanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.housemanager.api.models.TeamAPI;
import com.example.housemanager.databinding.ActivityTeamsBrowserBinding;
import com.example.housemanager.ui.adapters.TeamsListAdapter;
import com.example.housemanager.viewmodel.FootballViewModel;

public class TeamsBrowserActivity extends AppCompatActivity implements TeamsListAdapter.OnTeamClickListener {

    private ActivityTeamsBrowserBinding binding;
    private FootballViewModel viewModel;
    private TeamsListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTeamsBrowserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupViewModel();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Equipos de LaLiga");
        }
    }

    private void setupRecyclerView() {
        adapter = new TeamsListAdapter(this);
        binding.recyclerTeams.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTeams.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(FootballViewModel.class);

        // Cuando cambien los equipos, actualizar la lista
        viewModel.getTeams().observe(this, teams -> {
            if (teams != null && !teams.isEmpty()) {
                adapter.submit(teams);
            } else {
                Toast.makeText(this, "No hay equipos disponibles", Toast.LENGTH_SHORT).show();
            }
        });

        // Cargar los equipos
        viewModel.loadTeams();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Cuando toquen un equipo, abrir su detalle
    @Override
    public void onTeamClick(TeamAPI team) {
        Intent intent = new Intent(this, TeamDetailActivity.class);
        intent.putExtra(TeamDetailActivity.EXTRA_TEAM_ID, team.getId());
        intent.putExtra(TeamDetailActivity.EXTRA_TEAM_NAME, team.getName());
        intent.putExtra(TeamDetailActivity.EXTRA_TEAM_CREST, team.getCrest());
        startActivity(intent);
    }
}