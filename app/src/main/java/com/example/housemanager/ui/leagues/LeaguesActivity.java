package com.example.housemanager.ui.leagues;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.housemanager.ui.adapters.LeaguesAdapter;
import com.example.housemanager.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// Muestra mis ligas. Si no hay, enseña estado vacío con CTA.
public class LeaguesActivity extends AppCompatActivity {

    private RecyclerView rvLeagues;
    private View emptyState;
    private FloatingActionButton fabCreateLeague;
    private FloatingActionButton fabDeleteLeague;

    private java.util.List<String> currentLeagueNames = new java.util.ArrayList<>();
    private java.util.List<Integer> currentLeagueIds = new java.util.ArrayList<>();

    private com.example.housemanager.repository.FootballRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leagues);

        repository = com.example.housemanager.repository.FootballRepository.getInstance(this);

        // Toolbar básica con back.
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            try {
                setSupportActionBar(toolbar);
            } catch (Exception e) {
                android.util.Log.w("LeaguesActivity", "Error al configurar toolbar", e);
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Mis Ligas");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            android.util.Log.w("LeaguesActivity", "Toolbar no encontrada en el layout");
        }

        rvLeagues = findViewById(R.id.recycler_view_leagues);
        emptyState = findViewById(R.id.empty_state_layout);
        fabCreateLeague = findViewById(R.id.fab_create_league);
        fabDeleteLeague = findViewById(R.id.fab_delete_league);

        rvLeagues.setLayoutManager(new LinearLayoutManager(this));

        // CTA flotante a crear liga.
        fabCreateLeague.setOnClickListener(v ->
                startActivity(new Intent(this, CreateLeagueActivity.class)));

        // Botón borrar liga: abre diálogo para elegir cuál eliminar
        if (fabDeleteLeague != null) {
            fabDeleteLeague.setOnClickListener(v -> showDeleteLeagueDialog());
        }

        // Botones del estado vacío (si existen en tu layout).
        View btnCreateEmpty = findViewById(R.id.btn_create_league_empty);
        View btnJoinEmpty = findViewById(R.id.btn_join_league_empty);

        if (btnCreateEmpty != null) {
            btnCreateEmpty.setOnClickListener(v ->
                    startActivity(new Intent(this, CreateLeagueActivity.class)));
        }
        if (btnJoinEmpty != null) {
            btnJoinEmpty.setOnClickListener(v ->
                    android.widget.Toast.makeText(this, "Unirse a liga - Próximamente", android.widget.Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserLeagues();
    }

    // Cargo ligas mock y actualizo UI.
    private void loadUserLeagues() {
        // se usa para el diálogo de borrado
        // Observar ligas desde Room y actualizar la UI
        com.example.housemanager.database.HouseManagerDatabase db = com.example.housemanager.database.HouseManagerDatabase.getInstance(getApplicationContext());
        db.leagueDao().getAllLeagues().observe(this, entities -> {
            java.util.List<LeagueManager.League> list = new java.util.ArrayList<>();
            java.util.List<Integer> ids = new java.util.ArrayList<>();
            if (entities != null) {
                for (com.example.housemanager.database.entities.LeagueEntity e : entities) {
                    LeagueManager.League l = new LeagueManager.League(
                            e.getName(),
                            e.getType(),
                            "Privada".equalsIgnoreCase(e.getType()),
                            e.getBudget(),
                            e.getMarketHour(),
                            e.getTeamType(),
                            e.getParticipants(),
                            e.getStatus(),
                            e.getCreatedDate()
                    );
                    list.add(l);
                    ids.add(e.getId());
                }
            }

            if (list.isEmpty()) {
                rvLeagues.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            } else {
                rvLeagues.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
                rvLeagues.setAdapter(new LeaguesAdapter(list, ids, this));
            }

            // cache para diálogo de borrado
            currentLeagueNames.clear();
            currentLeagueIds.clear();
            if (entities != null) {
                for (com.example.housemanager.database.entities.LeagueEntity e : entities) {
                    currentLeagueNames.add(e.getName());
                    currentLeagueIds.add(e.getId());
                }
            }
        });
    }

    private void showDeleteLeagueDialog() {
        if (currentLeagueIds.isEmpty()) {
            com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "No tienes ligas para eliminar", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
            return;
        }
        String[] items = currentLeagueNames.toArray(new String[0]);
        final int[] selected = {0};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar liga")
                .setSingleChoiceItems(items, 0, (d, which) -> selected[0] = which)
                .setPositiveButton("Eliminar", (d, w) -> {
                    long leagueId = currentLeagueIds.get(selected[0]);
                    repository.deleteLeagueCompletely(leagueId);
                    com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Liga eliminada", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_leagues, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) { finish(); return true; }
        if (id == R.id.action_delete_league) {
            showDeleteLeagueDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
