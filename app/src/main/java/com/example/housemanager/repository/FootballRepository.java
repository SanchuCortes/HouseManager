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

/** Orquesta acceso a DB y API. Expone datos listos para UI. */
public class FootballRepository {

    private static final String TAG = "FootballRepository";

    /** Preferencias para control de sincronización. */
    private static final String PREFS_NAME = "football_sync_prefs";
    private static final String PREF_LAST_SYNC = "last_full_sync";
    private static final String PREF_TEAMS_SYNCED = "teams_synced";
    private static final String PREF_PLAYERS_SYNCED = "players_synced";
    private static final long SYNC_INTERVAL_DAYS = 7;

    /** Notifica estado de sincronización. */
    public interface SyncCallback {
        void onSuccess();
        void onError(Throwable t);
        void onProgress(String message, int current, int total);
    }

    private static FootballRepository instance;

    private final PlayerDao playerDao;
    private final TeamDao teamDao;
    private final FootballApiService apiService;
    private final ExecutorService executor;
    private final SharedPreferences syncPrefs;

    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);
    private final MutableLiveData<String> syncStatus = new MutableLiveData<>("");

    private FootballRepository(Context context) {
        HouseManagerDatabase db = HouseManagerDatabase.getInstance(context);
        playerDao = db.playerDao();
        teamDao = db.teamDao();
        apiService = ApiClient.getClient().create(FootballApiService.class);
        executor = Executors.newFixedThreadPool(3);
        syncPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Singleton del repositorio. */
    public static synchronized FootballRepository getInstance(Context context) {
        if (instance == null) {
            instance = new FootballRepository(context.getApplicationContext());
        }
        return instance;
    }

    /** Estado para mostrar en UI. */
    public LiveData<Boolean> getIsSyncing() { return isSyncing; }
    public LiveData<String> getSyncStatus() { return syncStatus; }

    /** Equipos listos para UI. */
    public LiveData<List<Team>> getAllTeams() {
        return Transformations.map(
                teamDao.getAllTeamEntities(),
                this::convertTeamEntitiesToMarketTeams
        );
    }

    /** Jugadores listos para UI. */
    public LiveData<List<Player>> getAllPlayers() {
        return Transformations.map(
                playerDao.getAllPlayerEntities(),
                this::convertPlayerEntitiesToMarketPlayers
        );
    }

    /** Plantilla de un equipo en formato DTO usado por UI. */
    public LiveData<List<PlayerAPI>> getSquadApiByTeam(int teamId) {
        LiveData<List<PlayerEntity>> entities = playerDao.getSquadByTeam(teamId);
        return Transformations.map(entities, list -> {
            List<PlayerAPI> out = new ArrayList<>();
            if (list == null) return out;
            for (PlayerEntity e : list) {
                PlayerAPI dto = new PlayerAPI();
                dto.setId(e.getPlayerId());
                dto.setName(e.getName());
                dto.setPosition(e.getPosition());
                dto.setNationality(e.getNationality());
                dto.setPoints(e.getTotalPoints());
                out.add(dto);
            }
            return out;
        });
    }

    /** Búsqueda simple para el mercado. */
    public LiveData<List<Player>> searchPlayers(String searchTerm) {
        return Transformations.map(
                playerDao.searchAvailablePlayers(searchTerm),
                this::convertPlayerEntitiesToMarketPlayers
        );
    }

    /** Conteo para el mercado. */
    public LiveData<Integer> getAvailablePlayersCount() {
        return playerDao.getAvailablePlayersCount();
    }

    /** Punto de entrada de sincronización. Decide estrategia y ejecuta. */
    public void syncLaLigaTeams(@Nullable SyncCallback callback) {
        isSyncing.postValue(true);
        syncStatus.postValue("Comprobando datos...");

        executor.execute(() -> {
            try {
                SyncDecision d = decideSyncStrategy();
                switch (d.strategy) {
                    case USE_CACHE:
                        useCachedData(callback);
                        break;
                    case FULL_SYNC:
                        performFullSync(callback);
                        break;
                    case PARTIAL_SYNC:
                        // aquí simplificamos: se fuerza full para completar
                        performFullSync(callback);
                        break;
                    case MOCK_FALLBACK:
                        generateMockDataAsFallback(callback);
                        break;
                }
            } catch (Exception e) {
                handleSyncError(e, callback);
            }
        });
    }

    /** Revisa datos y antigüedad para decidir estrategia. */
    private SyncDecision decideSyncStrategy() {
        int teams = teamDao.getTeamsCountSync();
        int players = playerDao.getPlayersCountSync();
        long last = syncPrefs.getLong(PREF_LAST_SYNC, 0);
        long days = (System.currentTimeMillis() - last) / (1000 * 60 * 60 * 24);

        SyncDecision d = new SyncDecision();
        if (teams == 0 && players == 0) {
            d.strategy = SyncStrategy.FULL_SYNC;
            d.reason = "inicio";
        } else if (days >= SYNC_INTERVAL_DAYS) {
            d.strategy = SyncStrategy.FULL_SYNC;
            d.reason = "caducado";
        } else if (teams > 0 && players < (teams * 15)) {
            d.strategy = SyncStrategy.PARTIAL_SYNC;
            d.reason = "incompleto";
        } else {
            d.strategy = SyncStrategy.USE_CACHE;
            d.reason = "válido";
        }
        return d;
    }

    /** Usa los datos locales sin tocar la red. */
    private void useCachedData(SyncCallback callback) {
        syncStatus.postValue("Usando cache local");
        isSyncing.postValue(false);
        if (callback != null) callback.onSuccess();
    }

    /** Sincroniza equipos y plantillas desde la API. */
    private void performFullSync(SyncCallback callback) {
        syncStatus.postValue("Descargando equipos...");

        apiService.getLaLigaTeams().enqueue(new Callback<TeamsResponse>() {
            @Override
            public void onResponse(Call<TeamsResponse> call, Response<TeamsResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getTeams() == null) {
                    generateMockDataAsFallback(callback);
                    return;
                }
                List<TeamAPI> apiTeams = response.body().getTeams();

                executor.execute(() -> {
                    try {
                        syncStatus.postValue("Guardando equipos...");
                        saveTeamsToDatabase(apiTeams);

                        syncStatus.postValue("Descargando plantillas...");
                        syncAllPlayersFromAPI(apiTeams, callback);

                    } catch (Exception e) {
                        handleSyncError(e, callback);
                    }
                });
            }

            @Override
            public void onFailure(Call<TeamsResponse> call, Throwable t) {
                generateMockDataAsFallback(callback);
            }
        });
    }

    /** Recorre equipos y descarga sus jugadores respetando límite de peticiones. */
    private void syncAllPlayersFromAPI(List<TeamAPI> teams, SyncCallback callback) {
        executor.execute(() -> {
            List<PlayerEntity> all = new ArrayList<>();
            int total = teams.size();

            for (int i = 0; i < total; i++) {
                TeamAPI t = teams.get(i);

                // espera para no exceder 10/min
                if (i > 0) {
                    try { Thread.sleep(7000L); } catch (InterruptedException ignored) { }
                }

                if (callback != null) {
                    int curr = i + 1;
                    runOnMainThread(() -> callback.onProgress("Equipo: " + t.getName(), curr, total));
                }

                try {
                    Response<TeamAPI> r = apiService.getTeamDetails(t.getId()).execute();
                    if (r.isSuccessful() && r.body() != null && r.body().getSquad() != null) {
                        all.addAll(convertApiPlayersToEntities(r.body().getSquad(), t));
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Fallo en equipo " + t.getName(), e);
                }
            }

            finalizeSyncWithRealData(all, callback);
        });
    }

    /** Guarda jugadores y marca fin de sincronización. */
    private void finalizeSyncWithRealData(List<PlayerEntity> allPlayers, SyncCallback callback) {
        executor.execute(() -> {
            try {
                syncStatus.postValue("Guardando jugadores...");
                playerDao.deleteAllPlayers();
                if (!allPlayers.isEmpty()) playerDao.insertPlayers(allPlayers);

                markSyncCompleted(allPlayers.size());
                syncStatus.postValue("Listo");
                isSyncing.postValue(false);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                handleSyncError(e, callback);
            }
        });
    }

    /** Solo si la API falla. Crea datos de ejemplo para no dejar la app vacía. */
    private void generateMockDataAsFallback(SyncCallback callback) {
        executor.execute(() -> {
            try {
                teamDao.deleteAllTeams();
                playerDao.deleteAllPlayers();
                List<TeamEntity> teams = createMockTeams();
                teamDao.insertTeams(teams);
                List<PlayerEntity> players = createMockPlayers(teams);
                playerDao.insertPlayers(players);
                markSyncCompleted(players.size());
                syncStatus.postValue("Datos de ejemplo cargados");
                isSyncing.postValue(false);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                handleSyncError(e, callback);
            }
        });
    }

    /** Guarda equipos recibidos de la API. */
    private void saveTeamsToDatabase(List<TeamAPI> apiTeams) {
        List<TeamEntity> entities = new ArrayList<>();
        for (TeamAPI t : apiTeams) {
            TeamEntity e = new TeamEntity();
            e.setTeamId(t.getId());
            e.setName(t.getName() != null ? t.getName() : "Equipo");
            e.setCrestUrl(t.getCrest() != null ? t.getCrest() : "");
            entities.add(e);
        }
        teamDao.deleteAllTeams();
        teamDao.insertTeams(entities);
    }

    /** Convierte DTOs de API a entidades de DB. */
    private List<PlayerEntity> convertApiPlayersToEntities(List<PlayerAPI> apiPlayers, TeamAPI team) {
        List<PlayerEntity> out = new ArrayList<>();
        for (PlayerAPI p : apiPlayers) {
            PlayerEntity e = new PlayerEntity();
            e.setPlayerId(p.getId());
            e.setName(p.getName() != null ? p.getName() : "Jugador");
            e.setTeamId(team.getId());
            e.setTeamName(team.getName() != null ? team.getName() : "");
            e.setPosition(translatePositionToSpanish(p.getPosition()));
            e.setNationality(p.getNationality() != null ? p.getNationality() : "España");
            e.setCurrentPrice(calculatePlayerPrice(e.getPosition()));
            e.setTotalPoints(generateRandomPoints(e.getPosition()));
            e.setAvailable(true);
            out.add(e);
        }
        return out;
    }

    /** Traduce posiciones al esquema usado en la app. */
    private String translatePositionToSpanish(String englishPosition) {
        if (englishPosition == null) return "Medio";
        String pos = englishPosition.toUpperCase();
        if (pos.contains("GOALKEEPER")) return "Portero";
        if (pos.contains("DEFENDER")) return "Defensa";
        if (pos.contains("MIDFIELDER")) return "Medio";
        if (pos.contains("FORWARD") || pos.contains("ATTACKER") || pos.contains("STRIKER")) return "Delantero";
        return "Medio";
    }

    /** Calcula precio aproximado según posición. */
    private int calculatePlayerPrice(String position) {
        switch (position) {
            case "Portero":   return (int) (Math.random() * 10) + 8;
            case "Defensa":   return (int) (Math.random() * 15) + 5;
            case "Medio":     return (int) (Math.random() * 20) + 8;
            case "Delantero": return (int) (Math.random() * 25) + 10;
            default:          return 12;
        }
    }

    /** Genera puntos aproximados según posición. */
    private int generateRandomPoints(String position) {
        switch (position) {
            case "Portero":   return (int) (Math.random() * 50) + 30;
            case "Defensa":   return (int) (Math.random() * 60) + 20;
            case "Medio":     return (int) (Math.random() * 80) + 30;
            case "Delantero": return (int) (Math.random() * 100) + 40;
            default:          return 50;
        }
    }

    /** Marca fin de sincronización en preferencias. */
    private void markSyncCompleted(int playersCount) {
        syncPrefs.edit()
                .putLong(PREF_LAST_SYNC, System.currentTimeMillis())
                .putBoolean(PREF_TEAMS_SYNCED, true)
                .putInt(PREF_PLAYERS_SYNCED, playersCount)
                .apply();
    }

    /** Actualiza estado de error. */
    private void handleSyncError(Throwable error, SyncCallback callback) {
        Log.e(TAG, "sync error", error);
        syncStatus.postValue("Error: " + error.getMessage());
        isSyncing.postValue(false);
        if (callback != null) callback.onError(error);
    }

    /** Ejecuta en hilo principal. */
    private void runOnMainThread(Runnable action) {
        android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
        h.post(action);
    }

    /** Decisión de sincronización. */
    private static class SyncDecision {
        SyncStrategy strategy;
        String reason;
    }

    /** Tipos de sincronización. */
    private enum SyncStrategy { USE_CACHE, FULL_SYNC, PARTIAL_SYNC, MOCK_FALLBACK }

    /** Equipos de ejemplo. */
    private List<TeamEntity> createMockTeams() {
        String[] names = {
                "Real Madrid","FC Barcelona","Atlético Madrid","Real Sociedad",
                "Betis","Villarreal","Athletic","Valencia",
                "Sevilla","Getafe","Osasuna","Celta",
                "Rayo","Mallorca","Girona","Las Palmas",
                "Alavés","Cádiz","Granada","Almería"
        };
        List<TeamEntity> out = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            TeamEntity t = new TeamEntity();
            t.setTeamId(i + 1);
            t.setName(names[i]);
            t.setCrestUrl("https://crests.football-data.org/" + (i + 1) + ".png");
            out.add(t);
        }
        return out;
    }

    /** Jugadores de ejemplo. */
    private List<PlayerEntity> createMockPlayers(List<TeamEntity> teams) {
        String[] porteros = {"Ter Stegen", "Courtois", "Oblak", "Unai Simón"};
        String[] defensas = {"Ramos", "Koundé", "Alaba", "Araujo", "Pau Torres"};
        String[] medios   = {"Modric", "Pedri", "Koke", "Gavi", "De Jong"};
        String[] delanteros = {"Benzema", "Lewandowski", "Griezmann", "Morata", "Vinícius"};

        List<PlayerEntity> players = new ArrayList<>();
        int id = 1;

        for (TeamEntity team : teams) {
            for (int i = 0; i < 2; i++) players.add(mockPlayer(id++, team, "Portero",   porteros[i % porteros.length]));
            for (int i = 0; i < 6; i++) players.add(mockPlayer(id++, team, "Defensa",   defensas[i % defensas.length]));
            for (int i = 0; i < 6; i++) players.add(mockPlayer(id++, team, "Medio",     medios[i % medios.length]));
            for (int i = 0; i < 4; i++) players.add(mockPlayer(id++, team, "Delantero", delanteros[i % delanteros.length]));
        }
        return players;
    }

    /** Crea un jugador de ejemplo. */
    private PlayerEntity mockPlayer(int id, TeamEntity team, String pos, String name) {
        PlayerEntity e = new PlayerEntity();
        e.setPlayerId(id);
        e.setName(name);
        e.setTeamId(team.getTeamId());
        e.setTeamName(team.getName());
        e.setPosition(pos);
        e.setNationality("España");
        e.setCurrentPrice(calculatePlayerPrice(pos));
        e.setTotalPoints(generateRandomPoints(pos));
        e.setAvailable(true);
        return e;
    }

    /** Convierte entidades de equipo a modelo de UI. */
    private List<Team> convertTeamEntitiesToMarketTeams(List<TeamEntity> entities) {
        List<Team> out = new ArrayList<>();
        if (entities == null) return out;
        for (TeamEntity e : entities) {
            Team t = new Team();
            t.setTeamId(e.getTeamId());
            t.setName(e.getName());
            t.setCrestUrl(e.getCrestUrl());
            out.add(t);
        }
        return out;
    }

    /** Convierte entidades de jugador a modelo de UI. */
    private List<Player> convertPlayerEntitiesToMarketPlayers(List<PlayerEntity> entities) {
        List<Player> out = new ArrayList<>();
        if (entities == null) return out;
        for (PlayerEntity e : entities) {
            Player p = new Player();
            p.setPlayerId(e.getPlayerId());
            p.setName(e.getName());
            p.setTeamId(e.getTeamId());
            p.setTeamName(e.getTeamName());
            p.setNationality(e.getNationality());
            p.setPosition(e.getPosition());
            p.setCurrentPrice(e.getCurrentPrice());
            p.setTotalPoints(e.getTotalPoints());
            p.setAvailable(e.isAvailable());
            out.add(p);
        }
        return out;
    }
}
