package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.housemanager.database.entities.PlayerEntity;

import java.util.List;

/** Acceso a tabla players. */
@Dao
public interface PlayerDao {

    /** Inserta una lista de jugadores. Reemplaza si existe. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlayers(List<PlayerEntity> players);

    /** Inserta un jugador. Reemplaza si existe. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlayer(PlayerEntity player);

    /** Inserta ignorando conflictos: devuelve ids o -1 si ya existían. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insertIgnore(List<PlayerEntity> players);

    /** Actualiza SOLO campos de ficha sin tocar totalPoints ni available. */
    @Query("UPDATE players SET name=:name, teamId=:teamId, teamName=:teamName, position=:position, nationality=:nationality, currentPrice=:currentPrice, updatedAt=:updatedAt WHERE playerId=:playerId")
    void updateFromApiWithoutPoints(long playerId, String name, int teamId, String teamName, String position, String nationality, int currentPrice, long updatedAt);

    /** Actualiza un jugador. */
    @Update
    void updatePlayer(PlayerEntity player);

    /** Borra todos los jugadores. */
    @Query("DELETE FROM players")
    void deleteAllPlayers();

    /** Devuelve todos los jugadores para la UI. */
    @Query("SELECT * FROM players ORDER BY name ASC")
    LiveData<List<PlayerEntity>> getAllPlayerEntities();

    /** Devuelve la plantilla de un equipo. */
    @Query("SELECT * FROM players WHERE teamId = :teamId ORDER BY name ASC")
    LiveData<List<PlayerEntity>> getSquadByTeam(int teamId);

    /** Cuenta jugadores disponibles para métricas. */
    @Query("SELECT COUNT(*) FROM players WHERE available = 1")
    LiveData<Integer> getAvailablePlayersCount();

    /** Búsqueda por nombre o equipo, solo disponibles. */
    @Query("SELECT * FROM players WHERE " +
            "available = 1 AND " +
            "(name LIKE '%' || :searchTerm || '%' OR teamName LIKE '%' || :searchTerm || '%') " +
            "ORDER BY name ASC")
    LiveData<List<PlayerEntity>> searchAvailablePlayers(String searchTerm);

    /** Filtro por posición y término, solo disponibles. */
    @Query("SELECT * FROM players WHERE " +
            "available = 1 AND position = :position AND " +
            "(name LIKE '%' || :searchTerm || '%' OR teamName LIKE '%' || :searchTerm || '%') " +
            "ORDER BY name ASC")
    LiveData<List<PlayerEntity>> searchPlayersByPositionAndTerm(String position, String searchTerm);

    /** Listados por posición, ordenados por puntos. */
    @Query("SELECT * FROM players WHERE position = 'Portero' AND available = 1 ORDER BY totalPoints DESC")
    LiveData<List<PlayerEntity>> getAvailablePorteros();

    @Query("SELECT * FROM players WHERE position = 'Defensa' AND available = 1 ORDER BY totalPoints DESC")
    LiveData<List<PlayerEntity>> getAvailableDefensas();

    @Query("SELECT * FROM players WHERE position = 'Medio' AND available = 1 ORDER BY totalPoints DESC")
    LiveData<List<PlayerEntity>> getAvailableMedios();

    @Query("SELECT * FROM players WHERE position = 'Delantero' AND available = 1 ORDER BY totalPoints DESC")
    LiveData<List<PlayerEntity>> getAvailableDelanteros();

    /** Devuelve 10 jugadores aleatorios disponibles para el mercado (legacy). */
    @Query("SELECT * FROM players WHERE available = 1 ORDER BY RANDOM() LIMIT 10")
    LiveData<List<PlayerEntity>> getRandomAvailablePlayers();

    /** Mercado: lista del día (estable), ordenada por nombre. */
    @Query("SELECT * FROM players WHERE available = 1 ORDER BY name ASC")
    LiveData<List<PlayerEntity>> getMarketToday();

    /** Limpiar mercado: marcar todos como no disponibles. */
    @Query("UPDATE players SET available = 0")
    void clearMarket();

    /** Marcar no disponible (alias) */
    @Query("UPDATE players SET available = 0 WHERE playerId = :playerId")
    void markUnavailable(long playerId);

    /** Marcar disponibles por lote. */
    @Query("UPDATE players SET available = 1 WHERE playerId IN (:ids)")
    void markAvailableInIds(java.util.List<Integer> ids);

    /** Obtener IDs aleatorios de jugadores (sync). */
    @Query("SELECT playerId FROM players ORDER BY RANDOM() LIMIT :limit")
    java.util.List<Integer> getRandomPlayerIdsSync(int limit);

    /** Mercado: 10 jugadores aleatorios disponibles (LiveData). */
    @Query("SELECT * FROM players WHERE available = 1 ORDER BY RANDOM() LIMIT 10")
    LiveData<List<PlayerEntity>> getMarketRandom10();

    /** Mercado: 10 jugadores aleatorios disponibles (sync). */
    @Query("SELECT * FROM players WHERE available = 1 ORDER BY RANDOM() LIMIT 10")
    java.util.List<PlayerEntity> getMarketRandom10Sync();

    /** Conteo síncrono para decisiones de sincronización. */
    @Query("SELECT COUNT(*) FROM players")
    int getPlayersCountSync();

    /** Jugadores no poseídos en una liga concreta (consulta síncrona). */
    @Query("SELECT p.* FROM players p WHERE NOT EXISTS (SELECT 1 FROM LeaguePlayerOwnership o WHERE o.leagueId = :leagueId AND o.playerId = p.playerId)")
    List<com.example.housemanager.database.entities.PlayerEntity> getAllNotOwnedSync(long leagueId);

    /** Marca un jugador como comprado (no disponible). */
    @Query("UPDATE players SET available = 0 WHERE playerId = :playerId")
    void markAsBought(int playerId);

    /** Marca un jugador como disponible (en venta). */
    @Query("UPDATE players SET available = 1 WHERE playerId = :playerId")
    void markAsAvailable(int playerId);

    /** Marca como no disponibles a todos los jugadores cuyo id NO esté en la lista (para bajas tras sync). */
    @Query("UPDATE players SET available = 0 WHERE playerId NOT IN (:ids)")
    void markUnavailableNotInIds(java.util.List<Integer> ids);

    /** Obtiene jugadores por IDs de forma síncrona. */
    @Query("SELECT * FROM players WHERE playerId IN (:ids)")
    java.util.List<PlayerEntity> getByIdsSync(java.util.List<Integer> ids);

    /** Obtiene jugadores de un equipo (sync). */
    @Query("SELECT * FROM players WHERE teamId = :teamId")
    java.util.List<PlayerEntity> getByTeamSync(int teamId);

    /** Suma puntos a todos los jugadores de un equipo. */
    @Query("UPDATE players SET totalPoints = totalPoints + :points WHERE teamId = :teamId")
    void addPointsToTeam(int teamId, int points);

    /** Reinicia los puntos de todos los jugadores. */
    @Query("UPDATE players SET totalPoints = 0")
    void resetAllPoints();

    /** Establece el total de puntos absoluto para un jugador. */
    @Query("UPDATE players SET totalPoints = :total WHERE playerId = :playerId")
    void updateTotalPoints(int playerId, int total);

    /** Overload por si se llama con long. */
    @Query("UPDATE players SET totalPoints = :total WHERE playerId = :playerId")
    void updateTotalPoints(long playerId, int total);
}
