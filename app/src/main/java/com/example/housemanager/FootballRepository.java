package com.example.housemanager.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.housemanager.api.ApiClient;
import com.example.housemanager.FootballApiService;
import com.example.housemanager.TeamAPI;
import com.example.housemanager.TeamsResponse;
import com.example.housemanager.PlayerAPI;
import com.example.housemanager.database.HouseManagerDatabase;
import com.example.housemanager.database.dao.TeamDao;
import com.example.housemanager.database.dao.PlayerDao;
import com.example.housemanager.database.entities.Team;
import com.example.housemanager.database.entities.Player;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository que maneja datos de equipos y jugadores
 * Combina datos de API y base de datos local
 * Implementa patrón Repository para separar la lógica de datos
 */
public class FootballRepository {

    private static final String TAG = "FootballRepository";
    private static volatile FootballRepository INSTANCE;

    // DAOs para acceso a base de datos
    private final TeamDao teamDao;
    private final PlayerDao playerDao;
    private final FootballApiService apiService;
    private final ExecutorService executor;

    // LiveData para observar datos
    private final LiveData<List<Team>> allTeams;
    private final LiveData<List<Player>> allPlayers;
    private final LiveData<Integer> teamsCount;
    private final LiveData<Integer> playersCount;

    // Constructor privado para singleton
    private FootballRepository(Context context) {
        HouseManagerDatabase database = HouseManagerDatabase.getDatabase(context);
        teamDao = database.teamDao();
        playerDao = database.playerDao();
        apiService = ApiClient.getFootballApiService();
        executor = Executors.newFixedThreadPool(4);

        // Inicializar LiveData
        allTeams = teamDao.getAllTeams();
        allPlayers = playerDao.getAllPlayers();
        teamsCount = teamDao.getTeamsCountLive();
        playersCount = playerDao.getPlayersCountLive();
    }

