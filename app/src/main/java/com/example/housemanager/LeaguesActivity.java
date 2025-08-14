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

import java.util.List;

// Muestra mis ligas. Si no hay, enseña estado vacío con CTA.
public class LeaguesActivity extends AppCompatActivity {

    private RecyclerView rvLeagues;
    private View emptyState;
    private FloatingActionButton fabCreateLeague;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leagues);

        // Toolbar básica con back.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mis Ligas");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvLeagues = findViewById(R.id.recycler_view_leagues);
        emptyState = findViewById(R.id.empty_state_layout);
        fabCreateLeague = findViewById(R.id.fab_create_league);

        rvLeagues.setLayoutManager(new LinearLayoutManager(this));

        // CTA flotante a crear liga.
        fabCreateLeague.setOnClickListener(v ->
                startActivity(new Intent(this, CreateLeagueActivity.class)));

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
        List<LeagueManager.League> list = LeagueManager.getInstance().getUserLeagues();
        if (list == null || list.isEmpty()) {
            rvLeagues.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvLeagues.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            rvLeagues.setAdapter(new LeaguesAdapter(list, this));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Back de la toolbar.
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
