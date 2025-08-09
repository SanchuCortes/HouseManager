package com.example.housemanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // Para los logs

    // Elementos del drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;

    // Botones de la pantalla principal
    private MaterialButton btnViewLeagues;
    private MaterialButton btnCreateLeague;
    private MaterialButton btnJoinLeague;

    // TextViews para mostrar datos del usuario
    private TextView tvWelcome;
    private TextView tvActiveLeagues;
    private TextView tvIncompleteLineups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Iniciando MainActivity");
        setContentView(R.layout.activity_main);

        initViews();
        setupToolbar();
        setupNavigationDrawer();
        setupButtonListeners();
        loadInitialData();

        Log.d(TAG, "onCreate: MainActivity iniciada correctamente");
    }

    // Conectar todas las vistas con sus IDs
    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);

        btnViewLeagues = findViewById(R.id.btn_view_leagues);
        btnCreateLeague = findViewById(R.id.btn_create_league);
        btnJoinLeague = findViewById(R.id.btn_join_league);

        tvWelcome = findViewById(R.id.tv_welcome);
        tvActiveLeagues = findViewById(R.id.tv_active_leagues);
        tvIncompleteLineups = findViewById(R.id.tv_incomplete_lineups);
    }

    // Configurar la barra superior con el icono de menú
    private void setupToolbar() {
        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    // Configurar que pasa cuando clickeas en el menú lateral
    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_leagues) {
                    // Navegar a ver ligas
                    Intent intent = new Intent(MainActivity.this, LeaguesActivity.class);
                    startActivity(intent);

                } else if (id == R.id.nav_create_league) {
                    // Navegar a crear liga
                    Intent intent = new Intent(MainActivity.this, CreateLeagueActivity.class);
                    startActivity(intent);

                } else if (id == R.id.nav_join_league) {
                    showToast("Unirse a Liga clickeado");

                } else if (id == R.id.nav_statistics) {
                    showToast("Estadísticas clickeado");

                } else if (id == R.id.nav_settings) {
                    showToast("Ajustes clickeado");

                } else if (id == R.id.nav_help) {
                    showToast("Ayuda clickeado");
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    // Configurar que pasa cuando clickeas los botones principales
    private void setupButtonListeners() {
        btnViewLeagues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la pantalla de ligas
                Intent intent = new Intent(MainActivity.this, LeaguesActivity.class);
                startActivity(intent);
            }
        });

        btnCreateLeague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la pantalla de crear liga (selección de tipo)
                Intent intent = new Intent(MainActivity.this, CreateLeagueActivity.class);
                startActivity(intent);
            }
        });

        btnJoinLeague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí iría la navegación a unirse a liga
                showToast("Navegando a Unirse a Liga...");
            }
        });
    }

    // Cargar los datos que se muestran en pantalla
    private void loadInitialData() {
        // Por ahora datos fake, después vendrán de la base de datos
        tvWelcome.setText("¡Hola, Manager!");
        tvActiveLeagues.setText("3");
        tvIncompleteLineups.setText("1"); // Alineaciones que necesitan atención

        updateNavigationHeader();
    }

    // Actualizar los datos del header del menú lateral
    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView userName = headerView.findViewById(R.id.tv_user_name);
        TextView userMoney = headerView.findViewById(R.id.tv_user_money);

        userName.setText("Manager");
        userMoney.setText("Dinero disponible");
    }

    // Para mostrar mensajes rápidos (debug)
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }
}