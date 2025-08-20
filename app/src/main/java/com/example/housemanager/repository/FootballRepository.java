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
 * Repositorio central para manejar todos los datos de football
 * Se encarga de sincronizar con la API y mantener cache local
 */
public class FootballRepository {

    private static final String TAG = "FootballRepository";

    // Configuración del cache
    private static final String PREFS_NAME = "football_sync_prefs";
    private static final String PREF_LAST_SYNC = "last_full_sync";
    private static final String PREF_TEAMS_SYNCED = "teams_synced";
    private static final String PREF_PLAYERS_SYNCED = "players_synced";
    private static final long SYNC_INTERVAL_DAYS = 7; // Resincronizar cada semana

    // Callback para informar del progreso
    public interface SyncCallback {
        void onSuccess();
        void onError(Throwable t);
        default void onProgress(String message, int current, int total) {
            // Método opcional para progreso
        }
    }

    private static FootballRepository instance;

    // DAOs para acceso a datos
    private final PlayerDao playerDao;
    private final TeamDao teamDao;
    private final FootballApiService apiService;
    private final ExecutorService executor;
    private final SharedPreferences syncPrefs;

    // LiveData para comunicar estado a la UI
    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);
    private final MutableLiveData<String> syncStatus = new MutableLiveData<>("");

    private FootballRepository(Context context) {
        Log.d(TAG, "Inicializando FootballRepository");

        // Configurar base de datos
        HouseManagerDatabase db = HouseManagerDatabase.getInstance(context);
        playerDao = db.playerDao();
        teamDao = db.teamDao();

        // Configurar API
        apiService = ApiClient.getClient().create(FootballApiService.class);
        executor = Executors.newFixedThreadPool(3);

        // SharedPreferences para control de cache
        syncPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        Log.d(TAG, "FootballRepository listo");
    }

    public static synchronized FootballRepository getInstance(Context context) {
        if (instance == null) {
            instance = new FootballRepository(context.getApplicationContext());
        }
        return instance;
    }

    // Métodos principales para obtener datos

    /**
     * Devuelve todos los equipos para usar en el mercado de fichajes
     */
    public LiveData<List<Team>> getAllTeams() {
        return Transformations.map(teamDao.getAllTeamEntities(), this::convertTeamEntitiesToMarketTeams);
    }

    /**
     * Devuelve todos los jugadores para el mercado
     */
    public LiveData<List<Player>> getAllPlayers() {
        return Transformations.map(playerDao.getAllPlayerEntities(), this::convertPlayerEntitiesToMarketPlayers);
    }

    /**
     * Obtiene la plantilla de un equipo específico
     * Devuelve entities que luego el ViewModel convierte a PlayerAPI
     */
    public LiveData<List<PlayerEntity>> getSquadEntitiesByTeam(int teamId) {
        return playerDao.getSquadEntitiesByTeam(teamId);
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
     * Cuenta los jugadores disponibles en el mercado
     */
    public LiveData<Integer> getAvailablePlayersCount() {
        return playerDao.getAvailablePlayersCount();
    }

    // Sincronización principal

    /**
     * Método principal de sincronización. Decide automáticamente qué hacer.
     */
    public void syncLaLigaTeams(@Nullable SyncCallback callback) {
        Log.d(TAG, "Iniciando proceso de sincronización");
        isSyncing.setValue(true);
        syncStatus.setValue("Verificando datos existentes...");

        executor.execute(() -> {
            try {
                SyncDecision decision = decideSyncStrategy();
                Log.d(TAG, "Estrategia: " + decision.strategy + " - " + decision.reason);

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
                Log.e(TAG, "Error durante sincronización", e);
                handleSyncError(e, callback);
            }
        });
    }

    /**
     * Analiza el estado actual y decide qué hacer
     */
    private SyncDecision decideSyncStrategy() {
        int existingTeams = teamDao.getTeamsCountSync();
        int existingPlayers = playerDao.getPlayersCountSync();
        long lastSync = syncPrefs.getLong(PREF_LAST_SYNC, 0);
        long daysSinceLastSync = (System.currentTimeMillis() - lastSync) / (1000 * 60 * 60 * 24);

        Log.d(TAG, "Estado actual - Equipos: " + existingTeams + ", Jugadores: " + existingPlayers);
        Log.d(TAG, "Última sync hace " + daysSinceLastSync + " días");

        SyncDecision decision = new SyncDecision();

        if (existingTeams == 0 && existingPlayers == 0) {
            // Primera vez
            decision.strategy = SyncStrategy.FULL_SYNC;
            decision.reason = "Primera sincronización completa";

        } else if (daysSinceLastSync >= SYNC_INTERVAL_DAYS) {
            // Datos antiguos
            decision.strategy = SyncStrategy.FULL_SYNC;
            decision.reason = "Datos antiguos, actualizando";

        } else if (existingTeams > 0 && existingPlayers < (existingTeams * 15)) {
            // Faltan jugadores
            decision.strategy = SyncStrategy.PARTIAL_SYNC;
            decision.reason = "Faltan jugadores, completando";
            decision.missingPlayers = true;

        } else if (existingTeams > 0 && existingPlayers > 0) {
            // Datos válidos
            decision.strategy = SyncStrategy.USE_CACHE;
            decision.reason = "Datos válidos en cache";

        } else {
            // Algo raro, usar mock
            decision.strategy = SyncStrategy.MOCK_FALLBACK;
            decision.reason = "Estado inconsistente, usando mock";
        }

        return decision;
    }

    /**
     * Usa los datos que ya tenemos
     */
    private void useCachedData(SyncCallback callback) {
        Log.d(TAG, "Usando datos del cache");
        syncStatus.postValue("Cargando desde cache...");

        try {
            Thread.sleep(800); // Pausa para que se vea el mensaje
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        syncStatus.postValue("Datos cargados");
        isSyncing.postValue(false);

        if (callback != null) {
            callback.onSuccess();
        }
    }

    /**
     * Sincronización completa desde la API
     */
    private void performFullSync(SyncCallback callback) {
        Log.d(TAG, "Sincronización completa desde API");
        syncStatus.postValue("Conectando con football-data.org...");

        // Obtener equipos de LaLiga
        apiService.getLaLigaTeams().enqueue(new Callback<TeamsResponse>() {
            @Override
            public void onResponse(Call<TeamsResponse> call, Response<TeamsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TeamAPI> apiTeams = response.body().getTeams();
                    Log.d(TAG, "Recibidos " + apiTeams.size() + " equipos");

                    executor.execute(() -> {
                        try {
                            syncStatus.postValue("Guardando equipos...");
                            saveTeamsToDatabase(apiTeams);

                            syncStatus.postValue("Obteniendo jugadores...");
                            syncAllPlayersFromAPI(apiTeams, callback);

                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando equipos", e);
                            handleSyncError(e, callback);
                        }
                    });
                } else {
                    Log.e(TAG, "Error en API, código: " + response.code());
                    generateMockDataAsFallback(callback);
                }
            }

            @Override
            public void onFailure(Call<TeamsResponse> call, Throwable t) {
                Log.e(TAG, "Fallo de conexión", t);
                generateMockDataAsFallback(callback);
            }
        });
    }

    /**
     * Obtiene jugadores de todos los equipos
     */
    private void syncAllPlayersFromAPI(List<TeamAPI> teams, SyncCallback callback) {
        Log.d(TAG, "Sincronizando jugadores de " + teams.size() + " equipos");

        executor.execute(() -> {
            List<PlayerEntity> allPlayers = new ArrayList<>();
            int successfulTeams = 0;

            for (int i = 0; i < teams.size(); i++) {
                TeamAPI team = teams.get(i);
                final int teamIndex = i + 1;
                final int totalTeams = teams.size();

                // Esperar entre llamadas para no saturar la API
                if (i > 0) {
                    try {
                        Thread.sleep(7000); // 7 segundos entre llamadas
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                String statusMessage = "Obteniendo jugadores de " + team.getName() + " (" + teamIndex + "/" + totalTeams + ")";
                syncStatus.postValue(statusMessage);

                if (callback != null) {
                    runOnMainThread(() -> callback.onProgress("Sincronizando " + team.getName(), teamIndex, totalTeams));
                }

                try {
                    Response<TeamAPI> response = apiService.getTeamDetails(team.getId()).execute();

                    if (response.isSuccessful() && response.body() != null) {
                        TeamAPI teamWithPlayers = response.body();

                        if (teamWithPlayers.getSquad() != null && !teamWithPlayers.getSquad().isEmpty()) {
                            List<PlayerEntity> teamPlayers = convertApiPlayersToEntities(
                                    teamWithPlayers.getSquad(), team);
                            allPlayers.addAll(teamPlayers);
                            successfulTeams++;

                            Log.d(TAG, "Equipo " + team.getName() + " OK: " + teamPlayers.size() + " jugadores");
                        }
                    } else {
                        Log.e(TAG, "Error con " + team.getName() + ", código: " + response.code());
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error sincronizando " + team.getName(), e);
                }
            }

            Log.d(TAG, "Sync terminada: " + successfulTeams + "/" + teams.size() + " equipos");
            Log.d(TAG, "Total jugadores: " + allPlayers.size());

            finalizeSyncWithRealData(allPlayers, callback);
        });
    }

    /**
     * Guarda todos los jugadores y finaliza
     */
    private void finalizeSyncWithRealData(List<PlayerEntity> allPlayers, SyncCallback callback) {
        executor.execute(() -> {
            try {
                syncStatus.postValue("Guardando " + allPlayers.size() + " jugadores...");

                // Si obtuvimos pocos jugadores, completar con mock
                if (allPlayers.size() < 100) {
                    Log.w(TAG, "Solo " + allPlayers.size() + " jugadores reales, completando con mock");
                    List<TeamEntity> teams = teamDao.getAllTeamsSync();
                    List<PlayerEntity> mockPlayers = generateMockPlayersForMissingTeams(teams);
                    allPlayers.addAll(mockPlayers);
                }

                // Limpiar y guardar
                playerDao.deleteAllPlayers();
                playerDao.insertPlayers(allPlayers);

                markSyncCompleted(allPlayers.size());

                syncStatus.postValue("Sincronización completada");
                isSyncing.postValue(false);

                if (callback != null) {
                    callback.onSuccess();
                }

            } catch (Exception e) {
                handleSyncError(e, callback);
            }
        });
    }

    /**
     * Sincronización parcial (solo lo que falta)
     */
    private void performPartialSync(SyncDecision decision, SyncCallback callback) {
        Log.d(TAG, "Sincronización parcial: " + decision.reason);
        syncStatus.postValue("Completando datos...");

        if (decision.missingPlayers) {
            executor.execute(() -> {
                try {
                    List<TeamEntity> teams = teamDao.getAllTeamsSync();
                    List<PlayerEntity> mockPlayers = generateMockPlayersForMissingTeams(teams);
                    playerDao.insertPlayers(mockPlayers);

                    markSyncCompleted(mockPlayers.size());

                    syncStatus.postValue("Datos completados");
                    isSyncing.postValue(false);

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
     * Genera datos mock cuando falla la API
     */
    private void generateMockDataAsFallback(SyncCallback callback) {
        Log.d(TAG, "API no disponible, generando datos mock");
        syncStatus.postValue("Generando datos de prueba...");

        executor.execute(() -> {
            try {
                // Limpiar todo
                teamDao.deleteAllTeams();
                playerDao.deleteAllPlayers();

                // Generar datos mock
                List<TeamEntity> mockTeams = createMockTeams();
                teamDao.insertTeams(mockTeams);

                List<PlayerEntity> mockPlayers = createMockPlayers(mockTeams);
                playerDao.insertPlayers(mockPlayers);

                markSyncCompleted(mockPlayers.size());

                syncStatus.postValue("Datos de prueba listos");
                isSyncing.postValue(false);

                if (callback != null) {
                    callback.onSuccess();
                }

            } catch (Exception e) {
                handleSyncError(e, callback);
            }
        });
    }

    // Métodos auxiliares

    /**
     * Guarda equipos de la API en la base de datos
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

        teamDao.deleteAllTeams();
        teamDao.insertTeams(entities);

        Log.d(TAG, "Guardados " + entities.size() + " equipos");
    }

    /**
     * Convierte jugadores de API a entities
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
     * Traduce posiciones del inglés al español
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

        return "Medio";
    }

    /**
     * Calcula precio según posición
     */
    private int calculatePlayerPrice(String position) {
        switch (position) {
            case "Portero":
                return (int) (Math.random() * 10) + 8;   // 8-18M
            case "Defensa":
                return (int) (Math.random() * 15) + 5;   // 5-20M
            case "Medio":
                return (int) (Math.random() * 20) + 8;   // 8-28M
            case "Delantero":
                return (int) (Math.random() * 25) + 10;  // 10-35M
            default:
                return 12;
        }
    }

    /**
     * Genera puntos según posición
     */
    private int generateRandomPoints(String position) {
        switch (position) {
            case "Portero":
                return (int) (Math.random() * 50) + 30;  // 30-80 pts
            case "Defensa":
                return (int) (Math.random() * 60) + 20;  // 20-80 pts
            case "Medio":
                return (int) (Math.random() * 80) + 30;  // 30-110 pts
            case "Delantero":
                return (int) (Math.random() * 100) + 40; // 40-140 pts
            default:
                return 50;
        }
    }

    /**
     * Marca la sync como completada
     */
    private void markSyncCompleted(int playersCount) {
        syncPrefs.edit()
                .putLong(PREF_LAST_SYNC, System.currentTimeMillis())
                .putBoolean(PREF_TEAMS_SYNCED, true)
                .putInt(PREF_PLAYERS_SYNCED, playersCount)
                .apply();

        Log.d(TAG, "Sync completada con " + playersCount + " jugadores");
    }

    /**
     * Maneja errores
     */
    private void handleSyncError(Throwable error, SyncCallback callback) {
        Log.e(TAG, "Error en sync", error);
        syncStatus.postValue("Error: " + error.getMessage());
        isSyncing.postValue(false);

        if (callback != null) {
            callback.onError(error);
        }
    }

    /**
     * Ejecuta en hilo principal
     */
    private void runOnMainThread(Runnable action) {
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        mainHandler.post(action);
    }

    // Conversores para la UI

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

    // Datos mock para fallback

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
        String[] porteros = {"Ter Stegen", "Courtois", "Oblak", "Unai Simón", "Bono", "Dmitrovic", "Remiro"};
        String[] defensas = {"Piqué", "Ramos", "Giménez", "Koundé", "Alaba", "Militão", "Hermoso", "Araujo", "Pau Torres"};
        String[] medios = {"Busquets", "Modric", "Koke", "Pedri", "Gavi", "Casemiro", "De Jong", "Camavinga", "Canales"};
        String[] delanteros = {"Benzema", "Lewandowski", "Griezmann", "Morata", "Depay", "Vinícius", "Ansu Fati", "Raphinha"};

        List<PlayerEntity> players = new ArrayList<>();
        int playerId = 1;

        for (TeamEntity team : teams) {
            // Plantilla completa: 2 porteros, 8 defensas, 8 medios, 6 delanteros

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
        player.setName(baseName + " " + playerId);
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
     * Genera jugadores mock para equipos faltantes
     */
    private List<PlayerEntity> generateMockPlayersForMissingTeams(List<TeamEntity> teams) {
        return createMockPlayers(teams);
    }

    // Métodos para fichajes

    /**
     * Marca un jugador como comprado
     */
    public void buyPlayer(int playerId, @Nullable SyncCallback callback) {
        executor.execute(() -> {
            try {
                playerDao.markPlayerAsUnavailable(playerId);
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Marca un jugador como vendido
     */
    public void sellPlayer(int playerId, @Nullable SyncCallback callback) {
        executor.execute(() -> {
            try {
                playerDao.markPlayerAsAvailable(playerId);
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    // Métodos adicionales para el mercado

    /**
     * Jugadores filtrados por posición
     */
    public LiveData<List<Player>> getPlayersByPosition(String position) {
        return Transformations.map(
                playerDao.getPlayersByPosition(position),
                this::convertPlayerEntitiesToMarketPlayers
        );
    }

    /**
     * Los mejores jugadores por puntos
     */
    public LiveData<List<Player>> getTopPlayersByPoints() {
        return Transformations.map(
                playerDao.getTopPlayersByPoints(),
                this::convertPlayerEntitiesToMarketPlayers
        );
    }

    /**
     * Actualiza precio de un jugador
     */
    public void updatePlayerPrice(int playerId, int newPrice, @Nullable SyncCallback callback) {
        executor.execute(() -> {
            try {
                playerDao.updatePlayerPrice(playerId, newPrice);
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Limpia todos los datos
     */
    public void clearAllData(@Nullable SyncCallback callback) {
        executor.execute(() -> {
            try {
                playerDao.deleteAllPlayers();
                teamDao.deleteAllTeams();
                syncPrefs.edit().clear().apply();

                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Fuerza nueva sync completa
     */
    public void forceSyncFromAPI(@Nullable SyncCallback callback) {
        syncPrefs.edit().remove(PREF_LAST_SYNC).apply();
        syncLaLigaTeams(callback);
    }

    // Getters para la UI

    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }

    public LiveData<String> getSyncStatus() {
        return syncStatus;
    }

    public void loadTeams() {
        syncLaLigaTeams(null);
    }

    // Clases auxiliares

    private static class SyncDecision {
        SyncStrategy strategy;
        String reason;
        boolean missingPlayers = false;
    }

    private enum SyncStrategy {
        USE_CACHE, FULL_SYNC, PARTIAL_SYNC, MOCK_FALLBACK
    }
}