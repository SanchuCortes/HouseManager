package com.example.housemanager;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

// Muestra el escudo, nombre y la plantilla del equipo
public class TeamDetailActivity extends AppCompatActivity {

    private FootballViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);

        // Toolbar con back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int teamId = getIntent().getIntExtra("EXTRA_TEAM_ID", -1);
        String teamName = getIntent().getStringExtra("EXTRA_TEAM_NAME");
        String crest = getIntent().getStringExtra("EXTRA_TEAM_CREST");

        if (getSupportActionBar() != null) getSupportActionBar().setTitle(teamName);

        ImageView ivCrest = findViewById(R.id.iv_crest);
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(teamName);

        if (crest != null && !crest.isEmpty()) {
            Glide.with(this).load(crest).into(ivCrest);
        }

        RecyclerView rv = findViewById(R.id.recycler_squad);
        rv.setLayoutManager(new LinearLayoutManager(this));
        PlayersSimpleAdapter adapter = new PlayersSimpleAdapter();
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(FootballViewModel.class);
        vm.getSquad().observe(this, adapter::submit);
        if (teamId != -1) vm.loadSquad(teamId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
