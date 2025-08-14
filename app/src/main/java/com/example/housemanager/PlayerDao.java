package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.housemanager.database.entities.Player;
import java.util.List;

@Dao
public interface PlayerDao {

    // ================ INSERTAR ================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlayers(List<Player> players);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlayer(Player player);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertPlayerIfNotExists(Player player);

    // ================ CONSULTAS BÁSICAS ================

    @Query("SELECT * FROM players ORDER BY name ASC")
    LiveData<List<Player>> getAllPlayers();

    @Query("SELECT * FROM players ORDER BY name ASC")
    List<Player> getAllPlayersSync();

    @Query("SELECT * FROM players WHERE player_id = :playerId")
    LiveData<Player> getPlayerById(int playerId);

    @Query("SELECT * FROM players WHERE player_id = :playerId")
    Player getPlayerByIdSync(int playerId);

    @Query("SELECT * FROM players WHERE name = :name")
    LiveData<List<Player>> getPlayersByName(String name);

    // ================ FILTROS POR POSICIÓN ================

    @Query("SELECT * FROM players WHERE position = :position ORDER BY total_points DESC")
    LiveData<List<Player>> getPlayersByPosition(String position);

    @Query("SELECT * FROM players WHERE position = :position ORDER BY total_points DESC")
    List<Player> getPlayersByPositionSync(String position);

    @Query("SELECT * FROM players WHERE position = 'GK' ORDER BY total_points DESC")
    LiveData<List<Player>> getGoalkeepers();

    @Query("SELECT * FROM players WHERE position = 'DEF' ORDER BY total_points DESC")
    LiveData<List<Player>> getDefenders();

    @Query("SELECT * FROM players WHERE position = 'MID' ORDER BY total_points DESC")
    LiveData<List<Player>> getMidfielders();

    @Query("SELECT * FROM players WHERE position = 'FWD' ORDER BY total_points DESC")
    LiveData<List<Player>> getForwards();

    // ================ FILTROS POR EQUIPO ================

    @Query("SELECT * FROM players WHERE team_id = :teamId ORDER BY shirt_number ASC")
    LiveData<List<Player>> getPlayersByTeam(int teamId);

    @Query("SELECT * FROM players WHERE team_id = :teamId ORDER BY shirt_number ASC")
    List<Player> getPlayersByTeamSync(int teamId);

    @Query("SELECT * FROM players WHERE team_name = :teamName ORDER BY name ASC")
    LiveData<List<Player>> getPlayersByTeamName(String teamName);

    // ================ BÚSQUEDAS ================

    @Query("SELECT * FROM players WHERE name LIKE '%' || :name || '%' ORDER BY name ASC")
    LiveData<List<Player>> searchPlayersByName(String name);

    @Query("SELECT * FROM players WHERE name LIKE '%' || :name || '%' ORDER BY name ASC")
    List<Player> searchPlayersByNameSync(String name);

    @Query("SELECT * FROM players WHERE nationality LIKE '%' || :nationality || '%' ORDER BY name ASC")
    LiveData<List<Player>> getPlayersByNationality(String nationality);

    // ================ RANKINGS Y ESTADÍSTICAS ================

    @Query("SELECT * FROM players ORDER BY total_points DESC LIMIT :limit")
    LiveData<List<Player>> getTopPlayersByPoints(int limit);

    @Query("SELECT * FROM players WHERE position = :position ORDER BY total_points DESC LIMIT :limit")
    LiveData<List<Player>> getTopPlayersByPositionAndPoints(String position, int limit);

    @Query("SELECT * FROM players ORDER BY goals DESC LIMIT :limit")
    LiveData<List<Player>> getTopScorers(int limit);

    @Query("SELECT * FROM players ORDER BY assists DESC LIMIT :limit")
    LiveData<List<Player>> getTopAssists(int limit);

    @Query("SELECT * FROM players WHERE position IN ('GK', 'DEF') ORDER BY clean_sheets DESC LIMIT :limit")
    LiveData<List<Player>> getTopCleanSheets(int limit);

    @Query("SELECT * FROM players ORDER BY current_price DESC LIMIT :limit")
    LiveData<List<Player>> getMostExpensivePlayers(int limit);

    @Query("SELECT * FROM players WHERE matches_played > 0 ORDER BY (CAST(total_points AS FLOAT) / matches_played) DESC LIMIT :limit")
    LiveData<List<Player>> getBestAveragePoints(int limit);

    @Query("SELECT * FROM players ORDER BY form_rating DESC LIMIT :limit")
    LiveData<List<Player>> getBestForm(int limit);

    // ================ DISPONIBILIDAD Y TRANSFERENCIAS ================

    @Query("SELECT * FROM players WHERE is_available = 1 ORDER BY position, total_points DESC")
    LiveData<List<Player>> getAvailablePlayers();

    @Query("SELECT * FROM players WHERE is_available = 1 ORDER BY position, total_points DESC")
    List<Player> getAvailablePlayersSync();

