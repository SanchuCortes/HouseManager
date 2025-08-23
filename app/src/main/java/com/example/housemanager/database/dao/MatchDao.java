package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.housemanager.database.entities.MatchEntity;

import java.util.List;

/** Acceso a tabla matches. */
@Dao
public interface MatchDao {

    /** Inserción masiva de partidos (REPLACE). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MatchEntity> matches);

    /** Borra todos los partidos. */
    @Query("DELETE FROM matches")
    void deleteAll();

    /** Devuelve partidos en un rango de tiempo, orden ascendente por fecha. */
    @Query("SELECT * FROM matches WHERE utcDateMillis >= :fromMillis AND utcDateMillis <= :toMillis ORDER BY utcDateMillis ASC")
    LiveData<List<MatchEntity>> getMatchesInRange(long fromMillis, long toMillis);

    /** Devuelve los próximos 'limit' partidos a partir de una fecha. */
    @Query("SELECT * FROM matches WHERE utcDateMillis >= :fromMillis ORDER BY utcDateMillis ASC LIMIT :limit")
    LiveData<List<MatchEntity>> getUpcoming(long fromMillis, int limit);

    /** Devuelve los 10 partidos de la jornada (persistidos) ordenados por fecha. */
    @Query("SELECT * FROM matches ORDER BY utcDateMillis ASC LIMIT 10")
    LiveData<List<MatchEntity>> getMatchdayMatchesLive();

    /** Devuelve los 10 partidos (lo que haya) con fecha válida, ordenados por fecha. Útil tras sync de jornada. */
    @Query("SELECT * FROM matches WHERE utcDateMillis > 0 ORDER BY utcDateMillis ASC LIMIT 10")
    LiveData<List<MatchEntity>> getUpcoming10();

    /** Devuelve todos los partidos de forma síncrona (para cálculo de puntos). */
    @Query("SELECT * FROM matches")
    List<MatchEntity> getAllSync();

    /** Cuenta los partidos futuros (>= fromMillis) de forma síncrona. */
    @Query("SELECT COUNT(*) FROM matches WHERE utcDateMillis >= :fromMillis")
    int countUpcomingSync(long fromMillis);
}
