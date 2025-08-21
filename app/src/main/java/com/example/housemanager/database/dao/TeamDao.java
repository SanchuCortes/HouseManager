package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.housemanager.database.entities.TeamEntity;

import java.util.List;

/** Acceso a tabla teams. */
@Dao
public interface TeamDao {

    /** Inserta una lista de equipos. Reemplaza si existe. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTeams(List<TeamEntity> teams);

    /** Inserta un equipo. Reemplaza si existe. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTeam(TeamEntity team);

    /** Borra todos los equipos. */
    @Query("DELETE FROM teams")
    void deleteAllTeams();

    /** Devuelve todos los equipos para la UI. */
    @Query("SELECT * FROM teams ORDER BY name ASC")
    LiveData<List<TeamEntity>> getAllTeamEntities();

    /** Conteo síncrono para decisiones de sincronización. */
    @Query("SELECT COUNT(*) FROM teams")
    int getTeamsCountSync();

    /** Listado síncrono de equipos para procesos internos. */
    @Query("SELECT * FROM teams ORDER BY name ASC")
    List<TeamEntity> getAllTeamsSync();

    /** Verifica existencia de un equipo. */
    @Query("SELECT EXISTS(SELECT 1 FROM teams WHERE teamId = :teamId)")
    LiveData<Boolean> teamExists(int teamId);
}