    @Query("SELECT * FROM players WHERE position = :position AND is_available = 1 ORDER BY total_points DESC")
    LiveData<List<Player>> getAvailablePlayersByPosition(String position);

    @Query("SELECT * FROM players WHERE is_available = 1 AND is_injured = 0 ORDER BY total_points DESC")
    LiveData<List<Player>> getAvailableHealthyPlayers();

    @Query("SELECT * FROM players WHERE is_injured = 1 ORDER BY name ASC")
    LiveData<List<Player>> getInjuredPlayers();

    // ================ FILTROS POR PRECIO ================

    @Query("SELECT * FROM players WHERE current_price BETWEEN :minPrice AND :maxPrice AND is_available = 1 ORDER BY current_price ASC")
    LiveData<List<Player>> getPlayersByPriceRange(double minPrice, double maxPrice);

    @Query("SELECT * FROM players WHERE current_price <= :maxPrice AND is_available = 1 ORDER BY total_points DESC")
    LiveData<List<Player>> getPlayersUnderPrice(double maxPrice);

    @Query("SELECT * FROM players WHERE current_price >= :minPrice ORDER BY current_price DESC")
    LiveData<List<Player>> getPlayersOverPrice(double minPrice);

    @Query("SELECT * FROM players WHERE current_price < base_price * 0.8 AND is_available = 1 ORDER BY (base_price - current_price) DESC")
    LiveData<List<Player>> getUndervaluedPlayers();

    @Query("SELECT * FROM players WHERE current_price > base_price * 1.5 ORDER BY (current_price - base_price) DESC")
    LiveData<List<Player>> getOvervaluedPlayers();

    // ================ FILTROS COMBINADOS ================

    @Query("SELECT * FROM players WHERE position = :position AND current_price <= :maxPrice AND is_available = 1 ORDER BY total_points DESC")
    LiveData<List<Player>> getPlayersByPositionAndPrice(String position, double maxPrice);

    @Query("SELECT * FROM players WHERE team_id = :teamId AND position = :position ORDER BY total_points DESC")
    LiveData<List<Player>> getPlayersByTeamAndPosition(int teamId, String position);

    @Query("SELECT * FROM players WHERE total_points >= :minPoints AND is_available = 1 ORDER BY current_price ASC")
    LiveData<List<Player>> getPlayersWithMinPoints(int minPoints);

    @Query("SELECT * FROM players WHERE form_rating >= :minRating AND is_available = 1 ORDER BY form_rating DESC")
    LiveData<List<Player>> getPlayersWithGoodForm(double minRating);

    // ================ ACTUALIZACIONES ================

    @Update
    void updatePlayer(Player player);

    @Update
    void updatePlayers(List<Player> players);

    @Query("UPDATE players SET total_points = total_points + :points WHERE player_id = :playerId")
    void addPointsToPlayer(int playerId, int points);

    @Query("UPDATE players SET current_price = :newPrice WHERE player_id = :playerId")
    void updatePlayerPrice(int playerId, double newPrice);

    @Query("UPDATE players SET is_available = :isAvailable WHERE player_id = :playerId")
    void setPlayerAvailability(int playerId, boolean isAvailable);

    @Query("UPDATE players SET is_injured = :isInjured WHERE player_id = :playerId")
    void setPlayerInjuryStatus(int playerId, boolean isInjured);

    @Query("UPDATE players SET form_rating = :rating WHERE player_id = :playerId")
    void updatePlayerForm(int playerId, double rating);

    @Query("UPDATE players SET goals = goals + :goals, assists = assists + :assists WHERE player_id = :playerId")
    void addPlayerStats(int playerId, int goals, int assists);

    @Query("UPDATE players SET yellow_cards = yellow_cards + :yellows, red_cards = red_cards + :reds WHERE player_id = :playerId")
    void addPlayerCards(int playerId, int yellows, int reds);

    @Query("UPDATE players SET clean_sheets = clean_sheets + 1 WHERE player_id = :playerId")
    void addCleanSheet(int playerId);

    @Query("UPDATE players SET matches_played = matches_played + 1 WHERE player_id = :playerId")
    void incrementMatchesPlayed(int playerId);

    @Query("UPDATE players SET last_updated = :timestamp WHERE player_id = :playerId")
    void updateLastUpdated(int playerId, long timestamp);

    // ================ ELIMINAR ================

    @Delete
    void deletePlayer(Player player);

    @Delete
    void deletePlayers(List<Player> players);

    @Query("DELETE FROM players WHERE player_id = :playerId")
    void deletePlayerById(int playerId);

    @Query("DELETE FROM players WHERE team_id = :teamId")
    void deletePlayersByTeam(int teamId);

    @Query("DELETE FROM players")
    void deleteAllPlayers();

    @Query("DELETE FROM players WHERE last_updated < :timestamp")
    void deleteOldPlayers(long timestamp);

    // ================ CONTADORES ================