    /**
     * Obtiene la instancia singleton del repository
     */
    public static FootballRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (FootballRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FootballRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // ================ EQUIPOS ================

    public LiveData<List<Team>> getAllTeams() {
        return allTeams;
    }

    public LiveData<Team> getTeamById(int teamId) {
        return teamDao.getTeamById(teamId);
    }

    public LiveData<List<Team>> searchTeamsByName(String name) {
        return teamDao.searchTeamsByName(name);
    }

    public LiveData<List<Team>> getTopTeams(int limit) {
        return teamDao.getTopTeams(limit);
    }

    public LiveData<List<Team>> getTeamsByStandings() {
        return teamDao.getTeamsByStandings();
    }

    public LiveData<Integer> getTeamsCount() {
        return teamsCount;
    }

    /**
     * Inserta un equipo en la base de datos
     */
    public void insertTeam(Team team) {
        executor.execute(() -> teamDao.insertTeam(team));
    }

    /**
     * Actualiza un equipo en la base de datos
     */
    public void updateTeam(Team team) {
        executor.execute(() -> teamDao.updateTeam(team));
    }

    // ================ JUGADORES ================

    public LiveData<List<Player>> getAllPlayers() {
        return allPlayers;
    }

    public LiveData<List<Player>> getPlayersByPosition(String position) {
        return playerDao.getPlayersByPosition(position);
    }

    public LiveData<List<Player>> getPlayersByTeam(int teamId) {
        return playerDao.getPlayersByTeam(teamId);
    }

    public LiveData<Player> getPlayerById(int playerId) {
        return playerDao.getPlayerById(playerId);
    }

    public LiveData<List<Player>> getAvailablePlayers() {
        return playerDao.getAvailablePlayers();
    }

    public LiveData<List<Player>> getTopPlayersByPoints(int limit) {
        return playerDao.getTopPlayersByPoints(limit);
    }

    public LiveData<List<Player>> getPlayersByPriceRange(double minPrice, double maxPrice) {
        return playerDao.getPlayersByPriceRange(minPrice, maxPrice);
    }

    public LiveData<List<Player>> searchPlayersByName(String name) {
        return playerDao.searchPlayersByName(name);
    }

    public LiveData<Integer> getPlayersCount() {
        return playersCount;
    }

    public LiveData<Integer> getPlayersCountByPosition(String position) {
        return playerDao.getPlayersCountByPositionLive(position);
    }

    /**
     * Inserta un jugador en la base de datos
     */
    public void insertPlayer(Player player) {
        executor.execute(() -> playerDao.insertPlayer(player));
    }

    /**
     * Actualiza un jugador en la base de datos
     */
    public void updatePlayer(Player player) {
        executor.execute(() -> playerDao.updatePlayer(player));
    }

    // ================ SINCRONIZACIÓN CON API ================

    /**
     * Sincroniza los equipos de La Liga desde la API
     */
    public void syncLaLigaTeams(SyncCallback callback) {
        Log.d(TAG, "Iniciando sincronización de equipos de La Liga...");

        Call<TeamsResponse> call = apiService.getLaLigaTeams();
        call.enqueue(new Callback<TeamsResponse>() {
            @Override
            public void onResponse(Call<TeamsResponse> call, Response<TeamsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TeamAPI> apiTeams = response.body().getTeams();
                    Log.d(TAG, "Equipos recibidos de la API: " + apiTeams.size());

                    // Convertir a entidades Room y guardar en BD
                    executor.execute(() -> {
                        List<Team> teams = convertApiTeamsToEntities(apiTeams);
                        teamDao.insertTeams(teams);
                        Log.d(TAG, "Equipos guardados en base de datos: " + teams.size());

                        // Sincronizar jugadores de cada equipo
                        syncPlayersForAllTeams(teams, callback);
                    });
                } else {
                    Log.e(TAG, "Error en respuesta API equipos: " + response.code());
                    callback.onError("Error al obtener equipos: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TeamsResponse> call, Throwable t) {
                Log.e(TAG, "Error de conexión equipos: " + t.getMessage());
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Sincroniza solo los equipos sin jugadores (más rápido)
     */
    public void syncLaLigaTeamsOnly(SyncCallback callback) {
        Log.d(TAG, "Sincronizando solo equipos de La Liga...");

        Call<TeamsResponse> call = apiService.getLaLigaTeams();
        call.enqueue(new Callback<TeamsResponse>() {
            @Override
            public void onResponse(Call<TeamsResponse> call, Response<TeamsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TeamAPI> apiTeams = response.body().getTeams();

                    executor.execute(() -> {
                        List<Team> teams = convertApiTeamsToEntities(apiTeams);
                        teamDao.insertTeams(teams);
                        callback.onSuccess("Equipos sincronizados: " + teams.size());
                    });
                } else {
                    callback.onError("Error al obtener equipos: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TeamsResponse> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Sincroniza jugadores de un equipo específico
     */
    public void syncTeamPlayers(int teamId, SyncCallback callback) {
        Log.d(TAG, "Sincronizando jugadores del equipo: " + teamId);

        Call<TeamAPI> call = apiService.getTeamDetails(teamId);
        call.enqueue(new Callback<TeamAPI>() {
            @Override
            public void onResponse(Call<TeamAPI> call, Response<TeamAPI> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getSquad() != null) {
                    TeamAPI teamAPI = response.body();

                    executor.execute(() -> {
                        // Obtener el equipo de la BD
                        Team team = teamDao.getTeamByIdSync(teamId);
                        if (team != null) {
                            List<Player> players = convertApiPlayersToEntities(teamAPI.getSquad(), team);
                            playerDao.insertPlayers(players);
                            callback.onSuccess("Jugadores sincronizados: " + players.size());
                        } else {
                            callback.onError("Equipo no encontrado en BD: " + teamId);
                        }
                    });
                } else {
                    callback.onError("Error al obtener jugadores: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TeamAPI> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Sincroniza jugadores de todos los equipos
     */
    private void syncPlayersForAllTeams(List<Team> teams, SyncCallback callback) {
        Log.d(TAG, "Iniciando sincronización de jugadores...");

        final int[] teamsProcessed = {0};
        final List<Player> allPlayers = new ArrayList<>();
        final int totalTeams = teams.size();

        for (Team team : teams) {
            // Pequeña pausa entre llamadas para respetar límite de API
            try {
                Thread.sleep(150); // 150ms entre llamadas
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Call<TeamAPI> call = apiService.getTeamDetails(team.getTeamId());
            call.enqueue(new Callback<TeamAPI>() {
                @Override
                public void onResponse(Call<TeamAPI> call, Response<TeamAPI> response) {
                    teamsProcessed[0]++;

                    if (response.isSuccessful() && response.body() != null && response.body().getSquad() != null) {
                        List<PlayerAPI> apiPlayers = response.body().getSquad();
                        List<Player> teamPlayers = convertApiPlayersToEntities(apiPlayers, team);

                        synchronized (allPlayers) {
                            allPlayers.addAll(teamPlayers);
                        }

                        Log.d(TAG, "Jugadores del " + team.getName() + ": " + teamPlayers.size() +
                                " (Progreso: " + teamsProcessed[0] + "/" + totalTeams + ")");
                    }

                    // Si es el último equipo, guardar todos los jugadores
                    if (teamsProcessed[0] == totalTeams) {
                        executor.execute(() -> {
                            playerDao.insertPlayers(allPlayers);
                            Log.d(TAG, "Total jugadores guardados: " + allPlayers.size());
                            callback.onSuccess("Sincronización completa: " + totalTeams + " equipos y " + allPlayers.size() + " jugadores");
                        });
                    }
                }

                @Override
                public void onFailure(Call<TeamAPI> call, Throwable t) {
                    teamsProcessed[0]++;
                    Log.e(TAG, "Error al obtener jugadores de " + team.getName() + ": " + t.getMessage());

                    // Si es el último equipo, terminar aunque haya errores
                    if (teamsProcessed[0] == totalTeams) {
                        executor.execute(() -> {
                            if (!allPlayers.isEmpty()) {
                                playerDao.insertPlayers(allPlayers);
                                Log.d(TAG, "Jugadores guardados con algunos errores: " + allPlayers.size());
                                callback.onSuccess("Sincronización parcial: " + allPlayers.size() + " jugadores");
                            } else {
                                callback.onError("Error al sincronizar jugadores");
                            }
                        });
                    }
                }
            });
        }
    }

    // ================ CONVERSIONES API → ENTITIES ================

    /**
     * Convierte equipos de la API a entidades Room
     */
    private List<Team> convertApiTeamsToEntities(List<TeamAPI> apiTeams) {
        List<Team> teams = new ArrayList<>();
        int position = 1; // Posición temporal, se actualizará con datos reales

        for (TeamAPI apiTeam : apiTeams) {
            Team team = new Team(
                    apiTeam.getId(),
                    apiTeam.getName(),
                    apiTeam.getShortName(),
                    apiTeam.getTla(),
                    apiTeam.getCrest()
            );

            // Configurar posición temporal
            team.setPosition(position++);
            teams.add(team);
        }
        return teams;
    }

    /**
     * Convierte jugadores de la API a entidades Room
     */
    private List<Player> convertApiPlayersToEntities(List<PlayerAPI> apiPlayers, Team team) {
        List<Player> players = new ArrayList<>();

        for (PlayerAPI apiPlayer : apiPlayers) {
            // Filtrar jugadores sin posición válida
            if (apiPlayer.getPosition() == null || apiPlayer.getPosition().trim().isEmpty()) {
                continue;
            }

            // Convertir posición de API a nuestro formato
            String position = normalizePosition(apiPlayer.getPosition());

            Player player = new Player(
                    apiPlayer.getId(),
                    apiPlayer.getName(),
                    position,
                    team.getTeamId(),
                    team.getName()
            );

            // Configurar datos adicionales
            player.setNationality(apiPlayer.getNationality());
            player.setShirtNumber(apiPlayer.getShirtNumber());
            player.setDateOfBirth(apiPlayer.getDateOfBirth());

            players.add(player);
        }
        return players;
    }

    /**
     * Normaliza las posiciones de la API a nuestro formato estándar
     */
    private String normalizePosition(String apiPosition) {
        if (apiPosition == null) return "MID";

        String pos = apiPosition.toUpperCase().trim();

        // Porteros
        if (pos.contains("GOALKEEPER") || pos.equals("GK")) {
            return "GK";
        }

        // Defensas
        if (pos.contains("DEFENDER") || pos.contains("DEFENCE") ||
                pos.contains("BACK") || pos.contains("DEF") ||
                pos.contains("CENTRE-BACK") || pos.contains("LEFT-BACK") ||
                pos.contains("RIGHT-BACK") || pos.contains("WING-BACK")) {
            return "DEF";
        }

        // Delanteros
        if (pos.contains("FORWARD") || pos.contains("ATTACKER") ||
                pos.contains("STRIKER") || pos.equals("FWD") ||
                pos.contains("CENTRE-FORWARD") || pos.contains("WINGER")) {
            return "FWD";
        }

        // Centrocampistas (por defecto para casos ambiguos)
        return "MID";
    }

    // ================ UTILIDADES ================

    /**
     * Limpia todos los datos de la base de datos
     */
    public void clearAllData() {
        executor.execute(() -> {
            playerDao.deleteAllPlayers();
            teamDao.deleteAllTeams();
            Log.d(TAG, "Todos los datos borrados");
        });
    }

    /**
     * Actualiza los puntos de un jugador
     */
    public void updatePlayerPoints(int playerId, int points) {
        executor.execute(() -> {
            playerDao.addPointsToPlayer(playerId, points);
            Log.d(TAG, "Puntos actualizados para jugador: " + playerId + " (+" + points + ")");
        });
    }

    /**
     * Actualiza el precio de un jugador
     */
    public void updatePlayerPrice(int playerId, double newPrice) {
        executor.execute(() -> {
            playerDao.updatePlayerPrice(playerId, newPrice);
            Log.d(TAG, "Precio actualizado para jugador: " + playerId + " (" + newPrice + "M)");
        });
    }

    /**
     * Marca un jugador como disponible/no disponible
     */
    public void setPlayerAvailability(int playerId, boolean isAvailable) {
        executor.execute(() -> {
            playerDao.setPlayerAvailability(playerId, isAvailable);
            Log.d(TAG, "Disponibilidad actualizada para jugador: " + playerId + " (" + isAvailable + ")");
        });
    }

    /**
     * Obtiene estadísticas de la base de datos
     */
    public void getDatabaseStats(DatabaseStatsCallback callback) {
        executor.execute(() -> {
            int teams = teamDao.getTeamsCount();
            int players = playerDao.getPlayersCount();
            int availablePlayers = playerDao.getAvailablePlayersCount().getValue() != null
                    ? playerDao.getAvailablePlayersCount().getValue() : 0;
            long lastTeamSync = teamDao.getLastSyncTime();
            long lastPlayerSync = playerDao.getLastSyncTime();
            long lastSync = Math.max(lastTeamSync, lastPlayerSync);

            DatabaseStats stats = new DatabaseStats(teams, players, availablePlayers, lastSync);
            callback.onStatsReady(stats);
        });
    }

    /**
     * Verifica si necesita sincronización
     */
    public void checkSyncNeeded(SyncCheckCallback callback) {
        executor.execute(() -> {
            int teamsCount = teamDao.getTeamsCount();
            int playersCount = playerDao.getPlayersCount();

            // Necesita sync si no hay equipos o muy pocos jugadores
            boolean needsSync = teamsCount == 0 || playersCount < 100;

            callback.onSyncCheckComplete(needsSync, teamsCount, playersCount);
        });
    }

    // ================ INTERFACES Y CALLBACKS ================

    /**
     * Callback para operaciones de sincronización
     */
    public interface SyncCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    /**
     * Callback para estadísticas de la base de datos
     */
    public interface DatabaseStatsCallback {
        void onStatsReady(DatabaseStats stats);
    }

    /**
     * Callback para verificación de sincronización
     */
    public interface SyncCheckCallback {
        void onSyncCheckComplete(boolean needsSync, int teamsCount, int playersCount);
    }

    /**
     * Clase para estadísticas de la base de datos
     */
    public static class DatabaseStats {
        public final int teamsCount;
        public final int playersCount;
        public final int availablePlayersCount;
        public final long lastSyncTime;

        public DatabaseStats(int teamsCount, int playersCount, int availablePlayersCount, long lastSyncTime) {
            this.teamsCount = teamsCount;
            this.playersCount = playersCount;
            this.availablePlayersCount = availablePlayersCount;
            this.lastSyncTime = lastSyncTime;
        }

        public String getFormattedLastSync() {
            if (lastSyncTime == 0) return "Nunca";

            long now = System.currentTimeMillis();
            long diff = now - lastSyncTime;

            if (diff < 60000) return "Hace menos de 1 minuto";
            if (diff < 3600000) return "Hace " + (diff / 60000) + " minutos";
            if (diff < 86400000) return "Hace " + (diff / 3600000) + " horas";
            return "Hace " + (diff / 86400000) + " días";
        }

        @Override
        public String toString() {
            return "DatabaseStats{" +
                    "teams=" + teamsCount +
                    ", players=" + playersCount +
                    ", available=" + availablePlayersCount +
                    ", lastSync=" + getFormattedLastSync() +
                    '}';
        }
    }
}