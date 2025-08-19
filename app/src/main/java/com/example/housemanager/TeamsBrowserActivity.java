package com.example.housemanager;

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
    private FootballViewModel vm;
    private TeamsListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTeamsBrowserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Equipos (LaLiga)");
        }

        // Recycler
        adapter = new TeamsListAdapter(this);
        binding.recyclerTeams.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTeams.setAdapter(adapter);

        // ViewModel
        vm = new ViewModelProvider(this).get(FootballViewModel.class);
        vm.getTeams().observe(this, teams -> adapter.submit(teams));
        vm.loadTeams();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Click en un equipo: por ahora, toast
    @Override
    public void onTeamClick(TeamAPI team) {
        Toast.makeText(this, "Equipo: " + (team.getName() != null ? team.getName() : "-"), Toast.LENGTH_SHORT).show();
        // Si luego tienes TeamDetailActivity, navegas desde aquí.
    }
}