    @Query("SELECT COUNT(*) FROM players")
    int getPlayersCount();

    @Query("SELECT COUNT(*) FROM players")
    LiveData<Integer> getPlayersCountLive();

    @Query("SELECT COUNT(*) FROM players WHERE position = :position")
    int getPlayersCountByPosition(String position);

    @Query("SELECT COUNT(*) FROM players WHERE position = :position")
    LiveData<Integer> getPlayersCountByPositionLive(String position);

    @Query("SELECT COUNT(*) FROM players WHERE team_id = :teamId")
    LiveData<Integer> getPlayersCountByTeam(int teamId);

    @Query("SELECT COUNT(*) FROM players WHERE is_available = 1")
    LiveData<Integer> getAvailablePlayersCount();

    @Query("SELECT COUNT(*) FROM players WHERE is_injured = 1")
    LiveData<Integer> getInjuredPlayersCount();

    // ================ VERIFICACIONES ================

    @Query("SELECT EXISTS(SELECT 1 FROM players WHERE player_id = :playerId)")
    boolean playerExists(int playerId);

    @Query("SELECT EXISTS(SELECT 1 FROM players WHERE player_id = :playerId)")
    LiveData<Boolean> playerExistsLive(int playerId);

    @Query("SELECT EXISTS(SELECT 1 FROM players WHERE team_id = :teamId AND shirt_number = :number)")
    boolean shirtNumberTaken(int teamId, int number);

    @Query("SELECT player_id FROM players WHERE team_id = :teamId")
    List<Integer> getPlayerIdsByTeam(int teamId);

    @Query("SELECT player_id FROM players WHERE position = :position")
    List<Integer> getPlayerIdsByPosition(String position);

    // ================ ESTADÍSTICAS AGREGADAS ================

    @Query("SELECT AVG(current_price) FROM players WHERE position = :position")
    LiveData<Double> getAveragePriceByPosition(String position);

    @Query("SELECT AVG(total_points) FROM players WHERE matches_played > 0")
    LiveData<Double> getAveragePoints();

    @Query("SELECT AVG(form_rating) FROM players")
    LiveData<Double> getAverageFormRating();

    @Query("SELECT SUM(goals) FROM players WHERE team_id = :teamId")
    LiveData<Integer> getTotalGoalsByTeam(int teamId);

    @Query("SELECT SUM(assists) FROM players WHERE team_id = :teamId")
    LiveData<Integer> getTotalAssistsByTeam(int teamId);

    @Query("SELECT MAX(current_price) FROM players WHERE position = :position")
    double getMaxPriceByPosition(String position);

    @Query("SELECT MIN(current_price) FROM players WHERE position = :position AND is_available = 1")
    double getMinPriceByPosition(String position);

    // ================ UTILIDADES ================

    @Query("SELECT MAX(last_updated) FROM players")
    long getLastSyncTime();

    @Query("SELECT * FROM players WHERE last_updated < :timestamp")
    List<Player> getPlayersNeedingUpdate(long timestamp);

    @Query("SELECT DISTINCT nationality FROM players WHERE nationality IS NOT NULL ORDER BY nationality ASC")
    LiveData<List<String>> getAllNationalities();

    @Query("SELECT DISTINCT team_name FROM players ORDER BY team_name ASC")
    LiveData<List<String>> getAllTeamNames();

    @Query("SELECT DISTINCT position FROM players ORDER BY position ASC")
    List<String> getAllPositions();

    // ================ CONSULTAS COMPLEJAS ================

    @Query("""
        SELECT p.*, t.name as team_full_name, t.tla as team_code
        FROM players p 
        INNER JOIN teams t ON p.team_id = t.team_id 
        WHERE p.is_available = 1 
        ORDER BY p.total_points DESC
        """)
    LiveData<List<PlayerWithTeamInfo>> getAvailablePlayersWithTeamInfo();

    @Query("""
        SELECT position, COUNT(*) as count, AVG(current_price) as avg_price, AVG(total_points) as avg_points
        FROM players 
        WHERE is_available = 1 
        GROUP BY position 
        ORDER BY position
        """)
    LiveData<List<PositionStats>> getPositionStatistics();

    @Query("""
        SELECT * FROM players 
        WHERE total_points > (SELECT AVG(total_points) FROM players WHERE matches_played > 0)
        AND is_available = 1
        ORDER BY total_points DESC
        """)
    LiveData<List<Player>> getAboveAveragePlayers();

    @Query("""
        SELECT * FROM players 
        WHERE current_price < (SELECT AVG(current_price) FROM players WHERE position = players.position)
        AND is_available = 1
        ORDER BY (total_points / current_price) DESC
        """)
    LiveData<List<Player>> getBestValuePlayers();

    // Clases auxiliares para consultas complejas
    class PlayerWithTeamInfo {
        public Player player;
        public String team_full_name;
        public String team_code;
    }

    class PositionStats {
        public String position;
        public int count;
        public double avg_price;
        public double avg_points;
    }
}