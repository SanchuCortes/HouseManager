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
import com.example.housemanager.api.models.MatchesResponse;
import com.example.housemanager.api.models.MatchAPI;
import com.example.housemanager.api.models.PlayerAPI;
import com.example.housemanager.api.models.ScoreAPI;
import com.example.housemanager.api.models.TeamAPI;
import com.example.housemanager.api.models.TeamsResponse;
import com.example.housemanager.api.models.CompetitionAPI;
import com.example.housemanager.database.HouseManagerDatabase;
import com.example.housemanager.database.dao.MatchDao;
import com.example.housemanager.database.dao.PlayerDao;
import com.example.housemanager.database.dao.TeamDao;
import com.example.housemanager.database.entities.MatchEntity;
import com.example.housemanager.database.entities.PlayerEntity;
import com.example.housemanager.database.entities.TeamEntity;
import com.example.housemanager.market.Player;
import com.example.housemanager.market.Team;
import com.example.housemanager.database.pojo.ManagerScoreRow;
import com.example.housemanager.repository.models.ManagerScore;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Orquesta acceso a DB y API. Expone datos listos para UI. */
public class FootballRepository {

    /** Resultado de obtener jornada actual. */
    public interface MatchdayCallback {
        void onResult(int matchday);
        void onError(Throwable t);
    }

    private static final String TAG = "FootballRepository";

    /** Preferencias para control de sincronización. */
    private static final String PREFS_NAME = "football_sync_prefs";
    private static final String PREF_LAST_SYNC = "last_full_sync";
    private static final String PREF_TEAMS_SYNCED = "teams_synced";
    private static final String PREF_PLAYERS_SYNCED = "players_synced";
    private static final long SYNC_INTERVAL_DAYS = 1;

    /** Notifica estado de sincronización. */
    public interface SyncCallback {
        void onSuccess();
        void onError(Throwable t);
        void onProgress(String message, int current, int total);
    }

    private static FootballRepository instance;

    private final PlayerDao playerDao;
    private final TeamDao teamDao;
    private final com.example.housemanager.database.dao.LeagueDao leagueDao;
    private final com.example.housemanager.database.dao.LineupDao lineupDao;
    private final MatchDao matchDao;
    private final com.example.housemanager.database.dao.MarketDao marketDao;
    private final com.example.housemanager.database.dao.OwnershipDao ownershipDao;
    private final com.example.housemanager.database.dao.MarketStateDao marketStateDao;
    private final com.example.housemanager.database.dao.CaptainDao captainDao;
    private final com.example.housemanager.database.dao.PlayerMatchPointsDao playerMatchPointsDao;
    // DAOs adicionales para cálculo detallado de puntos por partido
    private final com.example.housemanager.database.dao.MatchEventDao matchEventDao;
    private final com.example.housemanager.database.dao.LineupEntryDao lineupEntryDao;
    private final com.example.housemanager.database.dao.PlayerPointsHistoryDao playerPointsHistoryDao;
    // Calculadora de puntos (por partido)
    private final PointsCalculator pointsCalculator = new PointsCalculator();

