package com.example.housemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

// Pantalla de inicio con navegación por Drawer y accesos rápidos.
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Preparo toolbar y drawer.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Accesos del dashboard.
        Button btnViewLeagues = findViewById(R.id.btn_view_leagues);
        Button btnCreateLeague = findViewById(R.id.btn_create_league);
        Button btnJoinLeague = findViewById(R.id.btn_join_league);

        // Ir al listado de ligas.
        btnViewLeagues.setOnClickListener(v ->
                startActivity(new Intent(this, LeaguesActivity.class)));

        // Ir al flujo de creación.
        btnCreateLeague.setOnClickListener(v ->
                startActivity(new Intent(this, CreateLeagueActivity.class)));

        // Placeholder hasta implementar.
        btnJoinLeague.setOnClickListener(v ->
                Toast.makeText(this, "Unirse a liga - Próximamente", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Gestiono clicks del drawer.
        int id = item.getItemId();

        if (id == R.id.nav_leagues) {
            startActivity(new Intent(this, LeaguesActivity.class));
        } else if (id == R.id.nav_create_league) {
            startActivity(new Intent(this, CreateLeagueActivity.class));
        } else if (id == R.id.nav_statistics) {
            android.widget.Toast.makeText(this, "Estadísticas - Próximamente", android.widget.Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            android.widget.Toast.makeText(this, "Ajustes - Próximamente", android.widget.Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_help) {
            android.widget.Toast.makeText(this, "Ayuda - Próximamente", android.widget.Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
