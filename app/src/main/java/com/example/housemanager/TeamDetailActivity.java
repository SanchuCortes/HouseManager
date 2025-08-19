package com.example.housemanager;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.housemanager.viewmodel.FootballViewModel;

public class TeamDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TEAM_ID = "team_id";

    private FootballViewModel vm;
    private PlayersSimpleAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int layoutId = getResources().getIdentifier("activity_team_detail", "layout", getPackageName());
        RecyclerView rv;

        if (layoutId != 0) {
            setContentView(layoutId);

            int rvId = getResources().getIdentifier("recycler_squad", "id", getPackageName());
            if (rvId == 0) {
                // alternativa común en tu proyecto
                rvId = getResources().getIdentifier("recycler_players", "id", getPackageName());
            }

            if (rvId != 0) {
                rv = findViewById(rvId);
            } else {
                // si el layout no tiene RecyclerView con esos ids, lo creamos y añadimos
                rv = new RecyclerView(this);
                rv.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                ((ViewGroup) findViewById(android.R.id.content)).addView(rv);
            }
        } else {
            // no existe activity_team_detail.xml: pantalla 100% programática
            rv = new RecyclerView(this);
            rv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            setContentView(rv);
        }

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlayersSimpleAdapter(); // sin callback: sólo mostramos la plantilla del equipo
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(FootballViewModel.class);
        vm.getSquad().observe(this, adapter::submit);

        int teamId = getIntent().getIntExtra(EXTRA_TEAM_ID, -1);
        if (teamId == -1) {
            // por compatibilidad con otros sitios donde el extra pueda llamarse "TEAM_ID"
            teamId = getIntent().getIntExtra("TEAM_ID", -1);
        }
        if (teamId != -1) {
            vm.loadSquad(teamId);
        }

        setTitle("Plantilla del equipo");
    }
}
