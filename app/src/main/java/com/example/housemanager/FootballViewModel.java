package com.example.housemanager;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.housemanager.market.Player;
import com.example.housemanager.market.Team;
import com.example.housemanager.repository.FootballRepository;

import java.util.List;

/**
 * ViewModel puente entre UI y FootballRepository.
 * - Expone LiveData de equipos y jugadores desde Room.
 * - Ofrece un método refreshAll() que dispara la sync con la API.
 * - Expone estado de carga, progreso, éxito y error para la UI.
 */
public class FootballViewModel extends AndroidViewModel {

    private final FootballRepository repo;

    // Datos que observa la UI
    private final LiveData<List<Team>> teams;
    private final LiveData<List<Player>> players;

    // Estado para mostrar en pantalla
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> progress = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> success = new MutableLiveData<>();

    public FootballViewModel(@NonNull Application app) {
        super(app);
        repo = FootballRepository.getInstance(app);

        // Room -> UI
        teams = repo.getAllTeams();
        players = repo.getAllPlayers();
    }

    // ========= Exposición de datos =========

    /** Equipos guardados en Room (se actualizan solos tras la sync). */
    public LiveData<List<Team>> getTeams() {
        return teams;
    }

    /** Jugadores guardados en Room (toda la “squad” disponible). */
    public LiveData<List<Player>> getSquad() {
        return players;
    }

    // ========= Estado de UI =========

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getProgress() { return progress; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getSuccess() { return success; }

    // ========= Acciones =========

    /**
     * Lanza la sincronización completa: equipos de LaLiga + plantillas.
     * Los LiveData de equipos/jugadores se actualizarán automáticamente
     * cuando Room reciba los inserts.
     */
    public void refreshAll() {
        loading.postValue(true);
        error.postValue(null);
        success.postValue(null);

        repo.syncLaLigaTeams(new FootballRepository.SyncCallback() {
            @Override
            public void onProgress(String message) {
                progress.postValue(message);
            }

            @Override
            public void onSuccess(String message) {
                loading.postValue(false);
                success.postValue(message);
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }
}
