package com.example.housemanager.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.housemanager.api.models.PlayerAPI;
import com.example.housemanager.api.models.TeamAPI;
import com.example.housemanager.market.Player;
import com.example.housemanager.market.Team;
import com.example.housemanager.repository.FootballRepository;

import java.util.ArrayList;
import java.util.List;

public class FootballViewModel extends AndroidViewModel {

    private static final String TAG = "FootballViewModel";

    private final FootballRepository repository;

    // Variables para mantener el estado de la UI
    private final MutableLiveData<List<TeamAPI>> teamsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<PlayerAPI>> squadLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Player>> marketPlayersLiveData = new MutableLiveData<>(new ArrayList<>());

    public FootballViewModel(@NonNull Application application) {
        super(application);
        repository = FootballRepository.getInstance(application);

        Log.d(TAG, "Inicializando ViewModel");

        // Cuando cambien los equipos en la BD, actualizar la UI
        repository.getAllTeams().observeForever(teams -> {
            List<TeamAPI> teamAPIs = mapTeamsToAPI(teams);
            teamsLiveData.setValue(teamAPIs);
            Log.d(TAG, "Equipos actualizados: " + teamAPIs.size());
        });

        // Lo mismo para los jugadores del mercado
        repository.getAllPlayers().observeForever(players -> {
            marketPlayersLiveData.setValue(players);
            Log.d(TAG, "Jugadores del mercado actualizados: " + (players != null ? players.size() : 0));
        });
    }

    // Métodos que usa la UI para obtener datos

    public LiveData<List<TeamAPI>> getTeams() {
        return teamsLiveData;
    }

    public LiveData<List<PlayerAPI>> getSquad() {
        return squadLiveData;
    }

    public LiveData<List<Player>> getMarketPlayers() {
        return marketPlayersLiveData;
    }

    // Llamar cuando necesitemos cargar los equipos
    public void loadTeams() {
        Log.d(TAG, "Iniciando carga de equipos");
        repository.syncLaLigaTeams(new FootballRepository.SyncCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Equipos cargados correctamente");
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "Error cargando equipos", t);
            }

            @Override
            public void onProgress(String message, int current, int total) {
                Log.d(TAG, "Progreso: " + message + " (" + current + "/" + total + ")");
            }
        });
    }

    // Cargar jugadores de un equipo específico
    public void loadSquad(int teamId) {
        Log.d(TAG, "Cargando plantilla del equipo: " + teamId);

        // Observar los jugadores de este equipo
        repository.getSquadApiByTeam(teamId).observeForever(players -> {
            if (players != null) {
                squadLiveData.setValue(players);
                Log.d(TAG, "Plantilla cargada: " + players.size() + " jugadores");
            } else {
                Log.w(TAG, "No hay jugadores para el equipo " + teamId);
                squadLiveData.setValue(new ArrayList<>());
            }
        });
    }

    // Buscar jugadores por nombre
    public LiveData<List<Player>> searchPlayers(String searchTerm) {
        Log.d(TAG, "Buscando jugadores: " + searchTerm);
        return repository.searchPlayers(searchTerm);
    }

    // Filtrar por posición
    public LiveData<List<Player>> getPlayersByPosition(String position) {
        Log.d(TAG, "Filtrando por posición: " + position);
        return repository.getPlayersByPosition(position);
    }

    // Fichar un jugador
    public void buyPlayer(int playerId, FootballRepository.SyncCallback callback) {
        Log.d(TAG, "Fichando jugador: " + playerId);
        repository.buyPlayer(playerId, callback);
    }

    // Vender un jugador
    public void sellPlayer(int playerId, FootballRepository.SyncCallback callback) {
        Log.d(TAG, "Vendiendo jugador: " + playerId);
        repository.sellPlayer(playerId, callback);
    }

    // Refrescar todo desde la API
    public void refreshData() {
        Log.d(TAG, "Refrescando datos desde la API");
        repository.forceSyncFromAPI(new FootballRepository.SyncCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Datos refrescados OK");
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "Error refrescando", t);
            }

            @Override
            public void onProgress(String message, int current, int total) {
                Log.d(TAG, "Progreso refresh: " + message);
            }
        });
    }

    // Para mostrar si está cargando
    public LiveData<Boolean> getIsSyncing() {
        return repository.getIsSyncing();
    }

    public LiveData<String> getSyncStatus() {
        return repository.getSyncStatus();
    }

    // Cuántos jugadores hay disponibles
    public LiveData<Integer> getAvailablePlayersCount() {
        return repository.getAvailablePlayersCount();
    }

    // Convertir los equipos de la BD al formato que usa la UI
    private List<TeamAPI> mapTeamsToAPI(List<Team> teams) {
        List<TeamAPI> teamAPIs = new ArrayList<>();
        if (teams == null) return teamAPIs;

        for (Team team : teams) {
            TeamAPI api = new TeamAPI();
            api.setId(team.getTeamId());
            api.setName(team.getName());
            api.setCrest(team.getCrestUrl());
            teamAPIs.add(api);
        }

        Log.d(TAG, "Convertidos " + teams.size() + " equipos");
        return teamAPIs;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "ViewModel limpiado");
    }
}