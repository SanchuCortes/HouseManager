package com.example.housemanager.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.housemanager.database.dao.PlayerDao;
import com.example.housemanager.database.dao.TeamDao;
import com.example.housemanager.database.entities.PlayerEntity;
import com.example.housemanager.database.entities.TeamEntity;

@Database(entities = {TeamEntity.class, PlayerEntity.class}, version = 1, exportSchema = false)
public abstract class HouseManagerDatabase extends RoomDatabase {
    public abstract TeamDao teamDao();
    public abstract PlayerDao playerDao();

    private static volatile HouseManagerDatabase INSTANCE;

    public static HouseManagerDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (HouseManagerDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            HouseManagerDatabase.class,
                            "house_manager.db"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