    private final FootballApiService apiService;
    private final ExecutorService executor;
    private final SharedPreferences syncPrefs;
    // Preferencias para selección diaria del mercado (por jugadores.available)
    private static final String PREF_MARKET_DAY = "market_day";

    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);
    private final MutableLiveData<String> syncStatus = new MutableLiveData<>("");

    private FootballRepository(Context context) {
        HouseManagerDatabase db = HouseManagerDatabase.getInstance(context);
        playerDao = db.playerDao();
        teamDao = db.teamDao();
        leagueDao = db.leagueDao();
        lineupDao = db.lineupDao();
        matchDao = db.matchDao();
        marketDao = db.marketDao();
        ownershipDao = db.ownershipDao();
        marketStateDao = db.marketStateDao();
        captainDao = db.captainDao();
        playerMatchPointsDao = db.playerMatchPointsDao();
        matchEventDao = db.matchEventDao();
        lineupEntryDao = db.lineupEntryDao();
        playerPointsHistoryDao = db.playerPointsHistoryDao();
        apiService = ApiClient.getClient().create(FootballApiService.class);
        executor = Executors.newFixedThreadPool(3);
        syncPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Asegurar partidos próximos persistidos en primer arranque (sin tocar UI)
        executor.execute(() -> {
            try {
                // Si no hay partidos, o no hay próximos a partir de 'ahora', refrescar desde API
                java.util.List<com.example.housemanager.database.entities.MatchEntity> existing = matchDao.getAllSync();
                long now = System.currentTimeMillis();
                int upcoming = matchDao.countUpcomingSync(now);
                if (existing == null || existing.isEmpty() || upcoming == 0) {
                    syncUpcomingMatches(null);
                }
            } catch (Exception ignored) { }
        });
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

    /** Ligas activas para el dashboard. */
    public LiveData<Integer> getActiveLeaguesCount() {
        return leagueDao.countActiveLeagues();
    }

    /** Alineación incompleta para el dashboard. */
    public LiveData<Boolean> isLineupIncomplete(int userId, int matchday) {
        return Transformations.map(
                lineupDao.isLineupIncompleteInt(userId, matchday),
                value -> value != null && value > 0
        );
    }

    /** Equipos listos para UI. */
    public LiveData<List<Team>> getAllTeams() {
        return Transformations.map(
                teamDao.getAllTeamEntities(),
                this::convertTeamEntitiesToMarketTeams
        );
    }

    /** Devuelve LiveData con los próximos 'limit' partidos a partir de 'ahora'. */
    public LiveData<List<MatchEntity>> getUpcomingMatches(int limit) {
        long now = System.currentTimeMillis();
        return matchDao.getUpcoming(now, limit);
    }

    /** Devuelve LiveData de los 10 partidos persistidos de la jornada (para Home). */
    public LiveData<List<MatchEntity>> getMatchdayMatchesLive() {
        return matchDao.getMatchdayMatchesLive();
    }

    /** Devuelve los 10 partidos disponibles ordenados por fecha (tras sync de jornada). */
    public LiveData<List<MatchEntity>> getUpcoming10MatchesLive() {
        return matchDao.getUpcoming10();
    }

    /**
     * Recalcula los puntos de jugadores a partir de los partidos persistidos de una jornada.
     * Regla simple: victoria=3, empate=1, derrota=0, aplicados a todos los jugadores del equipo.
     * Si se desea, se puede resetear antes de aplicar.
     */
    public void recomputePointsFromMatches(int matchday, @Nullable SyncCallback callback) {
        // Descargar y persistir partidos de la jornada y luego calcular puntos
        syncMatchday(matchday, new SyncCallback() {
            @Override
            public void onSuccess() {
                executor.execute(() -> {
                    try {
                        // Reiniciar puntos a 0 para mantener coherencia con la jornada actual
                        playerDao.resetAllPoints();

                        List<MatchEntity> matches = matchDao.getAllSync();
                        // Limpiar puntos por-partido de la jornada
                        playerMatchPointsDao.deleteByMatchday(matchday);
                        for (MatchEntity m : matches) {
                            String status = m.getStatus() != null ? m.getStatus().toUpperCase() : "";
                            Integer hs = m.getHomeScore();
                            Integer as = m.getAwayScore();
                            if (!"FINISHED".equals(status) || hs == null || as == null) continue;

                            int homePts;
                            int awayPts;
                            if (hs > as) { homePts = 3; awayPts = 0; }
                            else if (hs.equals(as)) { homePts = 1; awayPts = 1; }
                            else { homePts = 0; awayPts = 3; }

                            playerDao.addPointsToTeam((int) m.getHomeTeamId(), homePts);
                            playerDao.addPointsToTeam((int) m.getAwayTeamId(), awayPts);
                        }
                        if (callback != null) runOnMainThread(callback::onSuccess);
                    } catch (Exception e) {
                        if (callback != null) runOnMainThread(() -> callback.onError(e));
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                if (callback != null) callback.onError(t);
            }

            @Override
            public void onProgress(String message, int current, int total) { }
        });
    }

    /** Estado del mercado por liga (para countdown en UI). */
    public LiveData<com.example.housemanager.database.entities.MarketState> getMarketStateLive(long leagueId) {
        return marketStateDao.getLive(leagueId);
    }

    /** Clasificación por liga (LiveData) sumando puntos de jugadores por propietario. */
    public LiveData<List<ManagerScore>> getLeagueClassification(long leagueId) {
        LiveData<List<ManagerScoreRow>> rows = ownershipDao.getLeagueClassification(leagueId);
        return Transformations.map(rows, list -> {
            List<ManagerScore> out = new ArrayList<>();
            if (list == null) return out;
            for (ManagerScoreRow r : list) {
                out.add(new ManagerScore(r.getUserId(), r.getTotalPoints()));
            }
            return out;
        });
    }

    /** Clasificación de la jornada actual (aplica capitán x2 sobre puntos de ese partido). */
    public LiveData<List<ManagerScore>> getLeagueClassificationThisMatchday(long leagueId, int matchday) {
        LiveData<List<ManagerScoreRow>> rows = ownershipDao.getLeagueClassificationThisMatchday(leagueId, matchday);
        return Transformations.map(rows, list -> {
            List<ManagerScore> out = new ArrayList<>();
            if (list == null) return out;
            for (ManagerScoreRow r : list) {
                out.add(new ManagerScore(r.getUserId(), r.getTotalPoints()));
            }
            return out;
        });
    }

    /** Clasificación de la temporada (suma totalPoints + bonus capitán sobre su total). */
    public LiveData<List<ManagerScore>> getLeagueClassificationSeason(long leagueId) {
        LiveData<List<ManagerScoreRow>> rows = ownershipDao.getLeagueClassificationSeason(leagueId);
        return Transformations.map(rows, list -> {
            List<ManagerScore> out = new ArrayList<>();
            if (list == null) return out;
            for (ManagerScoreRow r : list) {
                out.add(new ManagerScore(r.getUserId(), r.getTotalPoints()));
            }
            return out;
        });
    }

    /**
     * Garantiza que el mercado de una liga esté generado una vez al día (10 jugadores no poseídos).
     * Si está expirado (o no existe), limpia e inserta nuevas entradas y actualiza MarketState.
     */
    public void ensureLeagueMarketGenerated(long leagueId, @Nullable SyncCallback callback) {
        executor.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                com.example.housemanager.database.entities.MarketState state = marketStateDao.getSync(leagueId);

                // Si hay estado vigente pero NO hay listados activos, forzar regeneración (arregla carrera al inicio)
                if (state != null && state.getMarketExpiresAtMillis() > now) {
                    try {
                        int active = marketDao.getActiveListingsCountSync(leagueId);
                        if (active == 0) {
                            state = null; // tratar como expirado/inexistente
                        }
                    } catch (Exception ignored) { }
                }

                if (state != null && state.getMarketExpiresAtMillis() > now) {
                    // No expirado y con listados → no hacer nada
                    if (callback != null) runOnMainThread(callback::onSuccess);
                    return;
                }

                // Expirado o inexistente → regenerar
                marketDao.clearLeagueMarket(leagueId);

                // Obtener todos los jugadores no poseídos
                List<PlayerEntity> candidates = playerDao.getAllNotOwnedSync(leagueId);
                java.util.Collections.shuffle(candidates);
                int count = Math.min(10, candidates.size());
                List<com.example.housemanager.database.entities.MarketListing> listings = new ArrayList<>();
                long listedAt = now;
                // Calcular expiración según la configuración de la liga (HH:mm); fallback a fin de día
                long expiresAt;
                try {
                    com.example.housemanager.database.entities.LeagueEntity league = leagueDao.getByIdSync(leagueId);
                    if (league != null && league.getMarketHour() != null) {
                        expiresAt = nextMarketResetMillis(league.getMarketHour());
                    } else {
                        expiresAt = endOfTodayMillis();
                    }
                } catch (Exception ex) {
                    expiresAt = endOfTodayMillis();
                }
                for (int i = 0; i < count; i++) {
                    PlayerEntity p = candidates.get(i);
                    com.example.housemanager.database.entities.MarketListing ml = new com.example.housemanager.database.entities.MarketListing();
                    ml.setLeagueId(leagueId);
                    ml.setPlayerId(p.getPlayerId());
                    ml.setListedAtMillis(listedAt);
                    ml.setExpiresAtMillis(expiresAt);
                    ml.setSold(false);
                    listings.add(ml);
                }
                if (!listings.isEmpty()) {
                    marketDao.insertAll(listings);

                    // Actualizar estado del mercado sólo si hay listados
                    com.example.housemanager.database.entities.MarketState newState = new com.example.housemanager.database.entities.MarketState();
                    newState.setLeagueId(leagueId);
                    newState.setLastGeneratedAtMillis(now);
                    newState.setMarketExpiresAtMillis(expiresAt);
                    marketStateDao.upsert(newState);
                }

                if (callback != null) runOnMainThread(callback::onSuccess);
            } catch (Exception e) {
                if (callback != null) runOnMainThread(() -> callback.onError(e));
            }
        });
    }

    /** Devuelve jugadores del mercado actual de una liga, uniendo listings con PlayerEntity. */
    public LiveData<List<Player>> getLeagueMarketPlayers(long leagueId) {
        androidx.lifecycle.MediatorLiveData<List<Player>> out = new androidx.lifecycle.MediatorLiveData<>();
        LiveData<List<com.example.housemanager.database.entities.MarketListing>> source = marketDao.getLiveListings(leagueId);
        out.addSource(source, listings -> {
            executor.execute(() -> {
                try {
                    List<Player> result = new ArrayList<>();
                    if (listings != null && !listings.isEmpty()) {
                        List<Integer> ids = new ArrayList<>();
                        for (com.example.housemanager.database.entities.MarketListing ml : listings) {
                            ids.add((int) ml.getPlayerId());
                        }
                        List<PlayerEntity> entities = playerDao.getByIdsSync(ids);
                        // index by id for quick lookup
                        java.util.Map<Integer, PlayerEntity> map = new java.util.HashMap<>();
                        for (PlayerEntity e : entities) map.put(e.getPlayerId(), e);
                        for (com.example.housemanager.database.entities.MarketListing ml : listings) {
                            PlayerEntity e = map.get((int) ml.getPlayerId());
                            if (e != null) {
                                result.add(convertEntityToMarketPlayer(e));
                            }
                        }
                    }
                    out.postValue(result);
                } catch (Exception ex) {
                    out.postValue(new ArrayList<>());
                }
            });
        });
        return out;
    }

    /** Compra en liga: marca ownership y listing vendido. No repone. */
    public void buyPlayer(long leagueId, int playerId, long ownerUserId, @Nullable SyncCallback callback) {
        executor.execute(() -> {
            try {
                // Insertar propiedad en la liga
                com.example.housemanager.database.entities.LeaguePlayerOwnership own = new com.example.housemanager.database.entities.LeaguePlayerOwnership();
                own.setLeagueId(leagueId);
                own.setPlayerId(playerId);
                own.setOwnerUserId(ownerUserId);
                own.setAcquiredPrice(0); // si no hay precio de compra almacenado aún
                own.setAcquiredAtMillis(System.currentTimeMillis());
                ownershipDao.insert(own);

                // Marcar listing como vendido
                marketDao.markSold(leagueId, playerId);

                // Opcional: también marcar no disponible globalmente si aplica a un pool global (mantener actual comportamiento)
                playerDao.markAsBought(playerId);

                if (callback != null) runOnMainThread(callback::onSuccess);
            } catch (Exception e) {
                if (callback != null) runOnMainThread(() -> callback.onError(e));
            }
        });
    }

    private long endOfTodayMillis() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    /** Calcula el próximo instante (hoy o mañana) a la hora HH:mm indicada por la liga. */
    private long nextMarketResetMillis(String marketHour) {
        try {
            String hhmm = marketHour != null ? marketHour.trim() : "14:00";
            String[] parts = hhmm.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            java.util.Calendar now = java.util.Calendar.getInstance();
            java.util.Calendar target = (java.util.Calendar) now.clone();
            target.set(java.util.Calendar.SECOND, 0);
            target.set(java.util.Calendar.MILLISECOND, 0);
            target.set(java.util.Calendar.HOUR_OF_DAY, hour);
            target.set(java.util.Calendar.MINUTE, minute);
            if (target.getTimeInMillis() <= now.getTimeInMillis()) {
                target.add(java.util.Calendar.DAY_OF_YEAR, 1);
            }
            return target.getTimeInMillis();
        } catch (Exception e) {
            return endOfTodayMillis();
        }
    }

    /** Obtiene la jornada actual desde la API. */
    public void getCurrentMatchday(@Nullable MatchdayCallback callback) {
        apiService.getCompetition().enqueue(new Callback<CompetitionAPI>() {
            @Override
            public void onResponse(Call<CompetitionAPI> call, Response<CompetitionAPI> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCurrentSeason() != null
                        && response.body().getCurrentSeason().getCurrentMatchday() != null) {
                    int md = response.body().getCurrentSeason().getCurrentMatchday();
                    if (callback != null) callback.onResult(md);
                } else {
                    // Fallback: deducir jornada de próximos 7 días (primer SCHEDULED)
                    String from = buildIsoDate(0);
                    String to = buildIsoDate(7);
                    apiService.getMatches(from, to).enqueue(new Callback<MatchesResponse>() {
                        @Override
                        public void onResponse(Call<MatchesResponse> call2, Response<MatchesResponse> resp2) {
                            if (resp2.isSuccessful() && resp2.body() != null && resp2.body().getMatches() != null) {
                                for (MatchAPI m : resp2.body().getMatches()) {
                                    if (m != null && "SCHEDULED".equalsIgnoreCase(m.getStatus()) && m.getMatchday() != null) {
                                        if (callback != null) callback.onResult(m.getMatchday());
                                        return;
                                    }
                                }
                            }
                            if (callback != null) callback.onError(new IllegalStateException("No matchday available"));
                        }

                        @Override
                        public void onFailure(Call<MatchesResponse> call2, Throwable t) {
                            if (callback != null) callback.onError(t);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<CompetitionAPI> call, Throwable t) {
                if (callback != null) callback.onError(t);
            }
        });
    }

    /** Sincroniza y persiste todos los partidos de una jornada específica. */
    public void syncMatchday(int matchday, @Nullable SyncCallback callback) {
        apiService.getMatchesByMatchday(matchday).enqueue(new Callback<MatchesResponse>() {
            @Override
            public void onResponse(Call<MatchesResponse> call, Response<MatchesResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getMatches() == null) {
                    if (callback != null) callback.onError(new IllegalStateException("Respuesta inválida"));
                    return;
                }
                List<MatchAPI> apiMatches = response.body().getMatches();
                executor.execute(() -> {
                    try {
                        List<MatchEntity> entities = convertApiMatchesToEntities(apiMatches);
                        matchDao.deleteAll();
                        if (!entities.isEmpty()) matchDao.insertAll(entities);
                        // tras persistir partidos de la jornada, recalcular puntos para todos los FINISHED existentes
                        recomputePointsForAllFinishedMatches(null);
                        if (callback != null) runOnMainThread(callback::onSuccess);
                    } catch (Exception e) {
                        if (callback != null) runOnMainThread(() -> callback.onError(e));
                    }
                });
            }

            @Override
            public void onFailure(Call<MatchesResponse> call, Throwable t) {
                if (callback != null) callback.onError(t);
            }
        });
    }

    /** Sincroniza partidos próximos de la semana desde la API y los guarda en Room. */
    public void syncUpcomingMatches(@Nullable SyncCallback callback) {
        // Construir rango [hoy .. hoy+7] en ISO yyyy-MM-dd
        Log.d(TAG, "Descargando próximos partidos...");
        String from = buildIsoDate(0);
        String to = buildIsoDate(7);
        Log.d(TAG, "Descargando próximos partidos: " + from + " -> " + to);

        apiService.getMatches(from, to).enqueue(new Callback<MatchesResponse>() {
            @Override
            public void onResponse(Call<MatchesResponse> call, Response<MatchesResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getMatches() == null) {
                    Log.w(TAG, "Respuesta inválida en getMatches");
                    if (callback != null) callback.onError(new IllegalStateException("Respuesta inválida"));
                    return;
                }
                List<MatchAPI> apiMatches = response.body().getMatches();
                Log.d(TAG, "[FootballRepository] Sync partidos: recibidos " + (apiMatches != null ? apiMatches.size() : 0));
                executor.execute(() -> {
                    try {
                        List<MatchEntity> entities = convertApiMatchesToEntities(apiMatches);
                        matchDao.deleteAll();
                        if (!entities.isEmpty()) matchDao.insertAll(entities);
                        Log.d(TAG, "Partidos guardados en Room: " + entities.size());
                        if (callback != null) runOnMainThread(callback::onSuccess);
                    } catch (Exception e) {
                        Log.e(TAG, "Error guardando partidos", e);
                        if (callback != null) runOnMainThread(() -> callback.onError(e));
                    }
                });
            }

            @Override
            public void onFailure(Call<MatchesResponse> call, Throwable t) {
                Log.e(TAG, "Fallo getMatches", t);
                if (callback != null) callback.onError(t);
            }
        });
    }

    /** Jugadores listos para UI. */
    public LiveData<List<Player>> getAllPlayers() {
        return Transformations.map(
                playerDao.getAllPlayerEntities(),
                this::convertPlayerEntitiesToMarketPlayers
        );
    }

    /** 10 jugadores aleatorios disponibles para el mercado (legacy). */
    public LiveData<List<Player>> getRandomMarketPlayers() {
        return Transformations.map(
                playerDao.getRandomAvailablePlayers(),
                this::convertPlayerEntitiesToMarketPlayers
        );
    }

    /** Mercado diario fijo: asegura selección del día y expone los disponibles ordenados por nombre. */
    public LiveData<List<Player>> getMarketPlayers() {
        // Asegurar selección diaria en background (no bloquea la observación de LiveData)
        ensureDailyMarketSelectionIfNeeded();
        return Transformations.map(
                playerDao.getMarketToday(),
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

    /** Jugadores propiedad del usuario en una liga, en formato PlayerAPI para UI de Mi Equipo. */
    public LiveData<List<PlayerAPI>> getMyTeamApiPlayers(long leagueId, long ownerUserId) {
        androidx.lifecycle.MediatorLiveData<List<PlayerAPI>> out = new androidx.lifecycle.MediatorLiveData<>();
        LiveData<java.util.List<Long>> ownedIdsLive = ownershipDao.getOwnedPlayerIdsLive(leagueId, ownerUserId);
        out.addSource(ownedIdsLive, ids -> {
            executor.execute(() -> {
                List<PlayerAPI> result = new ArrayList<>();
                try {
                    if (ids != null && !ids.isEmpty()) {
                        // Convert List<Long> to List<Integer> for DAO
                        List<Integer> intIds = new ArrayList<>();
                        for (Long l : ids) intIds.add(l.intValue());
                        List<PlayerEntity> entities = playerDao.getByIdsSync(intIds);
                        for (PlayerEntity e : entities) {
                            PlayerAPI dto = new PlayerAPI();
                            dto.setId(e.getPlayerId());
                            dto.setName(e.getName());
                            dto.setPosition(e.getPosition());
                            dto.setNationality(e.getNationality());
                            dto.setPoints(e.getTotalPoints());
                            result.add(dto);
                        }
                    }
                } catch (Exception ignored) { }
                out.postValue(result);
            });
        });
        return out;
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

    /** Jugadores por posición para el mercado. */
    public LiveData<List<Player>> getPlayersByPosition(String position) {
        if (position == null) return getAllPlayers();
        String p = position.trim().toLowerCase();
        LiveData<List<PlayerEntity>> source;
        switch (p) {
            case "portero":
            case "gk":
            case "goalkeeper":
                source = playerDao.getAvailablePorteros();
                break;
            case "defensa":
            case "df":
            case "defender":
                source = playerDao.getAvailableDefensas();
                break;
            case "medio":
            case "mf":
            case "midfielder":
                source = playerDao.getAvailableMedios();
                break;
            case "delantero":
            case "fw":
            case "forward":
                source = playerDao.getAvailableDelanteros();
                break;
            default:
                return getAllPlayers();
        }
        return Transformations.map(source, this::convertPlayerEntitiesToMarketPlayers);
    }

    /** Compra un jugador (lo marca como no disponible). */
    public void buyPlayer(int playerId, @Nullable SyncCallback callback) {
        executor.execute(() -> {
            try {
                // Asignar propiedad por defecto a la liga 1 y usuario 1 (alineado con MyTeamActivity)
                long leagueId = 1L;
                long ownerUserId = 1L;

                // Insertar propiedad en la liga
                com.example.housemanager.database.entities.LeaguePlayerOwnership own = new com.example.housemanager.database.entities.LeaguePlayerOwnership();
                own.setLeagueId(leagueId);
                own.setPlayerId(playerId);
                own.setOwnerUserId(ownerUserId);
                own.setAcquiredPrice(0);
                own.setAcquiredAtMillis(System.currentTimeMillis());
                ownershipDao.insert(own);

                // Marcar listing como vendido si existía en el mercado de la liga 1
                try { marketDao.markSold(leagueId, playerId); } catch (Exception ignored) { }

                // Marcar jugador como no disponible (no reponer)
                playerDao.markAsBought(playerId);
                if (callback != null) runOnMainThread(callback::onSuccess);
            } catch (Exception e) {
                if (callback != null) runOnMainThread(() -> callback.onError(e));
            }
        });
    }

    /**
     * Compra un jugador asignándolo a un propietario si la BD lo soporta.
     * Nota: Actualmente PlayerEntity no tiene owner_id; cumplimos el requisito mínimo marcando available=0.
     */
    public void buyPlayer(int playerId, long ownerId, @Nullable SyncCallback callback) {
        executor.execute(() -> {
            try {
                // No hay columna owner_id en la tabla; actualizar solo disponibilidad del jugador comprado
                playerDao.markAsBought(playerId);
                if (callback != null) runOnMainThread(callback::onSuccess);
            } catch (Exception e) {
                if (callback != null) runOnMainThread(() -> callback.onError(e));
            }
        });
    }

    /** Vende un jugador (lo marca como disponible). */
    public void sellPlayer(int playerId, @Nullable SyncCallback callback) {
        executor.execute(() -> {
            try {
                playerDao.markAsAvailable(playerId);
                if (callback != null) runOnMainThread(callback::onSuccess);
            } catch (Exception e) {
                if (callback != null) runOnMainThread(() -> callback.onError(e));
            }
        });
    }

    /** Fuerza sincronización completa desde API. */
    public void forceSyncFromAPI(@Nullable SyncCallback callback) {
        isSyncing.postValue(true);
        syncStatus.postValue("Forzando sincronización...");
        executor.execute(() -> {
            try {
                // Limpieza básica para garantizar datos frescos
                teamDao.deleteAllTeams();
                playerDao.deleteAllPlayers();
                performFullSync(callback);
            } catch (Exception e) {
                handleSyncError(e, callback);
            }
        });
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
                    handleSyncError(new IllegalStateException("Respuesta inválida de equipos"), callback);
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
                handleSyncError(t, callback);
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

                // Marcar como no disponibles a los que no lleguen en la API (bajas), sin borrar tabla
                if (allPlayers != null && !allPlayers.isEmpty()) {
                    java.util.List<Integer> ids = new java.util.ArrayList<>();
                    for (PlayerEntity p : allPlayers) ids.add(p.getPlayerId());
                    try { playerDao.markUnavailableNotInIds(ids); } catch (Exception ignored) { }

                    // Inserta ignorando (no tocará los existentes)
                    long[] ins = playerDao.insertIgnore(allPlayers);
                    long now = System.currentTimeMillis();
                    // Para los que ya existían (id = -1), actualizar SOLO campos de ficha
                    for (int i = 0; i < allPlayers.size(); i++) {
                        if (ins != null && i < ins.length && ins[i] == -1L) {
                            PlayerEntity e = allPlayers.get(i);
                            playerDao.updateFromApiWithoutPoints(
                                    e.getPlayerId(),
                                    e.getName(),
                                    e.getTeamId(),
                                    e.getTeamName(),
                                    e.getPosition(),
                                    e.getNationality(),
                                    e.getCurrentPrice(),
                                    now
                            );
                        }
                    }
                }

                Log.d(TAG, "Jugadores guardados/actualizados en Room: " + (allPlayers != null ? allPlayers.size() : 0));

                markSyncCompleted(allPlayers != null ? allPlayers.size() : 0);
                syncStatus.postValue("Listo");
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
        // Upsert: no borrar la tabla entera
        if (!entities.isEmpty()) teamDao.insertTeams(entities);
        Log.d(TAG, "Equipos guardados/actualizados en Room: " + entities.size());
    }

    /** Convierte DTOs de API a entidades de DB. */
    private List<PlayerEntity> convertApiPlayersToEntities(List<PlayerAPI> apiPlayers, TeamAPI team) {
        List<PlayerEntity> out = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (PlayerAPI p : apiPlayers) {
            PlayerEntity e = new PlayerEntity();
            e.setPlayerId(p.getId());
            e.setName(p.getName() != null ? p.getName() : "Jugador");
            e.setTeamId(team.getId());
            e.setTeamName(team.getName() != null ? team.getName() : "");
            e.setPosition(translatePositionToSpanish(p.getPosition()));
            e.setNationality(p.getNationality() != null ? p.getNationality() : "España");
            e.setCurrentPrice(calculatePlayerPrice(e.getPosition()));
            // No tocar totalPoints aquí: evitar sobrescribir puntos persistidos.
            e.setAvailable(true);
            e.setUpdatedAt(now);
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

    // Se eliminó la generación aleatoria de puntos. PlayerEntity.totalPoints es la única fuente de verdad
    // y se inicializa a 0 si no tenemos puntos reales provenientes de cálculos posteriores.

    /** Marca fin de sincronización en preferencias. */
    private void markSyncCompleted(int playersCount) {
        syncPrefs.edit()
                .putLong(PREF_LAST_SYNC, System.currentTimeMillis())
                .putBoolean(PREF_TEAMS_SYNCED, true)
                .putInt(PREF_PLAYERS_SYNCED, playersCount)
                .apply();
    }

    /** Construye una fecha ISO (yyyy-MM-dd) en UTC sumando daysOffset desde hoy. */
    private String buildIsoDate(int daysOffset) {
        java.time.LocalDate date = java.time.LocalDate.now(java.time.ZoneOffset.UTC).plusDays(daysOffset);
        return date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /** Convierte una lista de MatchAPI a entidades Room. */
    private List<MatchEntity> convertApiMatchesToEntities(List<MatchAPI> apiMatches) {
        List<MatchEntity> out = new ArrayList<>();
        if (apiMatches == null) return out;
        for (MatchAPI m : apiMatches) {
            try {
                MatchEntity e = new MatchEntity();
                e.setMatchId(m.getId());
                if (m.getHomeTeam() != null) {
                    e.setHomeTeamId(m.getHomeTeam().getId());
                    e.setHomeTeamName(m.getHomeTeam().getName());
                }
                if (m.getAwayTeam() != null) {
                    e.setAwayTeamId(m.getAwayTeam().getId());
                    e.setAwayTeamName(m.getAwayTeam().getName());
                }
                String utc = m.getUtcDate();
                long millis = 0L;
                if (utc != null && !utc.isEmpty()) {
                    try {
                        java.time.Instant ins = java.time.OffsetDateTime.parse(utc).toInstant();
                        millis = ins.toEpochMilli();
                    } catch (Exception exParse) {
                        // Fallback a parser robusto
                        millis = parseIsoToMillis(utc);
                        if (millis <= 0L) {
                            Log.w(TAG, "utcDate inválido: " + utc, exParse);
                            millis = 0L;
                        }
                    }
                }
                e.setUtcDateMillis(millis);
                e.setStatus(m.getStatus());
                Integer home = null;
                Integer away = null;
                ScoreAPI score = m.getScore();
                if (score != null && score.getFullTime() != null) {
                    home = score.getFullTime().getHome();
                    away = score.getFullTime().getAway();
                }
                e.setHomeScore(home);
                e.setAwayScore(away);
                out.add(e);
            } catch (Exception ex) {
                Log.w(TAG, "Error convirtiendo partido id=" + (m != null ? m.getId() : -1), ex);
            }
        }
        return out;
    }

    /** Parsea un ISO 8601 de football-data a epoch millis (UTC). */
    private long parseIsoToMillis(String iso) {
        if (iso == null || iso.isEmpty()) return 0L;
        try {
            // e.g., 2025-08-23T12:34:56Z
            return java.time.Instant.parse(iso).toEpochMilli();
        } catch (Exception e1) {
            try {
                // e.g., 2025-08-23T12:34:56+00:00 or with offset
                return java.time.OffsetDateTime.parse(iso, java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        .toInstant().toEpochMilli();
            } catch (Exception e2) {
                try {
                    // e.g., 2025-08-23T12:34 (assume UTC)
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(iso,
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                    return ldt.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
                } catch (Exception e3) {
                    return 0L;
                }
            }
        }
    }

    /** Recalcula puntos para todos los partidos FINISHED presentes en Room usando eventos y alineaciones persistidos. */
    public void recomputePointsForAllFinishedMatches(@Nullable SyncCallback callback) {
        executor.execute(() -> {
            try {
                List<MatchEntity> all = matchDao.getAllSync();
                if (all != null) {
                    for (MatchEntity m : all) {
                        if (m != null) {
                            String st = m.getStatus();
                            if (st != null && st.equalsIgnoreCase("FINISHED")) {
                                recomputePointsForMatchInternal(m);
                            }
                        }
                    }
                }
                if (callback != null) runOnMainThread(callback::onSuccess);
            } catch (Exception e) {
                if (callback != null) runOnMainThread(() -> callback.onError(e));
            }
        });
    }

    /** Alias para especificación: recalcula si es necesario (idempotente). */
    public void recomputePointsForAllFinishedMatchesIfNeeded() {
        recomputePointsForAllFinishedMatches(null);
    }

    private void recomputePointsForMatchInternal(MatchEntity match) {
        try {
            // Solo calcular si el partido está FINISHED
            if (match == null || match.getStatus() == null || !"FINISHED".equalsIgnoreCase(match.getStatus())) {
                return;
            }
            long matchId = match.getMatchId();
            // Cargar alineaciones y eventos de Room
            List<com.example.housemanager.database.entities.LineupEntryEntity> lineup = lineupEntryDao.getByMatch(matchId);
            List<com.example.housemanager.database.entities.MatchEventEntity> events = matchEventDao.getByMatch(matchId);

            // Construir conjunto de jugadores implicados (alineación o eventos)
            java.util.Set<Integer> playerIds = new java.util.HashSet<>();
            if (lineup != null) {
                for (com.example.housemanager.database.entities.LineupEntryEntity le : lineup) {
                    if (le != null) playerIds.add(le.getPlayerId());
                }
            }
            if (events != null) {
                for (com.example.housemanager.database.entities.MatchEventEntity ev : events) {
                    if (ev != null) playerIds.add(ev.getPlayerId());
                }
            }

            if (playerIds.isEmpty()) {
                return; // sin jugadores, nada que recalcular (no inventamos)
            }

            // Limpiar historial previo de ese partido e insertar de nuevo
            playerPointsHistoryDao.clearByMatch(matchId);

            // Obtener datos de jugadores para posición/teamId
            java.util.List<Integer> idsList = new java.util.ArrayList<>(playerIds);
            List<PlayerEntity> entities = playerDao.getByIdsSync(idsList);
            java.util.Map<Integer, PlayerEntity> byId = new java.util.HashMap<>();
            if (entities != null) {
                for (PlayerEntity e : entities) byId.put(e.getPlayerId(), e);
            }

            for (Integer pid : playerIds) {
                PlayerEntity pe = byId.get(pid);
                if (pe == null) continue; // jugador no conocido en Room
                int pts = pointsCalculator.computeForPlayerInMatch(
                        pid,
                        pe.getPosition(),
                        pe.getTeamId(),
                        match,
                        events,
                        lineup
                );

                com.example.housemanager.database.entities.PlayerPointsHistoryEntity rec = new com.example.housemanager.database.entities.PlayerPointsHistoryEntity();
                rec.setMatchId(matchId);
                rec.setPlayerId(pid);
                rec.setPoints(pts);
                playerPointsHistoryDao.insert(rec);

                // Recalcular total acumulado del jugador
                Integer total = playerPointsHistoryDao.getTotalForPlayer(pid);
                int finalTotal = (total != null ? total : 0);
                playerDao.updateTotalPoints(pid, finalTotal);
                Log.d(TAG, "updateTotalPoints playerId=" + pid + " total=" + finalTotal);
            }
        } catch (Exception ignored) {
        }
    }

    /** Sincroniza jornada actual, guarda partidos + alineaciones + eventos y recalcula puntos acumulados. */
    public void syncAndRecalculatePointsForCurrentMatchday(@Nullable SyncCallback callback) {
        executor.execute(() -> {
            try {
                // 1) Obtener jornada actual
                int md;
                try {
                    retrofit2.Response<com.example.housemanager.api.models.CompetitionDetails> compResp = apiService.getCompetitionDetails().execute();
                    Integer curr = (compResp.isSuccessful() && compResp.body() != null && compResp.body().getCurrentSeason() != null)
                            ? compResp.body().getCurrentSeason().getCurrentMatchday()
                            : null;
                    md = (curr != null) ? curr : 1;
                } catch (Exception e) {
                    md = 1; // fallback
                }

                // 2) Descargar partidos por jornada y persistir
                retrofit2.Response<MatchesResponse> matchesResp = apiService.getMatchesByMatchday(md).execute();
                if (!matchesResp.isSuccessful() || matchesResp.body() == null) {
                    throw new IllegalStateException("No se pudieron obtener los partidos de la jornada " + md);
                }
                List<MatchAPI> apiMatches = matchesResp.body().getMatches();
                List<MatchEntity> entities = convertApiMatchesToEntities(apiMatches);
                matchDao.deleteAll();
                if (!entities.isEmpty()) matchDao.insertAll(entities);

                // 3) Para cada partido, descargar detalle (si disponible), persistir lineups y eventos
                for (MatchEntity m : entities) {
                    try {
                        retrofit2.Response<com.example.housemanager.api.models.MatchDetailResponse> detResp = apiService.getMatchDetail(m.getMatchId()).execute();
                        if (detResp.isSuccessful() && detResp.body() != null) {
                            com.example.housemanager.api.models.MatchDetailResponse d = detResp.body();
                            // Persistir lineups
                            lineupEntryDao.clearByMatch(m.getMatchId());
                            java.util.List<com.example.housemanager.database.entities.LineupEntryEntity> entries = new java.util.ArrayList<>();
                            java.util.List<Integer> idsToResolve = new java.util.ArrayList<>();
                            if (d.getHomeTeamLineup() != null) {
                                for (com.example.housemanager.api.models.LineupEntryAPI le : d.getHomeTeamLineup().getStartXI()) {
                                    if (le != null && le.getPlayer() != null) {
                                        com.example.housemanager.database.entities.LineupEntryEntity e = new com.example.housemanager.database.entities.LineupEntryEntity();
                                        e.setMatchId(m.getMatchId());
                                        e.setPlayerId(le.getPlayer().getId());
                                        e.setTeamId((int) m.getHomeTeamId());
                                        e.setRole("STARTER");
                                        entries.add(e);
                                        idsToResolve.add(le.getPlayer().getId());
                                    }
                                }
                                for (com.example.housemanager.api.models.LineupEntryAPI le : d.getHomeTeamLineup().getSubstitutes()) {
                                    if (le != null && le.getPlayer() != null) {
                                        com.example.housemanager.database.entities.LineupEntryEntity e = new com.example.housemanager.database.entities.LineupEntryEntity();
                                        e.setMatchId(m.getMatchId());
                                        e.setPlayerId(le.getPlayer().getId());
                                        e.setTeamId((int) m.getHomeTeamId());
                                        e.setRole("SUB");
                                        entries.add(e);
                                        idsToResolve.add(le.getPlayer().getId());
                                    }
                                }
                            }
                            if (d.getAwayTeamLineup() != null) {
                                for (com.example.housemanager.api.models.LineupEntryAPI le : d.getAwayTeamLineup().getStartXI()) {
                                    if (le != null && le.getPlayer() != null) {
                                        com.example.housemanager.database.entities.LineupEntryEntity e = new com.example.housemanager.database.entities.LineupEntryEntity();
                                        e.setMatchId(m.getMatchId());
                                        e.setPlayerId(le.getPlayer().getId());
                                        e.setTeamId((int) m.getAwayTeamId());
                                        e.setRole("STARTER");
                                        entries.add(e);
                                        idsToResolve.add(le.getPlayer().getId());
                                    }
                                }
                                for (com.example.housemanager.api.models.LineupEntryAPI le : d.getAwayTeamLineup().getSubstitutes()) {
                                    if (le != null && le.getPlayer() != null) {
                                        com.example.housemanager.database.entities.LineupEntryEntity e = new com.example.housemanager.database.entities.LineupEntryEntity();
                                        e.setMatchId(m.getMatchId());
                                        e.setPlayerId(le.getPlayer().getId());
                                        e.setTeamId((int) m.getAwayTeamId());
                                        e.setRole("SUB");
                                        entries.add(e);
                                        idsToResolve.add(le.getPlayer().getId());
                                    }
                                }
                            }
                            if (!entries.isEmpty()) {
                                lineupEntryDao.insertAll(entries);
                            }

                            // Persistir eventos
                            matchEventDao.clearByMatch(m.getMatchId());
                            java.util.List<com.example.housemanager.database.entities.MatchEventEntity> evs = new java.util.ArrayList<>();
                            if (d.getEvents() != null) {
                                for (com.example.housemanager.api.models.MatchEventAPI ev : d.getEvents()) {
                                    if (ev == null || ev.getPlayer() == null) continue;
                                    String t = ev.getType() != null ? ev.getType().toUpperCase() : "";
                                    String mapped;
                                    if (t.contains("GOAL")) mapped = "GOAL";
                                    else if (t.contains("YELLOW")) mapped = "YELLOW";
                                    else if (t.contains("RED")) mapped = "RED";
                                    else continue;
                                    com.example.housemanager.database.entities.MatchEventEntity me = new com.example.housemanager.database.entities.MatchEventEntity();
                                    me.setMatchId(m.getMatchId());
                                    me.setPlayerId(ev.getPlayer().getId());
                                    // teamId: intentar resolver por PlayerEntity
                                    java.util.List<Integer> oneId = java.util.Collections.singletonList(ev.getPlayer().getId());
                                    java.util.List<PlayerEntity> pel = playerDao.getByIdsSync(oneId);
                                    if (pel != null && !pel.isEmpty()) me.setTeamId(pel.get(0).getTeamId());
                                    me.setType(mapped);
                                    me.setMinute(null);
                                    evs.add(me);
                                    idsToResolve.add(ev.getPlayer().getId());
                                }
                            }
                            if (!evs.isEmpty()) matchEventDao.insertAll(evs);

                            // 4) Recalcular puntos de este partido (usa lineup/events y actualiza totales)
                            recomputePointsForMatchInternal(m);
                        }
                    } catch (Exception ignoreOne) {
                        // Si detalle no está disponible, continuar con el siguiente partido
                    }
                }

                if (callback != null) runOnMainThread(callback::onSuccess);
            } catch (Exception e) {
                if (callback != null) runOnMainThread(() -> callback.onError(e));
            }
        });
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
    private enum SyncStrategy { USE_CACHE, FULL_SYNC, PARTIAL_SYNC }

    /** Genera la selección diaria del mercado si el día cambió. */
    private void ensureDailyMarketSelectionIfNeeded() {
        executor.execute(() -> {
            try {
                String today = todayYMD();
                String saved = syncPrefs.getString(PREF_MARKET_DAY, null);
                if (saved != null && saved.equals(today)) return; // ya generado hoy

                // Limpiar selección previa
                try { playerDao.clearMarket(); } catch (Exception ignored) { }

                // Elegir 10 ids aleatorios de TODOS los jugadores
                List<Integer> ids = playerDao.getRandomPlayerIdsSync(10);
                if (ids != null && !ids.isEmpty()) {
                    try { playerDao.markAvailableInIds(ids); } catch (Exception ignored) { }
                }

                // Guardar el día
                syncPrefs.edit().putString(PREF_MARKET_DAY, today).apply();
            } catch (Exception ignored) { }
        });
    }

    private String todayYMD() {
        java.time.ZoneId zone = java.time.ZoneId.systemDefault();
        java.time.LocalDate d = java.time.ZonedDateTime.now(zone).toLocalDate();
        return d.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
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
            out.add(convertEntityToMarketPlayer(e));
        }
        return out;
    }

    /** Convierte una entidad de jugador a modelo de UI. */
    private Player convertEntityToMarketPlayer(PlayerEntity e) {
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
        return p;
    }
}
