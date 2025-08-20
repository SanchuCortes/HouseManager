package com.example.housemanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.housemanager.repository.FootballRepository;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    // Componentes del drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    // Repository para datos
    private FootballRepository repository;

    // Botones del dashboard
    private Button btnViewLeagues;
    private Button btnCreateLeague;
    private Button btnJoinLeague;

    // Views opcionales que pueden no existir
    private ProgressBar progressSync;
    private TextView tvSyncStatus;
    private TextView tvActiveLeagues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Iniciando MainActivity");
        setContentView(R.layout.activity_main);

        repository = FootballRepository.getInstance(this);

        initViews();
        setupToolbarAndDrawer();
        setupButtons();
        initializeFootballData();

        Log.d(TAG, "MainActivity configurada correctamente");
    }

    private void initViews() {
        // Buscar los botones principales
        btnViewLeagues = findViewById(R.id.btn_view_leagues);
        btnCreateLeague = findViewById(R.id.btn_create_league);
        btnJoinLeague = findViewById(R.id.btn_join_league);

        // Estadísticas del dashboard
        tvActiveLeagues = findViewById(R.id.tv_active_leagues);

        // No necesitamos elementos de progreso
        progressSync = null;
        tvSyncStatus = null;
    }

    private void setupToolbarAndDrawer() {
        Log.d(TAG, "Configurando toolbar y drawer");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupButtons() {
        Log.d(TAG, "Configurando botones");

        // Ver ligas del usuario
        btnViewLeagues.setOnClickListener(v -> {
            Log.d(TAG, "Usuario quiere ver sus ligas");
            startActivity(new Intent(this, LeaguesActivity.class));
        });

        // Crear nueva liga
        btnCreateLeague.setOnClickListener(v -> {
            Log.d(TAG, "Usuario quiere crear una liga");
            startActivity(new Intent(this, CreateLeagueActivity.class));
        });

        // Unirse a liga (pendiente)
        btnJoinLeague.setOnClickListener(v -> {
            Log.d(TAG, "Usuario quiere unirse a una liga");
            Toast.makeText(this, "Función próximamente disponible", Toast.LENGTH_SHORT).show();
        });

        // Accesos directos para pruebas
        setupTestShortcuts();
    }

    private void setupTestShortcuts() {
        // Acceso directo al mercado
        TextView tvWelcome = findViewById(R.id.tv_welcome);
        if (tvWelcome != null) {
            tvWelcome.setOnLongClickListener(v -> {
                Log.d(TAG, "Acceso directo al mercado");
                // Verificar que hay datos antes de ir
                repository.getAvailablePlayersCount().observe(this, count -> {
                    if (count != null && count > 0) {
                        Log.d(TAG, "Hay " + count + " jugadores, abriendo mercado");
                        startActivity(new Intent(this, TransferMarketActivity.class));
                    } else {
                        Log.d(TAG, "Aún no hay jugadores cargados");
                        Toast.makeText(this, "Datos aún cargando, prueba en unos segundos", Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            });
        }

        // Acceso directo a mi equipo
        if (tvActiveLeagues != null) {
            tvActiveLeagues.setOnLongClickListener(v -> {
                Log.d(TAG, "Acceso directo a mi equipo");
                Intent intent = new Intent(this, MyTeamActivity.class);
                intent.putExtra(MyTeamActivity.EXTRA_TEAM_ID, 1);
                intent.putExtra(MyTeamActivity.EXTRA_LEAGUE_NAME, "Liga de Prueba");
                startActivity(intent);
                return true;
            });
        }
    }

    private void initializeFootballData() {
        Log.d(TAG, "Iniciando carga de datos en background");

        // Observer del estado de sync
        repository.getIsSyncing().observe(this, isSyncing -> {
            if (isSyncing != null) {
                if (isSyncing) {
                    Log.d(TAG, "Cargando datos de LaLiga");
                } else {
                    Log.d(TAG, "Datos listos");
                }
            }
        });

        // Observer de mensajes de estado
        repository.getSyncStatus().observe(this, status -> {
            if (status != null && !status.isEmpty()) {
                Log.d(TAG, "Estado: " + status);
            }
        });

        // Observer del número de jugadores
        repository.getAvailablePlayersCount().observe(this, count -> {
            if (count != null) {
                Log.d(TAG, "Jugadores disponibles: " + count);
            }
        });

        // Empezar la sincronización en background
        repository.syncLaLigaTeams(new FootballRepository.SyncCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Log.d(TAG, "Sincronización completada");
                });
            }

            @Override
            public void onError(Throwable t) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error en sincronización", t);
                });
            }

            @Override
            public void onProgress(String message, int current, int total) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Progreso: " + message + " (" + current + "/" + total + ")");
                });
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_leagues) {
            Log.d(TAG, "Ir a mis ligas desde drawer");
            startActivity(new Intent(this, LeaguesActivity.class));

        } else if (id == R.id.nav_create_league) {
            Log.d(TAG, "Crear liga desde drawer");
            startActivity(new Intent(this, CreateLeagueActivity.class));

        } else if (id == R.id.nav_statistics) {
            Log.d(TAG, "Estadísticas no implementadas aún");
            Toast.makeText(this, "Estadísticas próximamente", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_settings) {
            Log.d(TAG, "Configuración no implementada aún");
            Toast.makeText(this, "Configuración próximamente", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_help) {
            Log.d(TAG, "Mostrar ayuda");
            showHelpDialog();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showHelpDialog() {
        Log.d(TAG, "Mostrando diálogo de ayuda");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("HouseManager - Ayuda")
                .setMessage(
                        "Cómo usar la app:\n\n" +
                                "1. Los datos se cargan automáticamente\n" +
                                "2. Crea una liga o únete a una existente\n" +
                                "3. Ve al mercado y contrata jugadores\n" +
                                "4. Gestiona tu equipo y asigna capitán\n" +
                                "5. Compite cada jornada\n\n" +

                                "Trucos:\n" +
                                "- Mantén pulsado 'Hola Manager' para ir al mercado\n" +
                                "- Mantén pulsado las estadísticas para ir a Mi Equipo\n\n" +

                                "Datos:\n" +
                                "- 20 equipos reales de LaLiga\n" +
                                "- Más de 400 jugadores\n" +
                                "- Precios y puntos realistas\n\n" +

                                "¡Disfruta tu campo en casa!"
                )
                .setPositiveButton("Vale", (dialog, which) -> {
                    Log.d(TAG, "Usuario cerró la ayuda");
                })
                .setNeutralButton("Recargar datos", (dialog, which) -> {
                    Log.d(TAG, "Usuario pidió recargar datos");
                    reloadData();
                })
                .show();
    }

    private void reloadData() {
        Log.d(TAG, "Recargando datos");
        Toast.makeText(this, "Recargando datos...", Toast.LENGTH_SHORT).show();
        repository.syncLaLigaTeams(null);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            Log.d(TAG, "Cerrando drawer");
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Log.d(TAG, "Saliendo de la app");
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity visible otra vez");
        // Actualizar estadísticas si es necesario
        repository.getAvailablePlayersCount().observe(this, count -> {
            if (count != null) {
                Log.d(TAG, "Stats actualizadas: " + count + " jugadores");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity destruyéndose");
    }
}