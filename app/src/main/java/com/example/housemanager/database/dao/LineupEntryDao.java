package com.example.housemanager.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.housemanager.database.entities.LineupEntryEntity;

import java.util.List;

@Dao
public interface LineupEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LineupEntryEntity> entries);

    @Query("DELETE FROM lineup_entries WHERE matchId=:matchId")
    void clearByMatch(long matchId);

    @Query("SELECT * FROM lineup_entries WHERE matchId=:matchId")
    List<LineupEntryEntity> getByMatch(long matchId);
}
