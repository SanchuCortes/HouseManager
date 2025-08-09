package com.example.housemanager;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class LeaguesActivity extends AppCompatActivity {

    private static final String TAG = "LeaguesActivity";

    // Elementos de la UI
    private Toolbar toolbar;
    private RecyclerView recyclerViewLeagues;
    private View emptyStateLayout;
    private ImageView emptyStateIcon;
    private TextView emptyStateTitle;
    private TextView emptyStateSubtitle;
    private MaterialButton btnCreateLeagueEmpty;
    private MaterialButton btnJoinLeagueEmpty;

    // Datos
    private List<League> leaguesList;
    private LeaguesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Iniciando LeaguesActivity");
        setContentView(R.layout.activity_leagues);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadLeagues();

        Log.d(TAG, "onCreate: LeaguesActivity iniciada correctamente");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewLeagues = findViewById(R.id.recycler_view_leagues);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        emptyStateIcon = findViewById(R.id.empty_state_icon);
        emptyStateTitle = findViewById(R.id.empty_state_title);
        emptyStateSubtitle = findViewById(R.id.empty_state_subtitle);
        btnCreateLeagueEmpty = findViewById(R.id.btn_create_league_empty);
        btnJoinLeagueEmpty = findViewById(R.id.btn_join_league_empty);

        setupEmptyStateButtons();
    }

    private void setupEmptyStateButtons() {
        if (btnCreateLeagueEmpty != null) {
            btnCreateLeagueEmpty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Navegar a crear liga
                    showToast("Crear Liga clickeado");
                }
            });
        }

        if (btnJoinLeagueEmpty != null) {
            btnJoinLeagueEmpty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Navegar a unirse a liga
                    showToast("Unirse a Liga clickeado");
                }
            });
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Mis Ligas");
        }
    }

    private void setupRecyclerView() {
        leaguesList = new ArrayList<>();
        adapter = new LeaguesAdapter(leaguesList);

        recyclerViewLeagues.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLeagues.setAdapter(adapter);
    }

    private void loadLeagues() {
        // Por ahora simulamos datos - después vendrán de la base de datos
        leaguesList.clear();

        // Simular diferentes tipos de ligas
        leaguesList.add(new League("Liga Premium", "Mi liga privada VIP", 12, "Activa", "created"));
        leaguesList.add(new League("Liga Comunitaria Madrid", "Liga abierta de la comunidad", 24, "Activa", "community"));
        leaguesList.add(new League("Liga Familiar", "El clásico familiar", 6, "Finalizada", "created"));
        leaguesList.add(new League("Liga Pública Española", "Únete y compite", 156, "Activa", "community"));

        updateUI();
    }

    private void updateUI() {
        if (leaguesList.isEmpty()) {
            // Mostrar estado vacío
            showEmptyState();
        } else {
            // Mostrar lista de ligas
            showLeaguesList();
        }
    }

    private void showEmptyState() {
        if (recyclerViewLeagues != null) {
            recyclerViewLeagues.setVisibility(View.GONE);
        }
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.VISIBLE);
        }

        // Configurar mensaje de estado vacío
        if (emptyStateTitle != null) {
            emptyStateTitle.setText("¡Ups! Aún no hay ligas");
        }
        if (emptyStateSubtitle != null) {
            emptyStateSubtitle.setText("Crea tu primera liga o únete a una existente para empezar a jugar");
        }
    }

    private void showLeaguesList() {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
        }
        if (recyclerViewLeagues != null) {
            recyclerViewLeagues.setVisibility(View.VISIBLE);
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Botón atrás presionado
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Clase interna para representar una Liga
    public static class League {
        private String name;
        private String description;
        private int participants;
        private String status;
        private String type; // "created" o "community"

        public League(String name, String description, int participants, String status, String type) {
            this.name = name;
            this.description = description;
            this.participants = participants;
            this.status = status;
            this.type = type;
        }

        // Getters
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getParticipants() { return participants; }
        public String getStatus() { return status; }
        public String getType() { return type; }
    }
}