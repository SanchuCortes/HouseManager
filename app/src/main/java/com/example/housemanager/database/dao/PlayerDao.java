package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.housemanager.database.entities.PlayerEntity;

import java.util.List;

@Dao
public interface PlayerDao {

    // Operaciones básicas de guardado
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlayer(PlayerEntity player);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlayers(List<PlayerEntity> players);

    @Update
    void updatePlayer(PlayerEntity player);

    @Query("DELETE FROM players WHERE playerId = :playerId")
    void deletePlayer(int playerId);

    @Query("DELETE FROM players")
    void deleteAllPlayers();

    // Consultas principales - solo devolvemos entities, nada más
    @Query("SELECT * FROM players ORDER BY name ASC")
    LiveData<List<PlayerEntity>> getAllPlayerEntities();

    @Query("SELECT * FROM players WHERE available = 1 ORDER BY name ASC")
    LiveData<List<PlayerEntity>> getAvailablePlayers();

    // Esta es la consulta clave para Mi Equipo
    @Query("SELECT * FROM players WHERE teamId = :teamId ORDER BY name ASC")
    LiveData<List<PlayerEntity>> getSquadEntitiesByTeam(int teamId);

    @Query("SELECT * FROM players WHERE position = :position AND available = 1 ORDER BY name ASC")
    LiveData<List<PlayerEntity>> getPlayersByPosition(String position);

    @Query("SELECT * FROM players WHERE teamId = :teamId ORDER BY name ASC")
    LiveData<List<PlayerEntity>> getPlayersByTeam(int teamId);

    // Búsquedas básicas
    @Query("SELECT * FROM players WHERE available = 1 AND (name LIKE '%' || :searchTerm || '%' OR teamName LIKE '%' || :searchTerm || '%') ORDER BY name ASC")
    LiveData<List<PlayerEntity>> searchAvailablePlayers(String searchTerm);

    // Contadores que necesitamos
    @Query("SELECT COUNT(*) FROM players WHERE available = 1")
    LiveData<Integer> getAvailablePlayersCount();

    @Query("SELECT COUNT(*) FROM players WHERE teamId = :teamId")
    LiveData<Integer> getTeamPlayersCount(int teamId);

    @Query("SELECT COUNT(*) FROM players")
    int getPlayersCountSync();

    // Operaciones simples para fichajes
    @Query("UPDATE players SET available = 0 WHERE playerId = :playerId")
    void markPlayerAsUnavailable(int playerId);

    @Query("UPDATE players SET available = 1 WHERE playerId = :playerId")
    void markPlayerAsAvailable(int playerId);

    @Query("UPDATE players SET currentPrice = :newPrice WHERE playerId = :playerId")
    void updatePlayerPrice(int playerId, int newPrice);

    @Query("UPDATE players SET totalPoints = :points WHERE playerId = :playerId")
    void updatePlayerPoints(int playerId, int points);

    // Consulta individual
    @Query("SELECT * FROM players WHERE playerId = :playerId")
    LiveData<PlayerEntity> getPlayerById(int playerId);

    // Algunas estadísticas básicas
    @Query("SELECT * FROM players WHERE available = 1 ORDER BY totalPoints DESC LIMIT 10")
    LiveData<List<PlayerEntity>> getTopPlayersByPoints();

    @Query("SELECT * FROM players WHERE available = 1 ORDER BY currentPrice DESC LIMIT 10")
    LiveData<List<PlayerEntity>> getMostExpensivePlayers();

    @Query("SELECT * FROM players WHERE available = 1 ORDER BY currentPrice ASC LIMIT 10")
    LiveData<List<PlayerEntity>> getCheapestPlayers();

    // Filtros por posición específica - útiles para el mercado
    @Query("SELECT * FROM players WHERE position = 'Portero' AND available = 1 ORDER BY totalPoints DESC")
    LiveData<List<PlayerEntity>> getAvailablePorteros();

    @Query("SELECT * FROM players WHERE position = 'Defensa' AND available = 1 ORDER BY totalPoints DESC")
    LiveData<List<PlayerEntity>> getAvailableDefensas();

    @Query("SELECT * FROM players WHERE position = 'Medio' AND available = 1 ORDER BY totalPoints DESC")
    LiveData<List<PlayerEntity>> getAvailableMedios();

    @Query("SELECT * FROM players WHERE position = 'Delantero' AND available = 1 ORDER BY totalPoints DESC")
    LiveData<List<PlayerEntity>> getAvailableDelanteros();
}