package com.example.housemanager.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.housemanager.database.entities.PlayerMatchPoints;

import java.util.List;

@Dao
public interface PlayerMatchPointsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PlayerMatchPoints> points);

    @Query("DELETE FROM PlayerMatchPoints WHERE matchday = :matchday")
    void deleteByMatchday(int matchday);

    @Query("DELETE FROM PlayerMatchPoints")
    void clearAll();

    @Query("SELECT DISTINCT matchday FROM PlayerMatchPoints ORDER BY matchday ASC")
    List<Integer> getRecordedMatchdays();

    @Query("SELECT SUM(points) FROM PlayerMatchPoints WHERE playerId = :playerId")
    Integer getSeasonPointsForPlayer(int playerId);

    // Auditoría por partido
    @Query("SELECT * FROM PlayerMatchPoints WHERE matchId = :matchId")
    List<PlayerMatchPoints> getByMatchId(long matchId);

    @Query("SELECT SUM(points) FROM PlayerMatchPoints WHERE matchId = :matchId")
    Integer getTotalPointsForMatch(long matchId);
}
