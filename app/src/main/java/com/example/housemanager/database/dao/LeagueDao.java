package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.housemanager.database.entities.LeagueEntity;

import java.util.List;

@Dao
public interface LeagueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertLeague(LeagueEntity league);

    @Query("SELECT * FROM leagues ORDER BY name ASC")
    LiveData<List<LeagueEntity>> getAllLeagues();

    @Query("SELECT * FROM leagues ORDER BY name ASC")
    List<LeagueEntity> getAllLeaguesSync();

    // Cuenta ligas activas según el estado "Activa"
    @Query("SELECT COUNT(*) FROM leagues WHERE status = 'Activa'")
    LiveData<Integer> countActiveLeagues();

    // Obtiene una liga por id (para leer marketHour)
    @Query("SELECT * FROM leagues WHERE id = :id LIMIT 1")
    LeagueEntity getByIdSync(long id);

    // Presupuesto en vivo para una liga concreta
    @Query("SELECT budget FROM leagues WHERE id = :id LIMIT 1")
    LiveData<Integer> getBudgetLive(long id);

    @Query("DELETE FROM leagues WHERE id = :id")
    void deleteLeagueCore(long id);
}
