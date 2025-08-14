package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.housemanager.database.entities.Team;
import java.util.List;

@Dao
public interface TeamDao {

    // ================ INSERTAR ================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTeams(List<Team> teams);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTeam(Team team);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTeamIfNotExists(Team team);

    // ================ CONSULTAS BÁSICAS ================

    @Query("SELECT * FROM teams ORDER BY position ASC")
    LiveData<List<Team>> getAllTeams();

    @Query("SELECT * FROM teams ORDER BY position ASC")
    List<Team> getAllTeamsSync();

    @Query("SELECT * FROM teams WHERE team_id = :teamId")
    LiveData<Team> getTeamById(int teamId);

    @Query("SELECT * FROM teams WHERE team_id = :teamId")
    Team getTeamByIdSync(int teamId);

    @Query("SELECT * FROM teams WHERE tla = :tla")
    LiveData<Team> getTeamByTla(String tla);

    @Query("SELECT * FROM teams WHERE tla = :tla")
    Team getTeamByTlaSync(String tla);

    // ================ BÚSQUEDAS ================

    @Query("SELECT * FROM teams WHERE name LIKE '%' || :name || '%' OR short_name LIKE '%' || :name || '%' ORDER BY position ASC")
    LiveData<List<Team>> searchTeamsByName(String name);

    @Query("SELECT * FROM teams WHERE name LIKE '%' || :name || '%' OR short_name LIKE '%' || :name || '%' ORDER BY position ASC")
    List<Team> searchTeamsByNameSync(String name);

    // ================ CLASIFICACIÓN ================

    @Query("SELECT * FROM teams ORDER BY points DESC, goal_difference DESC, goals_for DESC")
    LiveData<List<Team>> getTeamsByStandings();

    @Query("SELECT * FROM teams ORDER BY points DESC, goal_difference DESC, goals_for DESC")
    List<Team> getTeamsByStandingsSync();

    @Query("SELECT * FROM teams ORDER BY points DESC LIMIT :limit")
    LiveData<List<Team>> getTopTeams(int limit);

    @Query("SELECT * FROM teams ORDER BY points ASC LIMIT :limit")
    LiveData<List<Team>> getBottomTeams(int limit);

    // ================ ESTADÍSTICAS ================

    @Query("SELECT * FROM teams ORDER BY goals_for DESC LIMIT :limit")
    LiveData<List<Team>> getTopScoringTeams(int limit);

    @Query("SELECT * FROM teams ORDER BY goals_against ASC LIMIT :limit")
    LiveData<List<Team>> getBestDefensiveTeams(int limit);

    @Query("SELECT * FROM teams WHERE played_games > 0 ORDER BY (CAST(won AS FLOAT) / played_games) DESC LIMIT :limit")
    LiveData<List<Team>> getTeamsByWinPercentage(int limit);

    @Query("SELECT AVG(points) FROM teams WHERE played_games > 0")
    LiveData<Double> getAveragePoints();

    @Query("SELECT AVG(goals_for) FROM teams WHERE played_games > 0")
    LiveData<Double> getAverageGoalsFor();

    @Query("SELECT AVG(goals_against) FROM teams WHERE played_games > 0")
    LiveData<Double> getAverageGoalsAgainst();

    // ================ FILTROS AVANZADOS ================

    @Query("SELECT * FROM teams WHERE points >= :minPoints ORDER BY points DESC")
    LiveData<List<Team>> getTeamsWithMinPoints(int minPoints);

    @Query("SELECT * FROM teams WHERE played_games >= :minGames ORDER BY position ASC")
    LiveData<List<Team>> getTeamsWithMinGames(int minGames);

    @Query("SELECT * FROM teams WHERE goal_difference >= :minDiff ORDER BY goal_difference DESC")
    LiveData<List<Team>> getTeamsWithPositiveGoalDiff(int minDiff);

    @Query("SELECT * FROM teams WHERE won >= :minWins ORDER BY won DESC")
    LiveData<List<Team>> getTeamsWithMinWins(int minWins);

    // ================ ACTUALIZACIONES ================

    @Update
    void updateTeam(Team team);

    @Update
    void updateTeams(List<Team> teams);

