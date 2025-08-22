package com.example.housemanager.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.housemanager.database.entities.PlayerPointsHistoryEntity;

@Dao
public interface PlayerPointsHistoryDao {

    @Insert
    void insert(PlayerPointsHistoryEntity e);

    @Query("DELETE FROM player_points_history WHERE matchId=:matchId")
    void clearByMatch(long matchId);

    @Query("SELECT SUM(points) FROM player_points_history WHERE playerId=:playerId")
    Integer getTotalForPlayer(int playerId);
}
