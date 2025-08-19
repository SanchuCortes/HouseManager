package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.example.housemanager.api.models.PlayerAPI;
import com.example.housemanager.database.entities.PlayerEntity;

import java.util.List;

@Dao
public interface PlayerDao {

    // Mercado / listados generales -> Entities reales
    @Query("SELECT * FROM players ORDER BY name ASC")
    LiveData<List<PlayerEntity>> getAllPlayerEntities();

    // Plantilla por equipo (TeamDetail/MyTeam) -> proyección directa a PlayerAPI
    @Query("SELECT " +
            "playerId    AS id, " +
            "name        AS name, " +
            "position    AS position, " +
            "nationality AS nationality, " +
            "0           AS points " +         // si no tienes columna 'points', devolvemos 0
            "FROM players " +
            "WHERE teamId = :teamId " +
            "ORDER BY name ASC")
    LiveData<List<PlayerAPI>> getSquadApiByTeam(int teamId);
}
