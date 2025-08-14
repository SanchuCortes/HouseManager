package com.example.housemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class LeaguesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvLeagues;
    private FloatingActionButton fabCreateLeague;

    private List<LeagueManager.League> leaguesList;
    private LeaguesAdapter leaguesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leagues);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadUserLeagues();
        setupFab();
        setupEmptyStateButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvLeagues = findViewById(R.id.recycler_view_leagues);
        fabCreateLeague = findViewById(R.id.fab_create_league);
    }

    // Configuramos el toolbar con botón de volver
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Mis Ligas");
        }
    }

    // Configuramos el RecyclerView con las ligas
    private void setupRecyclerView() {
        leaguesList = new ArrayList<>();
        leaguesAdapter = new LeaguesAdapter(leaguesList, this);
        rvLeagues.setLayoutManager(new LinearLayoutManager(this));
        rvLeagues.setAdapter(leaguesAdapter);
    }

    // Cargamos las ligas del usuario desde LeagueManager
    private void loadUserLeagues() {
        // Limpiar lista actual
        leaguesList.clear();

        // Obtener las ligas desde LeagueManager
        List<LeagueManager.League> leagues = LeagueManager.getInstance().getAllLeagues();
        leaguesList.addAll(leagues);

        // Actualizar el adapter
        leaguesAdapter.notifyDataSetChanged();

        // Mostrar u ocultar la lista según si hay ligas
        if (leaguesList.isEmpty()) {
            rvLeagues.setVisibility(View.GONE);
            // Mostrar estado vacío (si tienes el layout en tu XML)
            View emptyState = findViewById(R.id.empty_state_layout);
            if (emptyState != null) {
                emptyState.setVisibility(View.VISIBLE);
            }
        } else {
            rvLeagues.setVisibility(View.VISIBLE);
            // Ocultar estado vacío
            View emptyState = findViewById(R.id.empty_state_layout);
            if (emptyState != null) {
                emptyState.setVisibility(View.GONE);
            }
        }

        // Log para debug
        System.out.println("Ligas cargadas: " + leaguesList.size());
        for (LeagueManager.League league : leaguesList) {
            System.out.println("- " + league.toString());
        }
    }

    // Configuramos el botón flotante para crear nueva liga
    private void setupFab() {
        if (fabCreateLeague != null) {
            fabCreateLeague.setOnClickListener(v -> {
                Intent intent = new Intent(LeaguesActivity.this, CreateLeagueActivity.class);
                startActivity(intent);
            });
        }
    }

    // Configuramos los botones del estado vacío
    private void setupEmptyStateButtons() {
        // Botones del estado vacío (si existen en tu XML)
        View btnCreateEmpty = findViewById(R.id.btn_create_league_empty);
        View btnJoinEmpty = findViewById(R.id.btn_join_league_empty);

        if (btnCreateEmpty != null) {
            btnCreateEmpty.setOnClickListener(v -> {
                Intent intent = new Intent(LeaguesActivity.this, CreateLeagueActivity.class);
                startActivity(intent);
            });
        }

        if (btnJoinEmpty != null) {
            btnJoinEmpty.setOnClickListener(v -> {
                // Por ahora solo un mensaje
                android.widget.Toast.makeText(this, "Funcionalidad próximamente", android.widget.Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Recargamos las ligas cada vez que volvemos a esta pantalla
    @Override
    protected void onResume() {
        super.onResume();
        loadUserLeagues();
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