package com.example.housemanager.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.housemanager.api.models.PlayerAPI;
import com.example.housemanager.api.models.TeamAPI;
import com.example.housemanager.market.Team;
import com.example.housemanager.repository.FootballRepository;

import java.util.ArrayList;
import java.util.List;

public class FootballViewModel extends AndroidViewModel {

    private final FootballRepository repo;

    private final MutableLiveData<List<TeamAPI>> teamsApi = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<PlayerAPI>> squad  = new MutableLiveData<>(new ArrayList<>());

    public FootballViewModel(@NonNull Application app) {
        super(app);
        repo = FootballRepository.getInstance(app);

        LiveData<List<Team>> teamsRoom = repo.getAllTeams();
        Transformations.map(teamsRoom, list -> {
            List<TeamAPI> out = new ArrayList<>();
            if (list != null) {
                for (Team t : list) {
                    TeamAPI api = new TeamAPI();
                    api.setId(t.getTeamId());
                    api.setName(t.getName());
                    api.setCrest(t.getCrestUrl());
                    out.add(api);
                }
            }
            return out;
        }).observeForever(teamsApi::setValue);
    }

    public LiveData<List<TeamAPI>> getTeams() { return teamsApi; }

    public void loadTeams() {
        repo.syncLaLigaTeams(new FootballRepository.SyncCallback() {
            @Override public void onSuccess() { }
            @Override public void onError(Throwable t) { }
        });
    }

    public LiveData<List<PlayerAPI>> getSquad() { return squad; }

    public void loadSquad(int teamId) {
        repo.getSquadApiByTeam(teamId).observeForever(squad::setValue);
    }
}
