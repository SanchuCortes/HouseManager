package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.housemanager.database.entities.TeamEntity;

import java.util.List;

@Dao
public interface TeamDao {
    @Query("SELECT * FROM teams ORDER BY name ASC")
    LiveData<List<TeamEntity>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TeamEntity> entities);

    @Query("DELETE FROM teams")
    void clear();
}
