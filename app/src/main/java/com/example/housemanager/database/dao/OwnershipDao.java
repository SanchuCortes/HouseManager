package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.housemanager.database.entities.LeaguePlayerOwnership;

import java.util.List;

@Dao
public interface OwnershipDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LeaguePlayerOwnership ownership);

    // Conteo por posición para una liga/usuario (sync)
    @Query("SELECT p.position AS position, COUNT(*) AS count FROM players p JOIN LeaguePlayerOwnership o ON o.playerId = p.playerId WHERE o.leagueId = :leagueId AND o.ownerUserId = :userId GROUP BY p.position")
    java.util.List<com.example.housemanager.database.pojo.PositionCount> getPositionCountsSync(long leagueId, long userId);

    @Query("SELECT COUNT(*) FROM LeaguePlayerOwnership WHERE leagueId = :leagueId AND playerId = :playerId")
    int isOwnedInLeague(long leagueId, long playerId);

    @Query("SELECT playerId FROM LeaguePlayerOwnership WHERE leagueId = :leagueId AND ownerUserId = :ownerUserId")
    LiveData<List<Long>> getOwnedPlayerIdsLive(long leagueId, long ownerUserId);

    // Lectura sincrónica: todas las propiedades para un jugador (para PlayerMatchPoints por liga)
    @Query("SELECT * FROM LeaguePlayerOwnership WHERE playerId = :playerId")
    List<LeaguePlayerOwnership> getOwnershipsForPlayerSync(int playerId);

    // Mi equipo por liga/usuario (unir con players)
    @Query("SELECT p.* FROM players p JOIN LeaguePlayerOwnership o ON o.playerId = p.playerId WHERE o.leagueId = :leagueId AND o.ownerUserId = :userId ORDER BY p.position, p.name")
    LiveData<List<com.example.housemanager.database.entities.PlayerEntity>> getMySquad(long leagueId, long userId);

    // Valor de mi plantilla en una liga (suma de precios)
    @Query("SELECT COALESCE(SUM(p.currentPrice), 0) FROM LeaguePlayerOwnership o JOIN players p ON p.playerId = o.playerId WHERE o.leagueId = :leagueId AND o.ownerUserId = :userId")
    LiveData<Integer> getMySquadValueLive(long leagueId, long userId);

    // Ids de jugadores poseídos por una liga (sync)
    @Query("SELECT playerId FROM LeaguePlayerOwnership WHERE leagueId = :leagueId")
    List<Integer> getPlayerIdsOwnedByLeagueSync(long leagueId);

    /**
     * Clasificación por liga: suma de puntos de jugadores por propietario.
     */
    @Query("SELECT o.ownerUserId AS userId, SUM(p.totalPoints) AS totalPoints " +
           "FROM LeaguePlayerOwnership o JOIN players p ON p.playerId = o.playerId " +
           "WHERE o.leagueId = :leagueId " +
           "GROUP BY o.ownerUserId " +
           "ORDER BY totalPoints DESC")
    LiveData<List<com.example.housemanager.database.pojo.ManagerScoreRow>> getLeagueClassification(long leagueId);

    /** Clasificación esta jornada (aplica x2 al capitán). */
    @Query("SELECT o.ownerUserId AS userId, SUM(CASE WHEN pmp.playerId = c.captainPlayerId THEN pmp.points * 2 ELSE pmp.points END) AS totalPoints " +
           "FROM LeaguePlayerOwnership o " +
           "JOIN PlayerMatchPoints pmp ON pmp.playerId = o.playerId " +
           "LEFT JOIN Captains c ON c.leagueId = o.leagueId AND c.ownerUserId = o.ownerUserId " +
           "WHERE o.leagueId = :leagueId AND pmp.matchday = :matchday " +
           "GROUP BY o.ownerUserId " +
           "ORDER BY totalPoints DESC")
    LiveData<List<com.example.housemanager.database.pojo.ManagerScoreRow>> getLeagueClassificationThisMatchday(long leagueId, int matchday);

    /** Clasificación temporada (aplica x2 al capitán sobre el total del capitán). */
    @Query("SELECT o.ownerUserId AS userId, (SUM(p.totalPoints) + COALESCE((SELECT p2.totalPoints FROM players p2, Captains c2 WHERE c2.leagueId = :leagueId AND c2.ownerUserId = o.ownerUserId AND p2.playerId = c2.captainPlayerId LIMIT 1), 0)) AS totalPoints " +
           "FROM LeaguePlayerOwnership o JOIN players p ON p.playerId = o.playerId " +
           "WHERE o.leagueId = :leagueId " +
           "GROUP BY o.ownerUserId " +
           "ORDER BY totalPoints DESC")
    LiveData<List<com.example.housemanager.database.pojo.ManagerScoreRow>> getLeagueClassificationSeason(long leagueId);

    @Query("DELETE FROM LeaguePlayerOwnership WHERE leagueId = :leagueId")
    void deleteByLeague(long leagueId);
}
