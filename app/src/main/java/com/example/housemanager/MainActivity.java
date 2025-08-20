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

/**
 * Actividad principal de la aplicación.
 * Maneja la navegación por drawer y la sincronización inicial de datos.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    // Componentes de navegación
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    // Repositorio para manejar datos
    private FootballRepository repository;

    // Referencias a botones del dashboard
    private Button btnViewLeagues;
    private Button btnCreateLeague;
    private Button btnJoinLeague;

    // Elementos para mostrar progreso de sincronización
    private ProgressBar progressSync;
    private TextView tvSyncStatus;
    private TextView tvActiveLeagues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Iniciando MainActivity");
        setContentView(R.layout.activity_main);

        // Obtener instancia del repositorio
        repository = FootballRepository.getInstance(this);

        // Configurar toda la interfaz
        initViews();
        setupToolbarAndDrawer();
        setupButtons();

        // Empezar la carga de datos
        initializeAppData();

        Log.d(TAG, "MainActivity configurada correctamente");
    }

    /**
     * Inicializa todas las referencias a views
     */
    private void initViews() {
        // Botones principales del dashboard
        btnViewLeagues = findViewById(R.id.btn_view_leagues);
        btnCreateLeague = findViewById(R.id.btn_create_league);
        btnJoinLeague = findViewById(R.id.btn_join_league);

        // Elementos de estadísticas
        tvActiveLeagues = findViewById(R.id.tv_active_leagues);

        // Elementos de progreso (pueden no existir en todos los layouts)
        progressSync = findViewById(R.id.progress_sync);
        tvSyncStatus = findViewById(R.id.tv_sync_status);
    }

    /**
     * Configura la toolbar y el navigation drawer
     */
    private void setupToolbarAndDrawer() {
        Log.d(TAG, "Configurando toolbar y navigation drawer");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // Configurar el toggle para abrir/cerrar el drawer
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Configura los listeners de todos los botones
     */
    private void setupButtons() {
        Log.d(TAG, "Configurando listeners de botones");

        // Botón para ver las ligas del usuario
        btnViewLeagues.setOnClickListener(v -> {
            Log.d(TAG, "Usuario presionó Ver mis ligas");
            startActivity(new Intent(this, LeaguesActivity.class));
        });

        // Botón para crear una nueva liga
        btnCreateLeague.setOnClickListener(v -> {
            Log.d(TAG, "Usuario presionó Crear nueva liga");
            startActivity(new Intent(this, CreateLeagueActivity.class));
        });

        // Botón para unirse a una liga (por implementar)
        btnJoinLeague.setOnClickListener(v -> {
            Log.d(TAG, "Usuario presionó Unirse a liga");
            Toast.makeText(this, "Unirse a liga - Esta función estará disponible pronto", Toast.LENGTH_SHORT).show();
        });

        // Configurar accesos rápidos para testing
        setupTestingShortcuts();
    }

    /**
     * Configura accesos directos para facilitar el testing durante desarrollo
     */
    private void setupTestingShortcuts() {
        // Acceso directo al mercado manteniendo pulsado el título de bienvenida
        TextView tvWelcome = findViewById(R.id.tv_welcome);
        if (tvWelcome != null) {
            tvWelcome.setOnLongClickListener(v -> {
                Log.d(TAG, "Acceso directo al mercado activado");

                // Verificar que tengamos datos antes de ir al mercado
                repository.getAvailablePlayersCount().observe(this, count -> {
                    if (count != null && count > 0) {
                        Log.d(TAG, "Navegando al mercado con " + count + " jugadores disponibles");
                        startActivity(new Intent(this, TransferMarketActivity.class));
                    } else {
                        Log.d(TAG, "No hay jugadores disponibles todavía");
                        Toast.makeText(this,
                                "Esperando que se carguen los datos... Prueba en unos segundos",
                                Toast.LENGTH_LONG).show();
                    }
                });
                return true;
            });
        }

        // Acceso directo a Mi Equipo manteniendo pulsado las estadísticas
        if (tvActiveLeagues != null) {
            tvActiveLeagues.setOnLongClickListener(v -> {
                Log.d(TAG, "Acceso directo a Mi Equipo activado");
                Intent intent = new Intent(this, MyTeamActivity.class);
                intent.putExtra(MyTeamActivity.EXTRA_TEAM_ID, 1);
                intent.putExtra(MyTeamActivity.EXTRA_LEAGUE_NAME, "Liga de Prueba");
                startActivity(intent);
                return true;
            });
        }
    }

    /**
     * Inicia la carga y sincronización de datos de la aplicación
     */
    private void initializeAppData() {
        Log.d(TAG, "Iniciando carga de datos de la aplicación");

        // Observar el estado de sincronización
        repository.getIsSyncing().observe(this, isSyncing -> {
            if (isSyncing != null) {
                updateSyncUI(isSyncing, null);

                if (isSyncing) {
                    Log.d(TAG, "Sincronización en progreso");
                } else {
                    Log.d(TAG, "Sincronización terminada");
                }
            }
        });

        // Observar mensajes de estado detallado
        repository.getSyncStatus().observe(this, status -> {
            if (status != null && !status.isEmpty()) {
                updateSyncUI(null, status);
                Log.d(TAG, "Estado de sincronización: " + status);
            }
        });

        // Observar el número de jugadores disponibles
        repository.getAvailablePlayersCount().observe(this, count -> {
            if (count != null) {
                updatePlayersCount(count);
                Log.d(TAG, "Jugadores disponibles en el mercado: " + count);
            }
        });

        // Empezar la sincronización
        repository.syncLaLigaTeams(new FootballRepository.SyncCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Log.d(TAG, "Sincronización completada exitosamente");
                    showSyncCompletedMessage();
                });
            }

            @Override
            public void onError(Throwable t) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error durante la sincronización", t);
                    showSyncErrorMessage(t.getMessage());
                });
            }

            @Override
            public void onProgress(String message, int current, int total) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Progreso de sincronización: " + message + " (" + current + "/" + total + ")");
                    updateSyncProgress(message, current, total);
                });
            }
        });
    }

    /**
     * Actualiza los elementos de la UI relacionados con la sincronización
     */
    private void updateSyncUI(Boolean isSyncing, String status) {
        // Mostrar u ocultar la barra de progreso
        if (progressSync != null) {
            progressSync.setVisibility(
                    (isSyncing != null && isSyncing) ? ProgressBar.VISIBLE : ProgressBar.GONE
            );
        }

        // Actualizar el texto de estado
        if (tvSyncStatus != null && status != null) {
            tvSyncStatus.setText(status);
        }
    }

    /**
     * Actualiza el progreso de sincronización con información específica
     */
    private void updateSyncProgress(String message, int current, int total) {
        if (tvSyncStatus != null) {
            String progressText = message + " (" + current + "/" + total + ")";
            tvSyncStatus.setText(progressText);
        }

        if (progressSync != null) {
            progressSync.setMax(total);
            progressSync.setProgress(current);
        }
    }

    /**
     * Actualiza el contador de jugadores disponibles en la UI
     */
    private void updatePlayersCount(int count) {
        Log.d(TAG, "Actualizando contador de jugadores disponibles: " + count);
        // Aquí puedes actualizar algún TextView del dashboard si existe
        // Por ejemplo mostrar el número en las estadísticas
    }

    /**
     * Muestra un mensaje cuando la sincronización se completa exitosamente
     */
    private void showSyncCompletedMessage() {
        repository.getAvailablePlayersCount().observe(this, count -> {
            if (count != null && count > 0) {
                String message = "Datos cargados correctamente!\n\n" +
                        "Equipos: 20 equipos de LaLiga\n" +
                        "Jugadores: " + count + " disponibles\n\n" +
                        "Tips:\n" +
                        "- Mantén pulsado 'Hola Manager' para ir al mercado\n" +
                        "- Mantén pulsado el número de ligas para Mi Equipo\n\n" +
                        "Todo listo para jugar!";

                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                Log.d(TAG, "Mensaje de sincronización exitosa mostrado al usuario");
            }
        });
    }

    /**
     * Muestra un mensaje cuando hay un error en la sincronización
     */
    private void showSyncErrorMessage(String error) {
        String message = "Error cargando datos: " + error +
                "\n\nLa aplicación funcionará con datos limitados.\n" +
                "Revisa tu conexión a internet e inténtalo más tarde.";

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.d(TAG, "Mensaje de error mostrado al usuario");
    }

    /**
     * Maneja las selecciones del navigation drawer
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_leagues) {
            Log.d(TAG, "Navegando a Mis Ligas desde el drawer");
            startActivity(new Intent(this, LeaguesActivity.class));

        } else if (id == R.id.nav_create_league) {
            Log.d(TAG, "Navegando a Crear Liga desde el drawer");
            startActivity(new Intent(this, CreateLeagueActivity.class));

        } else if (id == R.id.nav_statistics) {
            Log.d(TAG, "Usuario seleccionó Estadísticas (no implementado)");
            Toast.makeText(this, "Estadísticas - Esta función estará disponible pronto", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_settings) {
            Log.d(TAG, "Usuario seleccionó Ajustes (no implementado)");
            Toast.makeText(this, "Ajustes - Esta función estará disponible pronto", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_help) {
            Log.d(TAG, "Usuario seleccionó Ayuda");
            showHelpDialog();
        }

        // Cerrar el drawer después de la selección
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Muestra un diálogo de ayuda con información sobre cómo usar la aplicación
     */
    private void showHelpDialog() {
        Log.d(TAG, "Mostrando diálogo de ayuda");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("HouseManager - Guía de Usuario")
                .setMessage(
                        "Cómo empezar:\n\n" +
                                "1. Los datos se cargan automáticamente al abrir la aplicación\n" +
                                "2. Crea una liga o únete a una existente\n" +
                                "3. Ve al mercado de fichajes y contrata jugadores\n" +
                                "4. Asigna tu capitán para que puntúe el doble\n" +
                                "5. Compite cada jornada de LaLiga\n\n" +

                                "Accesos rápidos:\n" +
                                "- Mantén pulsado 'Hola Manager' para ir al mercado\n" +
                                "- Mantén pulsado el número de ligas para ir a Mi Equipo\n\n" +

                                "Información de datos:\n" +
                                "- 20 equipos reales de LaLiga\n" +
                                "- Más de 400 jugadores disponibles\n" +
                                "- Precios y puntuaciones realistas\n" +
                                "- Posiciones en español\n\n" +

                                "Disfruta de tu campo en casa!"
                )
                .setPositiveButton("Entendido", (dialog, which) -> {
                    Log.d(TAG, "Usuario cerró el diálogo de ayuda");
                })
                .setNeutralButton("Recargar datos", (dialog, which) -> {
                    Log.d(TAG, "Usuario solicitó recarga de datos");
                    forceSyncData();
                })
                .show();
    }

    /**
     * Fuerza una nueva sincronización de datos
     */
    private void forceSyncData() {
        Log.d(TAG, "Forzando nueva sincronización de datos");
        Toast.makeText(this, "Iniciando nueva sincronización de datos...", Toast.LENGTH_SHORT).show();

        // Llamar a syncLaLigaTeams sin callback específico
        repository.syncLaLigaTeams(null);
    }

    /**
     * Maneja el botón de atrás cuando el drawer está abierto
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            Log.d(TAG, "Cerrando navigation drawer");
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Log.d(TAG, "Usuario presionó botón atrás, cerrando aplicación");
            super.onBackPressed();
        }
    }

    /**
     * Se ejecuta cuando la actividad vuelve a ser visible
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity visible de nuevo");
        updateDashboardStats();
    }

    /**
     * Actualiza las estadísticas mostradas en el dashboard
     */
    private void updateDashboardStats() {
        // Actualizar estadísticas del dashboard con datos frescos
        repository.getAvailablePlayersCount().observe(this, count -> {
            if (count != null) {
                Log.d(TAG, "Estadísticas del dashboard actualizadas: " + count + " jugadores");
                // Aquí podrías actualizar TextViews específicos del dashboard
            }
        });
    }

    /**
     * Se ejecuta cuando la actividad se destruye
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity destruida");
    }
}