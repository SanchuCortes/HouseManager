package com.example.housemanager.repository;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.housemanager.api.models.PlayerAPI;
import com.example.housemanager.database.HouseManagerDatabase;
import com.example.housemanager.database.dao.PlayerDao;
import com.example.housemanager.database.dao.TeamDao;
import com.example.housemanager.database.entities.PlayerEntity;
import com.example.housemanager.database.entities.TeamEntity;
import com.example.housemanager.market.Player;
import com.example.housemanager.market.Team;

import java.util.ArrayList;
import java.util.List;

public class FootballRepository {

    public interface SyncCallback {
        void onSuccess();
        void onError(Throwable t);
    }

    private static FootballRepository instance;

    private final PlayerDao playerDao;
    private final TeamDao teamDao;

    private FootballRepository(Context context) {
        HouseManagerDatabase db = HouseManagerDatabase.getInstance(context);
        playerDao = db.playerDao();
        teamDao   = db.teamDao();
    }

    public static synchronized FootballRepository getInstance(Context context) {
        if (instance == null) {
            instance = new FootballRepository(context.getApplicationContext());
        }
        return instance;
    }

    // ====== TEAMS ======
    public LiveData<List<Team>> getAllTeams() {
        return Transformations.map(teamDao.getAllTeamEntities(), list -> {
            List<Team> out = new ArrayList<>();
            if (list == null) return out;
            for (TeamEntity e : list) {
                Team t = new Team();
                t.setTeamId(e.getTeamId());
                t.setName(e.getName());
                t.setCrestUrl(e.getCrestUrl());
                // OJO: NO seteamos otros campos porque tus setters no coinciden o tipan distinto
                out.add(t);
            }
            return out;
        });
    }

    // ====== PLAYERS ======
    public LiveData<List<Player>> getAllPlayers() {
        return Transformations.map(playerDao.getAllPlayerEntities(), list -> {
            List<Player> out = new ArrayList<>();
            if (list == null) return out;
            for (PlayerEntity e : list) {
                Player p = new Player();
                p.setPlayerId(e.getPlayerId());
                p.setName(e.getName());
                p.setTeamId(e.getTeamId());
                p.setTeamName(e.getTeamName());
                p.setNationality(e.getNationality());
                p.setPosition(e.getPosition());
                p.setCurrentPrice(e.getCurrentPrice());
                p.setTotalPoints(e.getTotalPoints());
                // NO seteamos available/injured/lastUpdated porque los tipos/nombres no coinciden
                out.add(p);
            }
            return out;
        });
    }

    // Para TeamDetail/MyTeam
    public LiveData<List<PlayerAPI>> getSquadApiByTeam(int teamId) {
        return playerDao.getSquadApiByTeam(teamId);
    }

    // ====== SYNC ======
    public void syncLaLigaTeams(@Nullable SyncCallback cb) {
        try {
            // TODO: tu sync real (Retrofit + guardar en Room)
            if (cb != null) cb.onSuccess();
        } catch (Throwable t) {
            if (cb != null) cb.onError(t);
        }
    }
}
