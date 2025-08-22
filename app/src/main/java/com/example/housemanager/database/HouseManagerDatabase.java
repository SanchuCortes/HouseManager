package com.example.housemanager.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.housemanager.database.dao.PlayerDao;
import com.example.housemanager.database.dao.TeamDao;
import com.example.housemanager.database.entities.PlayerEntity;
import com.example.housemanager.database.entities.TeamEntity;

@Database(entities = {TeamEntity.class, PlayerEntity.class, com.example.housemanager.database.entities.LeagueEntity.class, com.example.housemanager.database.entities.LineupEntity.class, com.example.housemanager.database.entities.MatchEntity.class, com.example.housemanager.database.entities.LeaguePlayerOwnership.class, com.example.housemanager.database.entities.MarketListing.class, com.example.housemanager.database.entities.MarketState.class, com.example.housemanager.database.entities.Captain.class, com.example.housemanager.database.entities.PlayerMatchPoints.class, com.example.housemanager.database.entities.MatchEventEntity.class, com.example.housemanager.database.entities.LineupEntryEntity.class, com.example.housemanager.database.entities.PlayerPointsHistoryEntity.class}, version = 8, exportSchema = false)
public abstract class HouseManagerDatabase extends RoomDatabase {
    public abstract TeamDao teamDao();
    public abstract PlayerDao playerDao();
    public abstract com.example.housemanager.database.dao.LeagueDao leagueDao();
    public abstract com.example.housemanager.database.dao.LineupDao lineupDao();
    public abstract com.example.housemanager.database.dao.MatchDao matchDao();
    public abstract com.example.housemanager.database.dao.OwnershipDao ownershipDao();
    public abstract com.example.housemanager.database.dao.MarketDao marketDao();
    public abstract com.example.housemanager.database.dao.MarketStateDao marketStateDao();
    public abstract com.example.housemanager.database.dao.CaptainDao captainDao();
    public abstract com.example.housemanager.database.dao.PlayerMatchPointsDao playerMatchPointsDao();

    // New DAOs for points calculation support
    public abstract com.example.housemanager.database.dao.MatchEventDao matchEventDao();
    public abstract com.example.housemanager.database.dao.LineupEntryDao lineupEntryDao();
    public abstract com.example.housemanager.database.dao.PlayerPointsHistoryDao playerPointsHistoryDao();

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
