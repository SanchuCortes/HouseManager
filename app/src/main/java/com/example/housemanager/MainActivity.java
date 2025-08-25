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
    private TextView tvIncompleteLineups;
    private android.widget.LinearLayout llMatchesContainer;

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
        
        // Manejar botón atrás con OnBackPressedDispatcher (sin usar API deprecada)
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                try {
                    if (drawerLayout != null && drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
                        Log.d(TAG, "Cerrando navigation drawer");
                        drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
                        return;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error al manejar back con drawer", e);
                }
                Log.d(TAG, "Usuario presionó botón atrás, cerrando aplicación");
                finish();
            }
        });
        
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
        tvIncompleteLineups = findViewById(R.id.tv_incomplete_lineups);

        // Elementos de progreso (pueden no existir en todos los layouts)
        progressSync = findViewById(R.id.progress_sync);
        tvSyncStatus = findViewById(R.id.tv_sync_status);

        // Contenedor de próximos partidos
        llMatchesContainer = findViewById(R.id.ll_matches_container);
    }

    /**
     * Configura la toolbar y el navigation drawer
     */
    private void setupToolbarAndDrawer() {
        Log.d(TAG, "Configurando toolbar y navigation drawer");

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            Log.w(TAG, "Toolbar no encontrada en el layout. Saltando configuración de AppBar/Drawer.");
            return;
        }
        try {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                // Quitar el título de texto de la app en la esquina izquierda
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error al configurar la toolbar", e);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        if (drawerLayout == null) {
            Log.w(TAG, "DrawerLayout no encontrado. La navegación lateral estará deshabilitada.");
            return;
        }
        // Configurar el toggle para abrir/cerrar el drawer
        try {
            toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        } catch (Exception e) {
            Log.w(TAG, "Error configurando ActionBarDrawerToggle", e);
        }
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        } else {
            Log.w(TAG, "NavigationView no encontrado. Menú lateral no disponible.");
        }
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
            com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Unirse a liga - Esta función estará disponible pronto", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
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
                        com.google.android.material.snackbar.Snackbar.make(v,
                                "Esperando que se carguen los datos... Prueba en unos segundos",
                                com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
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

        // Observar métricas del dashboard
        observeDashboardMetrics();

        // Preparar sección de próximos partidos (sin bloquear la sincronización principal)
        setupUpcomingMatchesSection();

        // Forzar también la sync de próximos partidos al iniciar (asegura datos frescos)
        repository.syncUpcomingMatches(null);

        // Activar acción de depuración oculta: long-press en la lista de partidos para recalcular puntos
        if (llMatchesContainer != null) {
            llMatchesContainer.setOnLongClickListener(v -> {
                repository.getCurrentMatchday(new FootballRepository.MatchdayCallback() {
                    @Override public void onResult(int matchday) {
                        repository.recalcAllFinishedPoints(new FootballRepository.SyncCallback() {
                            @Override public void onSuccess() {
                                com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Puntos recalculados para partidos FINISHED", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                            }
                            @Override public void onError(Throwable t) {
                                com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Error recálculo: " + t.getMessage(), com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                            }
                            @Override public void onProgress(String message, int current, int total) {}
                        });
                    }
                    @Override public void onError(Throwable t) {
                        com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "No se pudo obtener jornada actual", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                    }
                });
                return true;
            });
        }

        // Empezar la sincronización
        repository.syncLaLigaTeams(new FootballRepository.SyncCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Log.d(TAG, "Sincronización completada exitosamente");
                    showSyncCompletedMessage();
                });
                // Tras sincronizar equipos/jugadores, sincronizar TODAS las jornadas hasta la actual y recalcular puntos
                repository.syncAndRecalculatePointsForAllMatchdaysUpToCurrent(null);
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

                Log.d(TAG, message);
                // Mensaje informativo no crítico: registro en logs en lugar de Toast
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

        Log.e(TAG, message);
        // Error no crítico: registrar en logs (sin Toast)
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
            com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Estadísticas - Esta función estará disponible pronto", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();

        } else if (id == R.id.nav_settings) {
            Log.d(TAG, "Usuario seleccionó Ajustes (no implementado)");
            com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Ajustes - Esta función estará disponible pronto", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();

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
        com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Iniciando nueva sincronización de datos...", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();

        // Llamar a syncLaLigaTeams sin callback específico
        repository.syncLaLigaTeams(null);
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
        // Mantener otras métricas si fuese necesario
        repository.getAvailablePlayersCount().observe(this, count -> {
            if (count != null) {
                Log.d(TAG, "Estadísticas del mercado: " + count + " jugadores disponibles");
            }
        });
    }

    /** Observa y pinta métricas reales del dashboard (ligas activas y alineación) */
    private void observeDashboardMetrics() {
        // Ligas activas desde Room
        repository.getActiveLeaguesCount().observe(this, count -> {
            if (tvActiveLeagues != null && count != null) {
                tvActiveLeagues.setText(String.valueOf(count));
            }
        });

        // Alineación incompleta por liga: mostrar cuántas ligas del usuario no pueden formar 11 válido
        repository.getIncompleteLineupsLeaguesCount(1L).observe(this, count -> {
            if (tvIncompleteLineups != null && count != null) {
                tvIncompleteLineups.setText(String.valueOf(count));
            }
        });
    }

    /** Configura/observa la sección de Próximos Partidos en el último card */
    private void setupUpcomingMatchesSection() {
        // Usar un ViewModel especializado para componer la lista con fallback sin observeForever
        com.example.housemanager.viewmodel.HomeViewModel homeVm = new androidx.lifecycle.ViewModelProvider(this).get(com.example.housemanager.viewmodel.HomeViewModel.class);
        homeVm.getHomeMatches().observe(this, this::renderMatchesList);
        // Lanzar un refresh no bloqueante para asegurar datos locales
        homeVm.refresh();
    }

    private void renderMatchesList(java.util.List<com.example.housemanager.database.entities.MatchEntity> matches) {
        if (llMatchesContainer == null) return;
        llMatchesContainer.removeAllViews();
        if (matches == null || matches.isEmpty()) {
            // Placeholder
            TextView tv = new TextView(this);
            tv.setText("No hay partidos próximos");
            tv.setTextAppearance(this, R.style.TextSecondary);
            llMatchesContainer.addView(tv);
            return;
        }
        // Limitar a 10
        int limit = Math.min(10, matches.size());
        // Formateador de fecha/hora local con día de la semana abreviado en español
        java.util.Locale localeEs = new java.util.Locale("es", "ES");
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("EEE dd/MM HH:mm", localeEs);
        // Forzar zona horaria Europe/Madrid (horario de España peninsular)
        java.time.ZoneId zone = java.time.ZoneId.of("Europe/Madrid");
        for (int i = 0; i < limit; i++) {
            com.example.housemanager.database.entities.MatchEntity m = matches.get(i);
            android.widget.LinearLayout row = new android.widget.LinearLayout(this);
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            int padV = (int) (8 * getResources().getDisplayMetrics().density);
            row.setPadding(0, padV / 2, 0, padV / 2);

            // Construimos el texto completo en una sola línea: "EEE dd/MM HH:mm — Local vs Visitante" o con marcador
            TextView tvDesc = new TextView(this);
            tvDesc.setTextAppearance(this, R.style.TextBody);
            tvDesc.setTextSize(14);

            Long ms = m.getUtcDateMillis();
            String when = "";
            if (ms != null && ms > 0) {
                try {
                    java.time.ZonedDateTime zdt = java.time.Instant.ofEpochMilli(ms).atZone(zone);
                    when = dtf.format(zdt);
                } catch (Exception e) {
                    android.util.Log.w("MatchesAdapter", "Error formateando fecha: " + ms, e);
                    when = ""; // sin fecha si falla
                }
            }

            String home = (m.getHomeTeamName() != null ? m.getHomeTeamName() : "");
            String away = (m.getAwayTeamName() != null ? m.getAwayTeamName() : "");
            Integer hs = m.getHomeScore();
            Integer as = m.getAwayScore();
            String status = m.getStatus();

            String text;
            boolean finished = status != null && status.equalsIgnoreCase("FINISHED");
            if (finished && (hs != null && as != null)) {
                // Partido finalizado: incluir marcador
                text = (when.isEmpty() ? "" : when + " — ") + home + " " + hs + "–" + as + " " + away;
            } else {
                // Próximo o no finalizado: sin marcador
                text = (when.isEmpty() ? "" : when + " — ") + home + " vs " + away;
            }
            tvDesc.setText(text);
            android.widget.LinearLayout.LayoutParams lpDesc = new android.widget.LinearLayout.LayoutParams(
                    0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(tvDesc, lpDesc);

            llMatchesContainer.addView(row);
        }
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