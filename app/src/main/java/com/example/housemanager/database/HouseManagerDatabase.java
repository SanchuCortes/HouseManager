package com.example.housemanager.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.housemanager.database.dao.PlayerDao;
import com.example.housemanager.database.dao.TeamDao;
import com.example.housemanager.database.entities.PlayerEntity;
import com.example.housemanager.database.entities.TeamEntity;

/**
 * Base de datos principal de la aplicación usando Room.
 * Maneja el almacenamiento local de equipos y jugadores de LaLiga.
 * Implementa el patrón Singleton para garantizar una única instancia.
 */
@Database(
        entities = {TeamEntity.class, PlayerEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class HouseManagerDatabase extends RoomDatabase {

    public abstract TeamDao teamDao();
    public abstract PlayerDao playerDao();

    private static volatile HouseManagerDatabase INSTANCE;

    /**
     * Obtiene la instancia única de la base de datos.
     * Crea la base de datos si no existe o devuelve la instancia existente.
     */
    public static HouseManagerDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (HouseManagerDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    HouseManagerDatabase.class,
                                    "house_manager.db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Cierra la instancia de la base de datos.
     * Principalmente usado para testing o limpieza de recursos.
     */
    public static void closeInstance() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
}