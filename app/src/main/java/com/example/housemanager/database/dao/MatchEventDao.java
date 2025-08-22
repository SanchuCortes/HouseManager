package com.example.housemanager.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.housemanager.database.entities.MatchEventEntity;

import java.util.List;

@Dao
public interface MatchEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MatchEventEntity> events);

    @Query("DELETE FROM match_events WHERE matchId=:matchId")
    void clearByMatch(long matchId);

    @Query("SELECT * FROM match_events WHERE matchId=:matchId")
    List<MatchEventEntity> getByMatch(long matchId);
}
