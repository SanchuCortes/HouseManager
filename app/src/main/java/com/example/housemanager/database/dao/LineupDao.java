package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface LineupDao {

    // Devuelve 1 si la alineación está incompleta o si no existe; 0 si está completa
    @Query("SELECT COALESCE((SELECT CASE WHEN (slot1 IS NOT NULL AND slot2 IS NOT NULL AND slot3 IS NOT NULL AND slot4 IS NOT NULL AND slot5 IS NOT NULL AND slot6 IS NOT NULL AND slot7 IS NOT NULL AND slot8 IS NOT NULL AND slot9 IS NOT NULL AND slot10 IS NOT NULL AND slot11 IS NOT NULL) THEN 0 ELSE 1 END FROM lineups WHERE userId = :userId AND matchday = :matchday LIMIT 1), 1)")
    LiveData<Integer> isLineupIncompleteInt(int userId, int matchday);
}
