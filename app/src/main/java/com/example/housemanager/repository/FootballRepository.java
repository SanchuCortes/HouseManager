package com.example.housemanager.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.housemanager.ApiClient;
import com.example.housemanager.api.FootballApiService;
import com.example.housemanager.api.models.PlayerAPI;
import com.example.housemanager.api.models.TeamAPI;
import com.example.housemanager.api.models.TeamsResponse;
import com.example.housemanager.database.HouseManagerDatabase;
import com.example.housemanager.database.dao.PlayerDao;
import com.example.housemanager.database.dao.TeamDao;
import com.example.housemanager.database.entities.PlayerEntity;
import com.example.housemanager.database.entities.TeamEntity;
import com.example.housemanager.market.Player;
import com.example.housemanager.market.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio principal para manejar datos de football.
 * Se encarga de sincronizar datos de la API y mantener cache local.
 */
public class FootballRepository {

    private static final String TAG = "FootballRepository";

    // Configuración para el cache de sincronización
    private static final String PREFS_NAME = "football_sync_prefs";
    private static final String PREF_LAST_SYNC = "last_full_sync";
    private static final String PREF_TEAMS_SYNCED = "teams_synced";
    private static final String PREF_PLAYERS_SYNCED = "players_synced";
    private static final long SYNC_INTERVAL_DAYS = 7; // Resincronizar cada semana

    // Callback para informar del progreso de sincronización
    public interface SyncCallback {
        void onSuccess();
        void onError(Throwable t);
        void onProgress(String message, int current, int total);
    }

    private static FootballRepository instance;

    // DAOs para acceso a base de datos
    private final PlayerDao playerDao;
    private final TeamDao teamDao;
    private final FootballApiService apiService;
    private final ExecutorService executor;
    private final SharedPreferences syncPrefs;

