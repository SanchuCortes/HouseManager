package com.example.housemanager.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.housemanager.BuildConfig;
import com.example.housemanager.database.dao.PlayerDao;
import com.example.housemanager.database.dao.TeamDao;
import com.example.housemanager.database.entities.PlayerEntity;
import com.example.housemanager.database.entities.TeamEntity;

@Database(
        entities = {TeamEntity.class, PlayerEntity.class, com.example.housemanager.database.entities.LeagueEntity.class, com.example.housemanager.database.entities.LineupEntity.class, com.example.housemanager.database.entities.MatchEntity.class, com.example.housemanager.database.entities.LeaguePlayerOwnership.class, com.example.housemanager.database.entities.MarketListing.class, com.example.housemanager.database.entities.MarketState.class, com.example.housemanager.database.entities.Captain.class, com.example.housemanager.database.entities.PlayerMatchPoints.class, com.example.housemanager.database.entities.MatchEventEntity.class, com.example.housemanager.database.entities.LineupEntryEntity.class, com.example.housemanager.database.entities.PlayerPointsHistoryEntity.class},
        version = 9,
        exportSchema = true
)
public abstract class HouseManagerDatabase extends RoomDatabase {
    private static final String TAG = "HouseManagerDatabase";

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


    public abstract com.example.housemanager.database.dao.MatchEventDao matchEventDao();
    public abstract com.example.housemanager.database.dao.LineupEntryDao lineupEntryDao();
    public abstract com.example.housemanager.database.dao.PlayerPointsHistoryDao playerPointsHistoryDao();

    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {

            db.execSQL("ALTER TABLE players ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0");
            Log.d(TAG, "MIGRATION_8_9 ejecutada correctamente: añadida columna players.updatedAt");
        }
    };

    private static volatile HouseManagerDatabase INSTANCE;

    public static HouseManagerDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (HouseManagerDatabase.class) {
                if (INSTANCE == null) {
                    RoomDatabase.Builder<HouseManagerDatabase> builder = Room.databaseBuilder(
                            context.getApplicationContext(),
                            HouseManagerDatabase.class,
                            "house_manager.db"
                    ).addMigrations(MIGRATION_8_9);

                    // En debug permitimos migración destructiva para no bloquear desarrollo.
                    if (BuildConfig.DEBUG) {
                        builder = builder.fallbackToDestructiveMigration();
                    }

                    INSTANCE = builder.build();
                }
            }
        }
        return INSTANCE;
    }
}
