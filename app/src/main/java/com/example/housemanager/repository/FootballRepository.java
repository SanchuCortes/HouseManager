package com.example.housemanager.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.housemanager.BuildConfig;
import com.example.housemanager.api.FootballApiService;
import com.example.housemanager.api.models.PlayerAPI;
import com.example.housemanager.api.models.TeamAPI;
import com.example.housemanager.api.models.TeamsResponse;
import com.example.housemanager.database.HouseManagerDatabase;
import com.example.housemanager.database.dao.PlayerDao;
import com.example.housemanager.database.dao.TeamDao;
import com.example.housemanager.database.entities.PlayerEntity;
import com.example.housemanager.database.entities.TeamEntity;
import com.example.housemanager.market.Player;
import com.example.housemanager.market.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FootballRepository {

    public interface SyncCallback {
        void onProgress(String message);
        void onSuccess(String message);
        void onError(String error);
    }

    private static final String TAG = "FootballRepository";
    private static final String BASE_URL = "https://api.football-data.org/v4/";

    private static FootballRepository INSTANCE;

    private final FootballApiService api;
    private final TeamDao teamDao;
    private final PlayerDao playerDao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private final LiveData<List<Team>> teamsLive;
    private final LiveData<List<Player>> playersLive;

    private FootballRepository(Context ctx) {
        HouseManagerDatabase db = HouseManagerDatabase.getInstance(ctx);
        teamDao = db.teamDao();
        playerDao = db.playerDao();

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor((Interceptor) chain -> {
                    Request o = chain.request();
                    Request req = o.newBuilder()
                            .header("X-Auth-Token", BuildConfig.FOOTBALL_API_KEY != null ? BuildConfig.FOOTBALL_API_KEY : "")
                            .method(o.method(), o.body())
                            .build();
                    return chain.proceed(req);
                }).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(FootballApiService.class);

        teamsLive = Transformations.map(teamDao.getAll(), list -> {
            List<Team> out = new ArrayList<>();
            if (list != null) for (TeamEntity e : list) {
                Team t = new Team();
                t.setTeamId(e.teamId);
                t.setName(e.name);
                t.setCrestUrl(e.crestUrl);
                out.add(t);
            }
            return out;
        });

        playersLive = Transformations.map(playerDao.getAll(), list -> {
            List<Player> out = new ArrayList<>();
            if (list != null) for (PlayerEntity e : list) {
                Player p = new Player();
                p.setPlayerId(e.playerId);
                p.setName(e.name);
                p.setTeamId(e.teamId);
                p.setTeamName(e.teamName);
                p.setNationality(e.nationality);
                p.setPosition(e.position);
                p.setCurrentPrice(e.currentPrice);
                p.setTotalPoints(e.totalPoints);
                p.setAvailable(e.available);
                out.add(p);
            }
            return out;
        });
    }

    public static synchronized FootballRepository getInstance(Context ctx) {
        if (INSTANCE == null) INSTANCE = new FootballRepository(ctx.getApplicationContext());
        return INSTANCE;
    }

    // ==== usados por tu Activity ====
    public LiveData<List<Team>> getAllTeams()    { return teamsLive; }
    public LiveData<List<Player>> getAllPlayers(){ return playersLive; }

    // ==== sync con API -> guarda en Room ====
    public void syncLaLigaTeams(SyncCallback cb) {
        if (cb != null) cb.onProgress("Descargando equipos...");
        api.getLaLigaTeams().enqueue(new Callback<TeamsResponse>() {
            @Override public void onResponse(Call<TeamsResponse> call, Response<TeamsResponse> r) {
                if (!r.isSuccessful() || r.body() == null) {
                    if (cb != null) cb.onError("Error equipos: " + r.code());
                    return;
                }
                List<TeamAPI> apiTeams = r.body().getTeams();

                io.execute(() -> {
                    teamDao.clear();
                    playerDao.clear();
                    List<TeamEntity> batch = new ArrayList<>();
                    for (TeamAPI t : apiTeams) {
                        TeamEntity e = new TeamEntity();
                        e.teamId = t.getId();
                        e.name = t.getName() != null ? t.getName() : "";
                        e.crestUrl = t.getCrest();
                        batch.add(e);
                    }
                    teamDao.insertAll(batch);
                });

                if (cb != null) cb.onProgress("Equipos guardados. Cargando plantillas...");
                fetchSquads(apiTeams, cb);
            }

            @Override public void onFailure(Call<TeamsResponse> call, Throwable t) {
                if (cb != null) cb.onError("Fallo equipos: " + t.getMessage());
            }
        });
    }

    private void fetchSquads(List<TeamAPI> teams, SyncCallback cb) {
        final int total = teams.size();
        final int[] done = {0};

        for (TeamAPI t : teams) {
            api.getTeamDetails(t.getId()).enqueue(new Callback<TeamAPI>() {
                @Override public void onResponse(Call<TeamAPI> call, Response<TeamAPI> r) {
                    done[0]++;
                    if (r.isSuccessful() && r.body() != null) {
                        TeamAPI detail = r.body();
                        List<PlayerAPI> squad = detail.getSquad(); // asegúrate de tener getSquad() en tu modelo

                        if (squad != null) {
                            List<PlayerEntity> batch = new ArrayList<>();
                            for (PlayerAPI p : squad) {
                                PlayerEntity e = new PlayerEntity();
                                e.playerId = p.getId();
                                e.name = p.getName() != null ? p.getName() : "";
                                e.teamId = detail.getId();
                                e.teamName = detail.getName() != null ? detail.getName() : "";
                                e.nationality = p.getNationality() != null ? p.getNationality() : "";
                                e.position = mapPosition(p.getPosition());
                                e.currentPrice = 5.0;
                                e.totalPoints = 0;
                                e.available = true;
                                batch.add(e);
                            }
                            io.execute(() -> playerDao.insertAll(batch));
                        }
                    }
                    if (cb != null) cb.onProgress("Plantillas: " + done[0] + "/" + total);
                    if (done[0] == total && cb != null) cb.onSuccess("Sincronización completada.");
                }

                @Override public void onFailure(Call<TeamAPI> call, Throwable t) {
                    done[0]++;
                    Log.w(TAG, "Fallo plantilla " + t.getMessage());
                    if (cb != null) cb.onProgress("Plantillas: " + done[0] + "/" + total + " (con errores)");
                    if (done[0] == total && cb != null) cb.onSuccess("Sincronización completada (con algunos fallos).");
                }
            });
        }
    }

    private String mapPosition(String apiPos) {
        if (apiPos == null) return "MID";
        switch (apiPos) {
            case "Goalkeeper": return "GK";
            case "Defence":    return "DEF";
            case "Midfield":   return "MID";
            case "Offence":    return "FWD";
            default:           return "MID";
        }
    }
}
