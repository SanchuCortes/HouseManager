package com.example.housemanager;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

// Pantalla "Mi equipo" con jugadores mock y FAB de placeholder.
public class MyTeamActivity extends AppCompatActivity {

    // Modelo ligero para no mezclar con entidades Room.
    public static class PlayerLite {
        private final String name, position;
        private final int price; // M€

        public PlayerLite(String name, String position, int price) {
            this.name = name; this.position = position; this.price = price;
        }
        public String getName() { return name; }
        public String getPosition() { return position; }
        public int getPrice() { return price; }
    }

    private final List<PlayerLite> players = new ArrayList<>();
    private PlayersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_team);

        // Toolbar + back.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Mi equipo");

        // Cabecera con nombre de liga.
        String leagueName = getIntent().getStringExtra("EXTRA_LEAGUE_NAME");
        TextView tvHeader = findViewById(R.id.tv_league_header);
        tvHeader.setText("Liga: " + (leagueName != null ? leagueName : "—"));

        // Recycler + datos mock.
        RecyclerView rv = findViewById(R.id.recycler_players);
        rv.setLayoutManager(new LinearLayoutManager(this));
        loadMockPlayers();
        adapter = new PlayersAdapter(players);
        rv.setAdapter(adapter);

        // FAB sin lógica real (placeholder).
        findViewById(R.id.fab_add_player)
                .setOnClickListener(v -> Snackbar.make(v, "Funcionalidad en desarrollo", Snackbar.LENGTH_SHORT).show());
    }

    // Lleno la lista con jugadores de ejemplo para la demo.
    private void loadMockPlayers() {
        players.clear();
        players.add(new PlayerLite("J. Oblak", "GK", 10));
        players.add(new PlayerLite("R. Araujo", "DEF", 12));
        players.add(new PlayerLite("D. Carvajal", "DEF", 11));
        players.add(new PlayerLite("F. Valverde", "MID", 15));
        players.add(new PlayerLite("Pedri", "MID", 14));
        players.add(new PlayerLite("V. Junior", "FWD", 20));
        players.add(new PlayerLite("R. Lewandowski", "FWD", 22));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Back de la toolbar.
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
