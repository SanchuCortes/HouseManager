package com.example.housemanager.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.housemanager.api.models.PlayerAPI;
import com.example.housemanager.api.models.TeamAPI;
import com.example.housemanager.database.entities.PlayerEntity;
import com.example.housemanager.repository.FootballRepository;

import java.util.ArrayList;
import java.util.List;

public class FootballViewModel extends AndroidViewModel {

    private final FootballRepository repo;

    public FootballViewModel(@NonNull Application app) {
        super(app);
        repo = FootballRepository.getInstance(app);
    }

    // Métodos para equipos
    public LiveData<List<TeamAPI>> getTeams() {
        // Convierto los equipos del repo a formato API para mostrar en la UI
        return Transformations.map(repo.getAllTeams(), this::convertTeamsForUI);
    }

    public void loadTeams() {
        // Inicio la sincronización sin callback específico
        repo.syncLaLigaTeams(null);
    }

    // Métodos para la plantilla de Mi Equipo
    private final MutableLiveData<List<PlayerAPI>> squadLiveData = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<PlayerAPI>> getSquad() {
        return squadLiveData;
    }

    public void loadSquad(int teamId) {
        // Cargo la plantilla desde el repo y la convierto para la UI
        repo.getSquadEntitiesByTeam(teamId).observeForever(entities -> {
            if (entities != null) {
                List<PlayerAPI> playersForUI = convertPlayersForUI(entities);
                squadLiveData.setValue(playersForUI);
            }
        });
    }

    // Métodos auxiliares para estado de sincronización
    public LiveData<Boolean> getIsSyncing() {
        return repo.getIsSyncing();
    }

    public LiveData<String> getSyncStatus() {
        return repo.getSyncStatus();
    }

    public LiveData<Integer> getAvailablePlayersCount() {
        return repo.getAvailablePlayersCount();
    }

    // Conversores privados - transforman datos del repo para la UI
    private List<TeamAPI> convertTeamsForUI(List<com.example.housemanager.market.Team> teams) {
        List<TeamAPI> result = new ArrayList<>();
        if (teams != null) {
            for (com.example.housemanager.market.Team team : teams) {
                TeamAPI apiTeam = new TeamAPI();
                apiTeam.setId(team.getTeamId());
                apiTeam.setName(team.getName());
                apiTeam.setCrest(team.getCrestUrl());
                result.add(apiTeam);
            }
        }
        return result;
    }

    private List<PlayerAPI> convertPlayersForUI(List<PlayerEntity> entities) {
        List<PlayerAPI> result = new ArrayList<>();
        if (entities != null) {
            for (PlayerEntity entity : entities) {
                PlayerAPI apiPlayer = new PlayerAPI();
                apiPlayer.setId(entity.getPlayerId());
                apiPlayer.setName(entity.getName());
                apiPlayer.setPosition(entity.getPosition());
                apiPlayer.setNationality(entity.getNationality());
                apiPlayer.setPoints(entity.getTotalPoints());
                result.add(apiPlayer);
            }
        }
        return result;
    }
}