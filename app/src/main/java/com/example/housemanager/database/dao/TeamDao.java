package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.example.housemanager.database.entities.TeamEntity;

import java.util.List;

@Dao
public interface TeamDao {

    // Devolvemos Entities (lo que realmente existe en la tabla 'teams')
    @Query("SELECT * FROM teams ORDER BY name ASC")
    LiveData<List<TeamEntity>> getAllTeamEntities();
}
