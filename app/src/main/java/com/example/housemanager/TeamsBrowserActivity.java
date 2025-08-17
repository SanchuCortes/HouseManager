package com.example.housemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.housemanager.ActivityTeamBrowserBinding;
import com.example.housemanager.market.Team;
import com.example.housemanager.FootballViewModel;

public class TeamBrowserActivity extends AppCompatActivity implements com.example.housemanager.ui.team.TeamListAdapter.OnTeamClickListener {

    public static final String EXTRA_TEAM_ID    = "EXTRA_TEAM_ID";
    public static final String EXTRA_TEAM_NAME  = "EXTRA_TEAM_NAME";
    public static final String EXTRA_TEAM_CREST = "EXTRA_TEAM_CREST";

    private ActivityTeamBrowserBinding binding;
    private FootballViewModel vm;
    private com.example.housemanager.ui.team.TeamListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTeamBrowserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Equipos (LaLiga)");
        }

        // Recycler
        adapter = new com.example.housemanager.ui.team.TeamListAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        // ViewModel
        vm = new ViewModelProvider(this).get(FootballViewModel.class);

        // Observa datos de Room
        vm.getTeams().observe(this, teams -> adapter.submitList(teams));

        // Estado de carga
        vm.getLoading().observe(this, loading -> {
            binding.progress.setVisibility(Boolean.TRUE.equals(loading) ? android.view.View.VISIBLE : android.view.View.GONE);
        });
        vm.getProgress().observe(this, msg -> binding.txtStatus.setText(msg != null ? msg : ""));
        vm.getError().observe(this, err -> {
            if (err != null) binding.txtStatus.setText(err);
        });

        // Dispara la sincronización con la API (equipos + plantillas)
        vm.refreshAll();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    // Al tocar un equipo, abre el detalle (squad)
    @Override
    public void onTeamClick(Team team) {
        Intent i = new Intent(this, TeamDetailActivity.class); // Asegúrate de tener esta Activity
        i.putExtra(EXTRA_TEAM_ID, team.getTeamId());
        i.putExtra(EXTRA_TEAM_NAME, team.getName());
        i.putExtra(EXTRA_TEAM_CREST, team.getCrestUrl());
        startActivity(i);
    }
}
