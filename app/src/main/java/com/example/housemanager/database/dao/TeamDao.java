package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.housemanager.database.entities.TeamEntity;

import java.util.List;

@Dao
public interface TeamDao {

    // Operaciones básicas de guardado
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTeam(TeamEntity team);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTeams(List<TeamEntity> teams);

    @Update
    void updateTeam(TeamEntity team);

    @Query("DELETE FROM teams WHERE teamId = :teamId")
    void deleteTeam(int teamId);

    @Query("DELETE FROM teams")
    void deleteAllTeams();

    // Consultas principales - solo entities
    @Query("SELECT * FROM teams ORDER BY name ASC")
    LiveData<List<TeamEntity>> getAllTeamEntities();

    @Query("SELECT * FROM teams WHERE teamId = :teamId")
    LiveData<TeamEntity> getTeamById(int teamId);

    // Búsquedas básicas
    @Query("SELECT * FROM teams WHERE name LIKE '%' || :searchTerm || '%' ORDER BY name ASC")
    LiveData<List<TeamEntity>> searchTeamsByName(String searchTerm);

    // Contadores
    @Query("SELECT COUNT(*) FROM teams")
    LiveData<Integer> getTeamsCount();

    @Query("SELECT COUNT(*) FROM teams")
    int getTeamsCountSync();

    // Para el repositorio cuando necesita datos síncronos
    @Query("SELECT * FROM teams")
    List<TeamEntity> getAllTeamsSync();

    // Consultas útiles para spinners
    @Query("SELECT name FROM teams ORDER BY name ASC")
    LiveData<List<String>> getAllTeamNames();

    // Validaciones
    @Query("SELECT EXISTS(SELECT 1 FROM teams WHERE teamId = :teamId)")
    LiveData<Boolean> teamExists(int teamId);

    @Query("SELECT EXISTS(SELECT 1 FROM teams WHERE name = :teamName)")
    LiveData<Boolean> teamNameExists(String teamName);
}