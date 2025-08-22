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

    @Query("SELECT COUNT(*) FROM LeaguePlayerOwnership WHERE leagueId = :leagueId AND playerId = :playerId")
    int isOwnedInLeague(long leagueId, long playerId);

    @Query("SELECT playerId FROM LeaguePlayerOwnership WHERE leagueId = :leagueId AND ownerUserId = :ownerUserId")
    LiveData<List<Long>> getOwnedPlayerIdsLive(long leagueId, long ownerUserId);

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
}
