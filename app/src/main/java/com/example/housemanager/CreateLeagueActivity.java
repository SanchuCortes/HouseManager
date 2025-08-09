package com.example.housemanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class CreateLeagueActivity extends AppCompatActivity {

    private static final String TAG = "CreateLeagueActivity";

    // Elementos de la UI
    private Toolbar toolbar;
    private MaterialCardView cardPrivateLeague;
    private MaterialCardView cardCommunityLeague;
    private MaterialButton btnCreatePrivate;
    private MaterialButton btnCreateCommunity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Iniciando CreateLeagueActivity");
        setContentView(R.layout.activity_create_league);

        initViews();
        setupToolbar();
        setupClickListeners();

        Log.d(TAG, "onCreate: CreateLeagueActivity iniciada correctamente");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cardPrivateLeague = findViewById(R.id.card_private_league);
        cardCommunityLeague = findViewById(R.id.card_community_league);
        btnCreatePrivate = findViewById(R.id.btn_create_private);
        btnCreateCommunity = findViewById(R.id.btn_create_community);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Crear Liga");
        }
    }

    private void setupClickListeners() {
        // Card Liga Privada
        if (cardPrivateLeague != null) {
            cardPrivateLeague.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToLeagueConfig("private");
                }
            });
        }

        // Botón Liga Privada
        if (btnCreatePrivate != null) {
            btnCreatePrivate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToLeagueConfig("private");
                }
            });
        }

        // Card Liga Comunitaria
        if (cardCommunityLeague != null) {
            cardCommunityLeague.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToLeagueConfig("community");
                }
            });
        }

        // Botón Liga Comunitaria
        if (btnCreateCommunity != null) {
            btnCreateCommunity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToLeagueConfig("community");
                }
            });
        }
    }

    private void navigateToLeagueConfig(String leagueType) {
        Log.d(TAG, "Navegando a configuración de liga: " + leagueType);

        Intent intent = new Intent(this, LeagueConfigActivity.class);
        intent.putExtra("league_type", leagueType);
        startActivity(intent);

        showToast("Configurando liga " + leagueType);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}