    // LiveData para comunicar el estado a la UI
    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);
    private final MutableLiveData<String> syncStatus = new MutableLiveData<>("");

    private FootballRepository(Context context) {
        Log.d(TAG, "Inicializando FootballRepository");

        // Configurar base de datos Room
        HouseManagerDatabase db = HouseManagerDatabase.getInstance(context);
        playerDao = db.playerDao();
        teamDao = db.teamDao();

        // Configurar cliente API
        apiService = ApiClient.getClient().create(FootballApiService.class);
        executor = Executors.newFixedThreadPool(3);

        // Configurar SharedPreferences para control de cache
        syncPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        Log.d(TAG, "FootballRepository inicializado correctamente");
    }

    public static synchronized FootballRepository getInstance(Context context) {
        if (instance == null) {
            instance = new FootballRepository(context.getApplicationContext());
        }
        return instance;
    }

    // ====== MÉTODOS PÚBLICOS PARA OBTENER DATOS ======

    /**
     * Obtiene todos los equipos como objetos Team para el mercado
     */
    public LiveData<List<Team>> getAllTeams() {
        return Transformations.map(teamDao.getAllTeamEntities(), this::convertTeamEntitiesToMarketTeams);
    }

    /**
     * Obtiene todos los jugadores como objetos Player para el mercado
     */
    public LiveData<List<Player>> getAllPlayers() {
        return Transformations.map(playerDao.getAllPlayerEntities(), this::convertPlayerEntitiesToMarketPlayers);
    }

    /**
     * Obtiene la plantilla de un equipo específico como PlayerAPI para MyTeam
     */
    public LiveData<List<PlayerAPI>> getSquadApiByTeam(int teamId) {
        return playerDao.getSquadApiByTeam(teamId);
    }

    /**
     * Busca jugadores por nombre o equipo
     */
    public LiveData<List<Player>> searchPlayers(String searchTerm) {
        return Transformations.map(
                playerDao.searchAvailablePlayers(searchTerm),
                this::convertPlayerEntitiesToMarketPlayers
        );
    }

    /**
     * Obtiene el número de jugadores disponibles en el mercado
     */
    public LiveData<Integer> getAvailablePlayersCount() {
        return playerDao.getAvailablePlayersCount();
    }

    // ====== SINCRONIZACIÓN PRINCIPAL ======

    /**
     * Método principal de sincronización. Decide automáticamente si usar cache o API.
     */
    public void syncLaLigaTeams(@Nullable SyncCallback callback) {
        Log.d(TAG, "Iniciando proceso de sincronización de LaLiga");
        isSyncing.setValue(true);
        syncStatus.setValue("Verificando datos existentes...");

        executor.execute(() -> {
            try {
                // Primero decidimos qué estrategia de sincronización usar
                SyncDecision decision = decideSyncStrategy();
                Log.d(TAG, "Estrategia seleccionada: " + decision.strategy + " - " + decision.reason);

                switch (decision.strategy) {
                    case USE_CACHE:
                        useCachedData(callback);
                        break;

                    case FULL_SYNC:
                        performFullSync(callback);
                        break;

                    case PARTIAL_SYNC:
                        performPartialSync(decision, callback);
                        break;

                    case MOCK_FALLBACK:
                        generateMockDataAsFallback(callback);
                        break;
                }

            } catch (Exception e) {
                Log.e(TAG, "Error durante la sincronización", e);
                handleSyncError(e, callback);
            }
        });
    }

    /**
     * Analiza el estado actual y decide qué tipo de sincronización hacer
     */
    private SyncDecision decideSyncStrategy() {
        int existingTeams = teamDao.getTeamsCountSync();
        int existingPlayers = playerDao.getPlayersCountSync();
        long lastSync = syncPrefs.getLong(PREF_LAST_SYNC, 0);
        long daysSinceLastSync = (System.currentTimeMillis() - lastSync) / (1000 * 60 * 60 * 24);

        Log.d(TAG, "Estado actual - Equipos: " + existingTeams + ", Jugadores: " + existingPlayers);
        Log.d(TAG, "Última sincronización hace " + daysSinceLastSync + " días");

        SyncDecision decision = new SyncDecision();

        if (existingTeams == 0 && existingPlayers == 0) {
            // Primera vez que se abre la app
            decision.strategy = SyncStrategy.FULL_SYNC;
            decision.reason = "Primera sincronización completa";
            Log.d(TAG, "Es la primera vez, necesitamos sincronización completa");

        } else if (daysSinceLastSync >= SYNC_INTERVAL_DAYS) {
            // Los datos son muy antiguos
            decision.strategy = SyncStrategy.FULL_SYNC;
            decision.reason = "Datos antiguos, necesitan actualización";
            Log.d(TAG, "Los datos son antiguos, resincronizando");

        } else if (existingTeams > 0 && existingPlayers < (existingTeams * 15)) {
            // Tenemos equipos pero faltan muchos jugadores
            decision.strategy = SyncStrategy.PARTIAL_SYNC;
            decision.reason = "Faltan jugadores, completando datos";
            decision.missingPlayers = true;
            Log.d(TAG, "Los equipos están pero faltan jugadores");

        } else if (existingTeams > 0 && existingPlayers > 0) {
            // Tenemos datos recientes y completos
            decision.strategy = SyncStrategy.USE_CACHE;
            decision.reason = "Datos válidos en cache";
            Log.d(TAG, "Los datos en cache son válidos, no necesitamos sincronizar");

        } else {
            // Algo raro pasó, mejor usar datos mock
            decision.strategy = SyncStrategy.MOCK_FALLBACK;
            decision.reason = "Estado inconsistente, usando datos mock";
            Log.w(TAG, "Estado de datos inconsistente, usando fallback");
        }

        return decision;
    }

    /**
     * Usa los datos que ya tenemos en cache
     */
    private void useCachedData(SyncCallback callback) {
        Log.d(TAG, "Usando datos del cache local");
        syncStatus.postValue("Cargando datos desde cache...");

        // Pausa pequeña para que el usuario vea el mensaje
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        syncStatus.postValue("Datos cargados correctamente");
        isSyncing.postValue(false);

        if (callback != null) {
            callback.onSuccess();
        }

        Log.d(TAG, "Cache cargado exitosamente");
    }

    /**
     * Realiza una sincronización completa desde la API
     */
    private void performFullSync(SyncCallback callback) {
        Log.d(TAG, "Iniciando sincronización completa desde la API");
        syncStatus.postValue("Conectando con football-data.org...");

        // Primero obtenemos la lista de equipos
        apiService.getLaLigaTeams().enqueue(new Callback<TeamsResponse>() {
            @Override
            public void onResponse(Call<TeamsResponse> call, Response<TeamsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TeamAPI> apiTeams = response.body().getTeams();
                    Log.d(TAG, "Recibidos " + apiTeams.size() + " equipos de la API");

                    executor.execute(() -> {
                        try {
                            // Guardar los equipos en la base de datos
                            syncStatus.postValue("Guardando equipos...");
                            saveTeamsToDatabase(apiTeams);

                            // Ahora sincronizar los jugadores de cada equipo
                            syncStatus.postValue("Obteniendo jugadores...");
                            syncAllPlayersFromAPI(apiTeams, callback);

                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando equipos", e);
                            handleSyncError(e, callback);
                        }
                    });
                } else {
                    Log.e(TAG, "Error en la API de equipos, código: " + response.code());
                    // Si falla la API, usamos datos mock
                    generateMockDataAsFallback(callback);
                }
            }

            @Override
            public void onFailure(Call<TeamsResponse> call, Throwable t) {
                Log.e(TAG, "Fallo de conexión obteniendo equipos", t);
                generateMockDataAsFallback(callback);
            }
        });
    }

    /**
     * Obtiene los jugadores de todos los equipos desde la API
     */
    private void syncAllPlayersFromAPI(List<TeamAPI> teams, SyncCallback callback) {
        Log.d(TAG, "Empezando a sincronizar jugadores de " + teams.size() + " equipos");

        executor.execute(() -> {
            List<PlayerEntity> allPlayers = new ArrayList<>();
            int successfulTeams = 0;

            for (int i = 0; i < teams.size(); i++) {
                TeamAPI team = teams.get(i);
                final int teamIndex = i + 1;
                final int totalTeams = teams.size();

                // Esperar entre llamadas para no saturar la API
                if (i > 0) {
                    Log.d(TAG, "Esperando 7 segundos antes de la siguiente llamada...");
                    try {
                        Thread.sleep(7000); // 7 segundos entre llamadas
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Log.w(TAG, "Sincronización interrumpida por el usuario");
                        break;
                    }
                }

                // Actualizar el estado en la UI
                String statusMessage = "Obteniendo jugadores de " + team.getName() + " (" + teamIndex + "/" + totalTeams + ")";
                syncStatus.postValue(statusMessage);
                Log.d(TAG, statusMessage);

                // Informar del progreso al callback
                if (callback != null) {
                    runOnMainThread(() -> callback.onProgress("Sincronizando " + team.getName(), teamIndex, totalTeams));
                }

                try {
                    // Hacer la llamada a la API para obtener los jugadores del equipo
                    Response<TeamAPI> response = apiService.getTeamDetails(team.getId()).execute();

                    if (response.isSuccessful() && response.body() != null) {
                        TeamAPI teamWithPlayers = response.body();

                        if (teamWithPlayers.getSquad() != null && !teamWithPlayers.getSquad().isEmpty()) {
                            // Convertir los jugadores de la API a entidades de base de datos
                            List<PlayerEntity> teamPlayers = convertApiPlayersToEntities(
                                    teamWithPlayers.getSquad(), team);
                            allPlayers.addAll(teamPlayers);
                            successfulTeams++;

                            Log.d(TAG, "Equipo " + team.getName() + " sincronizado: " + teamPlayers.size() + " jugadores");
                        } else {
                            Log.w(TAG, "El equipo " + team.getName() + " no tiene jugadores en la respuesta");
                        }
                    } else {
                        Log.e(TAG, "Error obteniendo jugadores de " + team.getName() + ", código: " + response.code());
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error sincronizando equipo " + team.getName(), e);
                }
            }

            Log.d(TAG, "Sincronización terminada: " + successfulTeams + "/" + teams.size() + " equipos exitosos");
            Log.d(TAG, "Total de jugadores obtenidos: " + allPlayers.size());

            // Finalizar la sincronización guardando todos los datos
            finalizeSyncWithRealData(allPlayers, callback);
        });
    }

    /**
     * Guarda todos los jugadores y marca la sincronización como completada
     */
    private void finalizeSyncWithRealData(List<PlayerEntity> allPlayers, SyncCallback callback) {
        executor.execute(() -> {
            try {
                syncStatus.postValue("Guardando " + allPlayers.size() + " jugadores en la base de datos...");
                Log.d(TAG, "Guardando " + allPlayers.size() + " jugadores en Room");

                // Si obtuvimos pocos jugadores reales, completamos con datos mock
                if (allPlayers.size() < 100) {
                    Log.w(TAG, "Solo obtuvimos " + allPlayers.size() + " jugadores reales, completando con mock");
                    List<TeamEntity> teams = teamDao.getAllTeamsSync();
                    List<PlayerEntity> mockPlayers = generateMockPlayersForMissingTeams(teams);
                    allPlayers.addAll(mockPlayers);
                    Log.d(TAG, "Agregados " + mockPlayers.size() + " jugadores mock adicionales");
                }

                // Limpiar datos antiguos y guardar los nuevos
                playerDao.deleteAllPlayers();
                playerDao.insertPlayers(allPlayers);

                // Marcar la sincronización como completada
                markSyncCompleted(allPlayers.size());

                syncStatus.postValue("Sincronización completada exitosamente");
                isSyncing.postValue(false);

                Log.d(TAG, "Sincronización completa exitosa: " + allPlayers.size() + " jugadores guardados");

                if (callback != null) {
                    callback.onSuccess();
                }

            } catch (Exception e) {
                Log.e(TAG, "Error finalizando la sincronización", e);
                handleSyncError(e, callback);
            }
        });
    }

    /**
     * Realiza una sincronización parcial (solo lo que falta)
     */
    private void performPartialSync(SyncDecision decision, SyncCallback callback) {
        Log.d(TAG, "Realizando sincronización parcial: " + decision.reason);
        syncStatus.postValue("Completando datos faltantes...");

        if (decision.missingPlayers) {
            Log.d(TAG, "Generando jugadores faltantes");
            executor.execute(() -> {
                try {
                    List<TeamEntity> teams = teamDao.getAllTeamsSync();
                    List<PlayerEntity> mockPlayers = generateMockPlayersForMissingTeams(teams);
                    playerDao.insertPlayers(mockPlayers);

                    markSyncCompleted(mockPlayers.size());

                    syncStatus.postValue("Datos completados correctamente");
                    isSyncing.postValue(false);

                    Log.d(TAG, "Sincronización parcial completada: " + mockPlayers.size() + " jugadores agregados");

                    if (callback != null) {
                        callback.onSuccess();
                    }
                } catch (Exception e) {
                    handleSyncError(e, callback);
                }
            });
        }
    }

    /**
     * Genera datos mock como fallback cuando falla la API
     */
    private void generateMockDataAsFallback(SyncCallback callback) {
        Log.d(TAG, "API no disponible, generando datos mock como alternativa");
        syncStatus.postValue("Generando datos de demostración...");

        executor.execute(() -> {
            try {
                // Limpiar datos existentes
                teamDao.deleteAllTeams();
                playerDao.deleteAllPlayers();

                // Generar equipos y jugadores mock
                List<TeamEntity> mockTeams = createMockTeams();
                teamDao.insertTeams(mockTeams);
                Log.d(TAG, "Generados " + mockTeams.size() + " equipos mock");

                List<PlayerEntity> mockPlayers = createMockPlayers(mockTeams);
                playerDao.insertPlayers(mockPlayers);
                Log.d(TAG, "Generados " + mockPlayers.size() + " jugadores mock");

                // Marcar como sincronizado aunque sean datos mock
                markSyncCompleted(mockPlayers.size());

                syncStatus.postValue("Datos de demostración listos");
                isSyncing.postValue(false);

                Log.d(TAG, "Datos mock generados completamente");

                if (callback != null) {
                    callback.onSuccess();
                }

            } catch (Exception e) {
                Log.e(TAG, "Error generando datos mock", e);
                handleSyncError(e, callback);
            }
        });
    }

    // ====== MÉTODOS AUXILIARES ======

    /**
     * Guarda los equipos de la API en la base de datos
     */
    private void saveTeamsToDatabase(List<TeamAPI> apiTeams) {
        List<TeamEntity> entities = new ArrayList<>();

        for (TeamAPI apiTeam : apiTeams) {
            TeamEntity entity = new TeamEntity();
            entity.setTeamId(apiTeam.getId());
            entity.setName(apiTeam.getName() != null ? apiTeam.getName() : "Equipo sin nombre");
            entity.setCrestUrl(apiTeam.getCrest() != null ? apiTeam.getCrest() : "");
            entities.add(entity);
        }

        // Limpiar equipos antiguos y guardar los nuevos
        teamDao.deleteAllTeams();
        teamDao.insertTeams(entities);

        Log.d(TAG, "Guardados " + entities.size() + " equipos en la base de datos");
    }

    /**
     * Convierte jugadores de la API a entidades de base de datos
     */
    private List<PlayerEntity> convertApiPlayersToEntities(List<PlayerAPI> apiPlayers, TeamAPI team) {
        List<PlayerEntity> entities = new ArrayList<>();

        for (PlayerAPI apiPlayer : apiPlayers) {
            PlayerEntity entity = new PlayerEntity();
            entity.setPlayerId(apiPlayer.getId());
            entity.setName(apiPlayer.getName() != null ? apiPlayer.getName() : "Jugador");
            entity.setTeamId(team.getId());
            entity.setTeamName(team.getName());
            entity.setPosition(translatePositionToSpanish(apiPlayer.getPosition()));
            entity.setNationality(apiPlayer.getNationality() != null ? apiPlayer.getNationality() : "España");
            entity.setCurrentPrice(calculatePlayerPrice(entity.getPosition()));
            entity.setTotalPoints(generateRandomPoints(entity.getPosition()));
            entity.setAvailable(true);

            entities.add(entity);
        }

        return entities;
    }

    /**
     * Traduce las posiciones del inglés al español
     */
    private String translatePositionToSpanish(String englishPosition) {
        if (englishPosition == null) return "Medio";

        String pos = englishPosition.toUpperCase();
        if (pos.contains("GOALKEEPER") || pos.contains("KEEPER")) {
            return "Portero";
        } else if (pos.contains("DEFENDER") || pos.contains("DEFENCE")) {
            return "Defensa";
        } else if (pos.contains("MIDFIELDER") || pos.contains("MIDFIELD")) {
            return "Medio";
        } else if (pos.contains("FORWARD") || pos.contains("ATTACKER") || pos.contains("STRIKER")) {
            return "Delantero";
        }

        return "Medio"; // Por defecto si no reconocemos la posición
    }

    /**
     * Calcula un precio realista según la posición del jugador
     */
    private int calculatePlayerPrice(String position) {
        switch (position) {
            case "Portero":
                return (int) (Math.random() * 10) + 8;   // Entre 8 y 18 millones
            case "Defensa":
                return (int) (Math.random() * 15) + 5;   // Entre 5 y 20 millones
            case "Medio":
                return (int) (Math.random() * 20) + 8;   // Entre 8 y 28 millones
            case "Delantero":
                return (int) (Math.random() * 25) + 10;  // Entre 10 y 35 millones
            default:
                return 12; // Precio por defecto
        }
    }

    /**
     * Genera puntos aleatorios realistas según la posición
     */
    private int generateRandomPoints(String position) {
        switch (position) {
            case "Portero":
                return (int) (Math.random() * 50) + 30;  // Entre 30 y 80 puntos
            case "Defensa":
                return (int) (Math.random() * 60) + 20;  // Entre 20 y 80 puntos
            case "Medio":
                return (int) (Math.random() * 80) + 30;  // Entre 30 y 110 puntos
            case "Delantero":
                return (int) (Math.random() * 100) + 40; // Entre 40 y 140 puntos
            default:
                return 50; // Puntos por defecto
        }
    }

    /**
     * Marca la sincronización como completada en SharedPreferences
     */
    private void markSyncCompleted(int playersCount) {
        syncPrefs.edit()
                .putLong(PREF_LAST_SYNC, System.currentTimeMillis())
                .putBoolean(PREF_TEAMS_SYNCED, true)
                .putInt(PREF_PLAYERS_SYNCED, playersCount)
                .apply();

        Log.d(TAG, "Sincronización marcada como completada con " + playersCount + " jugadores");
    }

    /**
     * Maneja errores durante la sincronización
     */
    private void handleSyncError(Throwable error, SyncCallback callback) {
        Log.e(TAG, "Error durante la sincronización", error);
        syncStatus.postValue("Error: " + error.getMessage());
        isSyncing.postValue(false);

        if (callback != null) {
            callback.onError(error);
        }
    }

    /**
     * Ejecuta código en el hilo principal de la UI
     */
    private void runOnMainThread(Runnable action) {
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        mainHandler.post(action);
    }

    // ====== CONVERSORES PARA LA UI ======

    /**
     * Convierte TeamEntity a Team para el mercado
     */
    private List<Team> convertTeamEntitiesToMarketTeams(List<TeamEntity> entities) {
        List<Team> teams = new ArrayList<>();
        if (entities == null) return teams;

        for (TeamEntity entity : entities) {
            Team team = new Team();
            team.setTeamId(entity.getTeamId());
            team.setName(entity.getName());
            team.setCrestUrl(entity.getCrestUrl());
            teams.add(team);
        }
        return teams;
    }

    /**
     * Convierte PlayerEntity a Player para el mercado
     */
    private List<Player> convertPlayerEntitiesToMarketPlayers(List<PlayerEntity> entities) {
        List<Player> players = new ArrayList<>();
        if (entities == null) return players;

        for (PlayerEntity entity : entities) {
            Player player = new Player();
            player.setPlayerId(entity.getPlayerId());
            player.setName(entity.getName());
            player.setTeamId(entity.getTeamId());
            player.setTeamName(entity.getTeamName());
            player.setNationality(entity.getNationality());
            player.setPosition(entity.getPosition());
            player.setCurrentPrice(entity.getCurrentPrice());
            player.setTotalPoints(entity.getTotalPoints());
            player.setAvailable(entity.isAvailable());
            players.add(player);
        }
        return players;
    }

    // ====== DATOS MOCK PARA FALLBACK ======

    /**
     * Crea equipos mock de LaLiga
     */
    private List<TeamEntity> createMockTeams() {
        String[] teamNames = {
                "Real Madrid", "FC Barcelona", "Atlético Madrid", "Real Sociedad",
                "Real Betis", "Villarreal", "Athletic Bilbao", "Valencia",
                "Sevilla", "Getafe", "Osasuna", "Celta Vigo",
                "Rayo Vallecano", "Mallorca", "Girona", "Las Palmas",
                "Deportivo Alavés", "Cádiz", "Granada", "Almería"
        };

        List<TeamEntity> teams = new ArrayList<>();

        for (int i = 0; i < teamNames.length; i++) {
            TeamEntity team = new TeamEntity();
            team.setTeamId(i + 1);
            team.setName(teamNames[i]);
            team.setCrestUrl("https://crests.football-data.org/" + (i + 1) + ".png");
            teams.add(team);
        }

        return teams;
    }

    /**
     * Crea jugadores mock para todos los equipos
     */
    private List<PlayerEntity> createMockPlayers(List<TeamEntity> teams) {
        // Nombres de jugadores por posición
        String[] porteros = {"Ter Stegen", "Courtois", "Oblak", "Unai Simón", "Bono", "Dmitrovic", "Remiro"};
        String[] defensas = {"Piqué", "Ramos", "Giménez", "Koundé", "Alaba", "Militão", "Hermoso", "Araujo", "Pau Torres"};
        String[] medios = {"Busquets", "Modric", "Koke", "Pedri", "Gavi", "Casemiro", "De Jong", "Camavinga", "Canales"};
        String[] delanteros = {"Benzema", "Lewandowski", "Griezmann", "Morata", "Depay", "Vinícius", "Ansu Fati", "Raphinha"};

        List<PlayerEntity> players = new ArrayList<>();
        int playerId = 1;

        for (TeamEntity team : teams) {
            // Generar plantilla completa: 2 porteros, 8 defensas, 8 medios, 6 delanteros

            // Porteros
            for (int i = 0; i < 2; i++) {
                players.add(createMockPlayer(playerId++, team, "Portero",
                        porteros[(playerId + i) % porteros.length]));
            }

            // Defensas
            for (int i = 0; i < 8; i++) {
                players.add(createMockPlayer(playerId++, team, "Defensa",
                        defensas[(playerId + i) % defensas.length]));
            }

            // Medios
            for (int i = 0; i < 8; i++) {
                players.add(createMockPlayer(playerId++, team, "Medio",
                        medios[(playerId + i) % medios.length]));
            }

            // Delanteros
            for (int i = 0; i < 6; i++) {
                players.add(createMockPlayer(playerId++, team, "Delantero",
                        delanteros[(playerId + i) % delanteros.length]));
            }
        }

        return players;
    }

    /**
     * Crea un jugador mock individual
     */
    private PlayerEntity createMockPlayer(int playerId, TeamEntity team, String position, String baseName) {
        PlayerEntity player = new PlayerEntity();
        player.setPlayerId(playerId);
        player.setName(baseName + " " + playerId); // Agregar número para hacer nombres únicos
        player.setTeamId(team.getTeamId());
        player.setTeamName(team.getName());
        player.setPosition(position);
        player.setNationality("España");
        player.setCurrentPrice(calculatePlayerPrice(position));
        player.setTotalPoints(generateRandomPoints(position));
        player.setAvailable(true);
        return player;
    }

    /**
     * Genera jugadores mock para equipos que no tienen jugadores completos
     */
    private List<PlayerEntity> generateMockPlayersForMissingTeams(List<TeamEntity> teams) {
        Log.d(TAG, "Generando jugadores mock para completar equipos faltantes");
        return createMockPlayers(teams);
    }

    // ====== MÉTODOS PARA FICHAJES ======

    /**
     * Marca un jugador como comprado (no disponible en el mercado)
     */
    public void buyPlayer(int playerId, @Nullable SyncCallback callback) {
        Log.d(TAG, "Comprando jugador con ID: " + playerId);

        executor.execute(() -> {
            try {
                playerDao.markPlayerAsUnavailable(playerId);
                Log.d(TAG, "Jugador " + playerId + " marcado como no disponible");

                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error comprando jugador " + playerId, e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Marca un jugador como vendido (disponible en el mercado de nuevo)
     */
    public void sellPlayer(int playerId, @Nullable SyncCallback callback) {
        Log.d(TAG, "Vendiendo jugador con ID: " + playerId);

        executor.execute(() -> {
            try {
                playerDao.markPlayerAsAvailable(playerId);
                Log.d(TAG, "Jugador " + playerId + " marcado como disponible");

                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error vendiendo jugador " + playerId, e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    // ====== MÉTODOS ADICIONALES PARA EL MERCADO ======

    /**
     * Obtiene jugadores filtrados por posición
     */
    public LiveData<List<Player>> getPlayersByPosition(String position) {
        return Transformations.map(
                playerDao.getPlayersByPosition(position),
                this::convertPlayerEntitiesToMarketPlayers
        );
    }

    /**
     * Obtiene los mejores jugadores por puntos
     */
    public LiveData<List<Player>> getTopPlayersByPoints() {
        return Transformations.map(
                playerDao.getTopPlayersByPoints(),
                this::convertPlayerEntitiesToMarketPlayers
        );
    }

    /**
     * Actualiza el precio de un jugador específico
     */
    public void updatePlayerPrice(int playerId, int newPrice, @Nullable SyncCallback callback) {
        Log.d(TAG, "Actualizando precio del jugador " + playerId + " a " + newPrice + "M");

        executor.execute(() -> {
            try {
                playerDao.updatePlayerPrice(playerId, newPrice);
                Log.d(TAG, "Precio actualizado correctamente");

                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error actualizando precio del jugador " + playerId, e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Limpia todos los datos de la base de datos
     */
    public void clearAllData(@Nullable SyncCallback callback) {
        Log.d(TAG, "Limpiando todos los datos de la base de datos");

        executor.execute(() -> {
            try {
                playerDao.deleteAllPlayers();
                teamDao.deleteAllTeams();
                syncPrefs.edit().clear().apply();

                Log.d(TAG, "Todos los datos limpiados correctamente");

                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error limpiando datos", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Fuerza una nueva sincronización completa
     */
    public void forceSyncFromAPI(@Nullable SyncCallback callback) {
        Log.d(TAG, "Forzando sincronización completa desde la API");
        syncPrefs.edit().remove(PREF_LAST_SYNC).apply();
        syncLaLigaTeams(callback);
    }

    // ====== GETTERS PARA LA UI ======

    /**
     * Devuelve si estamos sincronizando en este momento
     */
    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }

    /**
     * Devuelve el estado actual de la sincronización
     */
    public LiveData<String> getSyncStatus() {
        return syncStatus;
    }

    /**
     * Método de conveniencia para iniciar sincronización sin callback
     */
    public void loadTeams() {
        syncLaLigaTeams(null);
    }

    // ====== CLASES AUXILIARES ======

    private static class SyncDecision {
        SyncStrategy strategy;
        String reason;
        boolean missingPlayers = false;
    }

    private enum SyncStrategy {
        USE_CACHE, FULL_SYNC, PARTIAL_SYNC, MOCK_FALLBACK
    }
}