    @Query("UPDATE teams SET position = :position WHERE team_id = :teamId")
    void updateTeamPosition(int teamId, int position);

    @Query("UPDATE teams SET points = :points WHERE team_id = :teamId")
    void updateTeamPoints(int teamId, int points);

    @Query("UPDATE teams SET played_games = :games, won = :won, draw = :draw, lost = :lost WHERE team_id = :teamId")
    void updateTeamRecord(int teamId, int games, int won, int draw, int lost);

    @Query("UPDATE teams SET goals_for = :goalsFor, goals_against = :goalsAgainst, goal_difference = :goalDiff WHERE team_id = :teamId")
    void updateTeamGoals(int teamId, int goalsFor, int goalsAgainst, int goalDiff);

    @Query("UPDATE teams SET last_updated = :timestamp WHERE team_id = :teamId")
    void updateLastUpdated(int teamId, long timestamp);

    // ================ ELIMINAR ================

    @Delete
    void deleteTeam(Team team);

    @Delete
    void deleteTeams(List<Team> teams);

    @Query("DELETE FROM teams WHERE team_id = :teamId")
    void deleteTeamById(int teamId);

    @Query("DELETE FROM teams")
    void deleteAllTeams();

    @Query("DELETE FROM teams WHERE last_updated < :timestamp")
    void deleteOldTeams(long timestamp);

    // ================ CONTADORES ================

    @Query("SELECT COUNT(*) FROM teams")
    int getTeamsCount();

    @Query("SELECT COUNT(*) FROM teams")
    LiveData<Integer> getTeamsCountLive();

    @Query("SELECT COUNT(*) FROM teams WHERE played_games > 0")
    LiveData<Integer> getActiveTeamsCount();

    @Query("SELECT COUNT(*) FROM teams WHERE points > :points")
    LiveData<Integer> getTeamsAbovePoints(int points);

    // ================ VERIFICACIONES ================

    @Query("SELECT EXISTS(SELECT 1 FROM teams WHERE team_id = :teamId)")
    boolean teamExists(int teamId);

    @Query("SELECT EXISTS(SELECT 1 FROM teams WHERE team_id = :teamId)")
    LiveData<Boolean> teamExistsLive(int teamId);

    @Query("SELECT EXISTS(SELECT 1 FROM teams WHERE tla = :tla)")
    boolean teamExistsByTla(String tla);

    @Query("SELECT team_id FROM teams")
    List<Integer> getAllTeamIds();

    @Query("SELECT tla FROM teams")
    List<String> getAllTeamTlas();

    // ================ UTILIDADES ================

    @Query("SELECT MAX(last_updated) FROM teams")
    long getLastSyncTime();

    @Query("SELECT MIN(position) FROM teams WHERE position > 0")
    int getFirstPosition();

    @Query("SELECT MAX(position) FROM teams WHERE position > 0")
    int getLastPosition();

    @Query("SELECT * FROM teams WHERE last_updated < :timestamp")
    List<Team> getTeamsNeedingUpdate(long timestamp);

    // ================ CONSULTAS COMPLEJAS ================

    @Query("""
        SELECT t.*, 
               (SELECT COUNT(*) FROM players p WHERE p.team_id = t.team_id) as player_count
        FROM teams t 
        ORDER BY t.position ASC
        """)
    LiveData<List<TeamWithPlayerCount>> getTeamsWithPlayerCount();

    @Query("""
        SELECT * FROM teams 
        WHERE points > (SELECT AVG(points) FROM teams WHERE played_games > 0)
        ORDER BY points DESC
        """)
    LiveData<List<Team>> getAboveAverageTeams();

    @Query("""
        SELECT * FROM teams 
        WHERE position <= 4 AND played_games > 0
        ORDER BY position ASC
        """)
    LiveData<List<Team>> getChampionsLeagueTeams();

    @Query("""
        SELECT * FROM teams 
        WHERE position >= (SELECT COUNT(*) - 2 FROM teams WHERE played_games > 0) 
        AND played_games > 0
        ORDER BY position DESC
        """)
    LiveData<List<Team>> getRelegationTeams();

    // Clase auxiliar para consultas complejas
    class TeamWithPlayerCount {
        public Team team;
        public int player_count;
    }
}