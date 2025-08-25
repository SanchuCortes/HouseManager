package com.example.housemanager.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.housemanager.database.entities.MatchEntity;
import com.example.housemanager.repository.FootballRepository;

import java.util.List;

/**
 * ViewModel para la pantalla principal (Home), encargado de exponer los partidos a mostrar
 * siguiendo el orden de preferencia:
 * 1) Partidos de la jornada actual (si se puede determinar)
 * 2) Próximos 7 días por fecha
 * 3) Últimos 10 finalizados
 *
 * Sin observeForever. Todo lifecycle-aware mediante MediatorLiveData.
 */
public class HomeViewModel extends AndroidViewModel {

    private final FootballRepository repository;

    // Puede ser null cuando no se conoce la jornada actual
    private final MutableLiveData<Integer> currentMatchday = new MutableLiveData<>(null);
    private final MediatorLiveData<List<MatchEntity>> homeMatches = new MediatorLiveData<>();

    // Fuentes actuales para poder soltarlas al cambiar
    private LiveData<List<MatchEntity>> sourceMd;
    private LiveData<List<MatchEntity>> sourceRange;
    private LiveData<List<MatchEntity>> sourceFinished;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = FootballRepository.getInstance(application);

        // Preparar composición reactiva
        homeMatches.setValue(java.util.Collections.emptyList());
        homeMatches.addSource(currentMatchday, md -> rebuildSources(md));

        // Disparar una primera carga remota de la jornada actual (no bloqueante)
        repository.getCurrentMatchday(new FootballRepository.MatchdayCallback() {
            @Override public void onResult(int matchday) { currentMatchday.postValue(matchday); }
            @Override public void onError(Throwable t) { currentMatchday.postValue(null); }
        });
    }

    public LiveData<List<MatchEntity>> getHomeMatches() {
        return homeMatches;
    }

    /** Permite al UI pedir refresco de datos (opcional). */
    public void refresh() {
        // Aseguramos que haya datos de próximos partidos por si no hay jornada actual
        repository.syncUpcomingMatches(null);
        // Volvemos a consultar jornada actual
        repository.getCurrentMatchday(new FootballRepository.MatchdayCallback() {
            @Override public void onResult(int matchday) { currentMatchday.postValue(matchday); }
            @Override public void onError(Throwable t) { /* mantenemos valor actual */ }
        });
    }

    private void rebuildSources(@Nullable Integer md) {
        // Limpiar fuentes previas
        if (sourceMd != null) homeMatches.removeSource(sourceMd);
        if (sourceRange != null) homeMatches.removeSource(sourceRange);
        if (sourceFinished != null) homeMatches.removeSource(sourceFinished);
        sourceMd = null; sourceRange = null; sourceFinished = null;

        // No modificamos el esquema (no hay columna matchday fiable). Aunque conozcamos el número, usamos fecha como criterio.
        attachRangeFallback();
    }

    private void attachRangeFallback() {
        long now = System.currentTimeMillis();
        long sevenDays = now + 7L * 24L * 60L * 60L * 1000L;
        sourceRange = repository.getMatchesInRangeLive(now, sevenDays);
        homeMatches.addSource(sourceRange, list -> {
            if (list != null && !list.isEmpty()) {
                homeMatches.setValue(list);
            } else {
                attachFinishedFallback();
            }
        });
    }

    private void attachFinishedFallback() {
        sourceFinished = repository.getLastFinishedLive(10);
        homeMatches.addSource(sourceFinished, list -> {
            // Puede estar vacío; devolvemos lo que haya (vacío significa que aún no tenemos nada en DB)
            homeMatches.setValue(list != null ? list : java.util.Collections.emptyList());
        });
    }
}
