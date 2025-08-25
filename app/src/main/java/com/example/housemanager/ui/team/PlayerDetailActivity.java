package com.example.housemanager.ui.team;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.housemanager.R;

public class PlayerDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_detail);

        setupToolbar();
        loadPlayerData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalles del Jugador");
        }
    }

    private void loadPlayerData() {
        // Obtener datos del intent
        int playerId = getIntent().getIntExtra("player_id", -1);
        String playerName = getIntent().getStringExtra("player_name");
        String playerPosition = getIntent().getStringExtra("player_position");
        String playerNationality = getIntent().getStringExtra("player_nationality");
        int playerPoints = getIntent().getIntExtra("player_points", 0);
        String teamName = getIntent().getStringExtra("team_name");

        // Referencias a las vistas
        TextView textTitle = findViewById(R.id.textTitle);
        TextView textSubtitle = findViewById(R.id.textSubtitle);

        // Mostrar información del jugador
        if (textTitle != null) {
            textTitle.setText(playerName != null ? playerName : "Jugador Desconocido");
        }

        if (textSubtitle != null) {
            StringBuilder details = new StringBuilder();

            if (teamName != null) {
                details.append("🏟️ Equipo: ").append(teamName).append("\n");
            }

            if (playerPosition != null) {
                details.append("⚽ Posición: ").append(translatePosition(playerPosition)).append("\n");
            }

            if (playerNationality != null) {
                details.append("🌍 Nacionalidad: ").append(playerNationality).append("\n");
            }

            details.append("🏆 Puntos totales: ").append(playerPoints).append("\n");
            details.append("🆔 ID: ").append(playerId);

            textSubtitle.setText(details.toString());
        }

        // Cambiar título de la toolbar
        if (getSupportActionBar() != null && playerName != null) {
            getSupportActionBar().setTitle(playerName);
        }
    }

    /**
     * Traduce posiciones del inglés al español si es necesario
     */
    private String translatePosition(String position) {
        if (position == null) return "Desconocida";

        // Si ya está en español, devolverlo
        if (position.equals("Portero") || position.equals("Defensa") ||
                position.equals("Medio") || position.equals("Delantero")) {
            return position;
        }

        // Traducir del inglés
        String pos = position.toLowerCase();
        if (pos.contains("goalkeeper") || pos.contains("keeper")) {
            return "Portero";
        } else if (pos.contains("defender") || pos.contains("defence")) {
            return "Defensa";
        } else if (pos.contains("midfielder") || pos.contains("midfield")) {
            return "Centrocampista";
        } else if (pos.contains("forward") || pos.contains("attacker") || pos.contains("striker")) {
            return "Delantero";
        }

        return position; // Devolver original si no se puede traducir
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