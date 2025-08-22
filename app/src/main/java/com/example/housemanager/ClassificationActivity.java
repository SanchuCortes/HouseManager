package com.example.housemanager;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.housemanager.repository.models.ManagerScore;
import com.example.housemanager.ui.adapters.ManagerScoreAdapter;
import com.example.housemanager.viewmodel.FootballViewModel;
import com.example.housemanager.repository.FootballRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Muestra clasificación por managers (temporada por defecto).
 */
public class ClassificationActivity extends AppCompatActivity {

    public static final String EXTRA_LEAGUE_ID = "EXTRA_LEAGUE_ID";

    private long leagueId = 1L;

    private FootballViewModel viewModel;
    private ManagerScoreAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classification);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Clasificación");
        }

        leagueId = getIntent().getLongExtra(EXTRA_LEAGUE_ID, 1L);
        if (leagueId == 0L) leagueId = 1L;

        // Recycler
        RecyclerView rv = findViewById(R.id.recycler_scores);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManagerScoreAdapter();
        rv.setAdapter(adapter);

        // ViewModel + Repository
        viewModel = new ViewModelProvider(this).get(FootballViewModel.class);

        // Clasificación de temporada por defecto
        FootballRepository repo = FootballRepository.getInstance(getApplicationContext());
        androidx.lifecycle.LiveData<java.util.List<com.example.housemanager.repository.models.ManagerScore>>[] current = new androidx.lifecycle.LiveData[1];
        current[0] = repo.getLeagueClassificationSeason(leagueId);
        current[0].observe(this, scores -> {
            if (scores == null) scores = new ArrayList<>();
            adapter.submit(scores);
        });

        // Toggle Temporada/Jornada
        android.view.View btnSeason = findViewById(R.id.btn_season);
        android.view.View btnMatchday = findViewById(R.id.btn_matchday);
        if (btnSeason != null) {
            btnSeason.setOnClickListener(v -> {
                if (current[0] != null) current[0].removeObservers(this);
                current[0] = repo.getLeagueClassificationSeason(leagueId);
                current[0].observe(this, scores -> {
                    if (scores == null) scores = new ArrayList<>();
                    adapter.submit(scores);
                });
            });
        }
        if (btnMatchday != null) {
            btnMatchday.setOnClickListener(v -> {
                // Obtener jornada actual y observar clasificación de jornada
                repo.getCurrentMatchday(new FootballRepository.MatchdayCallback() {
                    @Override public void onResult(int matchday) {
                        runOnUiThread(() -> {
                            if (current[0] != null) current[0].removeObservers(ClassificationActivity.this);
                            current[0] = repo.getLeagueClassificationThisMatchday(leagueId, matchday);
                            current[0].observe(ClassificationActivity.this, scores -> {
                                if (scores == null) scores = new ArrayList<>();
                                adapter.submit(scores);
                            });
                        });
                    }
                    @Override public void onError(Throwable t) {
                        // fallback: mantener temporada
                    }
                });
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
