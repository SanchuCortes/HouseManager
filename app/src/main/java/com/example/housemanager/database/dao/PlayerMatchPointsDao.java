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
}
