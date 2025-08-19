package com.example.housemanager;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.housemanager.market.Player;
import com.example.housemanager.market.Team;
import com.example.housemanager.repository.FootballRepository;
import com.example.housemanager.viewmodel.FootballViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransferMarketActivity extends AppCompatActivity {

    private FootballRepository repository;

    private RecyclerView recyclerView;
    private MarketAdapter adapter;

    private EditText etSearch;
    private Spinner spnTeam;
    private Spinner spnPosition;

    private final List<Player> allPlayers = new ArrayList<>();
    private final List<Team> allTeams = new ArrayList<>();

    private final String[] positions = new String[]{"Todas", "GK", "DEF", "MID", "FWD"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = FootballRepository.getInstance(this);

        // UI simple programática: barra de filtros + recycler
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout filters = new LinearLayout(this);
        filters.setOrientation(LinearLayout.HORIZONTAL);
        filters.setPadding(dp(12), dp(12), dp(12), dp(8));
        filters.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        etSearch = new EditText(this);
        etSearch.setHint("Buscar jugador");
        LinearLayout.LayoutParams lpSearch = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        etSearch.setLayoutParams(lpSearch);

        spnTeam = new Spinner(this);
        LinearLayout.LayoutParams lpTeam = new LinearLayout.LayoutParams(dp(0), ViewGroup.LayoutParams.WRAP_CONTENT, 0.8f);
        lpTeam.leftMargin = dp(8);
        spnTeam.setLayoutParams(lpTeam);

        spnPosition = new Spinner(this);
        LinearLayout.LayoutParams lpPos = new LinearLayout.LayoutParams(dp(0), ViewGroup.LayoutParams.WRAP_CONTENT, 0.6f);
        lpPos.leftMargin = dp(8);
        spnPosition.setLayoutParams(lpPos);

        filters.addView(etSearch);
        filters.addView(spnTeam);
        filters.addView(spnPosition);

        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        root.addView(filters);
        root.addView(recyclerView);
        setContentView(root);

        // Adapters para filtros
        ArrayAdapter<String> posAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, positions);
        spnPosition.setAdapter(posAdapter);

        ArrayAdapter<String> teamsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spnTeam.setAdapter(teamsAdapter);

        // Adapter del listado
        adapter = new MarketAdapter(new ArrayList<>(), new MarketAdapter.OnBuyClick() {
            @Override public void onBuy(Player p) { confirmBuy(p); }
        });
        recyclerView.setAdapter(adapter);

        // Observers de datos
        repository.getAllPlayers().observe(this, players -> {
            allPlayers.clear();
            if (players != null) allPlayers.addAll(players);
            applyFilters();
        });

        repository.getAllTeams().observe(this, teams -> {
            allTeams.clear();
            if (teams != null) allTeams.addAll(teams);
            // refrescamos spinner de equipos
            List<String> items = new ArrayList<>();
            items.add("Todos");
            for (Team t : allTeams) items.add(t.getName());
            teamsAdapter.clear();
            teamsAdapter.addAll(items);
            teamsAdapter.notifyDataSetChanged();
        });

        // Búsqueda y filtros
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        spnTeam.setOnItemSelectedListener(new SimpleItemSelected(() -> applyFilters()));
        spnPosition.setOnItemSelectedListener(new SimpleItemSelected(() -> applyFilters()));

        // Si querías forzar una sync:
        repository.syncLaLigaTeams(null);
    }

    private void applyFilters() {
        String q = etSearch.getText() != null ? etSearch.getText().toString().trim().toLowerCase(Locale.ROOT) : "";
        String posSel = (String) spnPosition.getSelectedItem();
        String teamSel = (String) spnTeam.getSelectedItem();

        List<Player> filtered = new ArrayList<>();
        for (Player p : allPlayers) {
            boolean ok = true;

            if (!q.isEmpty()) {
                String haystack = (p.getName() + " " + p.getTeamName()).toLowerCase(Locale.ROOT);
                ok &= haystack.contains(q);
            }

            if (posSel != null && !"Todas".equals(posSel)) {
                ok &= posSel.equalsIgnoreCase(p.getPosition());
            }

            if (teamSel != null && !"Todos".equals(teamSel)) {
                ok &= teamSel.equalsIgnoreCase(p.getTeamName());
            }

            if (ok) filtered.add(p);
        }
        adapter.submit(filtered);
    }

    private void confirmBuy(Player p) {
        new AlertDialog.Builder(this)
                .setTitle("Comprar jugador")
                .setMessage("¿Quieres fichar a " + p.getName() + " por " + p.getCurrentPrice() + " €?")
                .setPositiveButton("Comprar", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(TransferMarketActivity.this, "Has comprado a " + p.getName(), Toast.LENGTH_SHORT).show();
                        // Aquí podrías marcarlo como no disponible en Room, o moverlo a tu equipo.
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    // ---------------------------------------------------------
    // Utilidad para no escribir un listener por cada spinner
    // ---------------------------------------------------------
    private static class SimpleItemSelected implements android.widget.AdapterView.OnItemSelectedListener {
        private final Runnable onSelected;
        SimpleItemSelected(Runnable r) { this.onSelected = r; }
        @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { onSelected.run(); }
        @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
    }

    // ---------------------------------------------------------
    // Adaptador interno del mercado con botón "Comprar"
    // ---------------------------------------------------------
    private static class MarketAdapter extends RecyclerView.Adapter<MarketAdapter.VH> {

        interface OnBuyClick { void onBuy(Player p); }

        private final List<Player> data;
        private final OnBuyClick onBuy;

        MarketAdapter(List<Player> data, OnBuyClick onBuy) {
            this.data = data;
            this.onBuy = onBuy;
        }

        void submit(List<Player> players) {
            data.clear();
            if (players != null) data.addAll(players);
            notifyDataSetChanged();
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout row = new LinearLayout(parent.getContext());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dpStatic(parent, 12), dpStatic(parent, 10), dpStatic(parent, 12), dpStatic(parent, 10));
            row.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView tvName = new TextView(parent.getContext());
            tvName.setTextSize(16);
            tvName.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView tvMeta = new TextView(parent.getContext());
            tvMeta.setTextSize(13);

            TextView btnBuy = new TextView(parent.getContext());
            btnBuy.setText("Comprar");
            btnBuy.setGravity(Gravity.CENTER);
            btnBuy.setPadding(dpStatic(parent, 10), dpStatic(parent, 6), dpStatic(parent, 10), dpStatic(parent, 6));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = dpStatic(parent, 6);
            btnBuy.setLayoutParams(lp);
            btnBuy.setBackgroundResource(android.R.drawable.btn_default);

            row.addView(tvName);
            row.addView(tvMeta);
            row.addView(btnBuy);

            return new VH(row, tvName, tvMeta, btnBuy);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int position) {
            Player p = data.get(position);
            h.tvName.setText(p.getName());
            String meta = p.getPosition() + " · " + (p.getTeamName() == null ? "-" : p.getTeamName()) + " · " + p.getCurrentPrice() + " € · " + p.getTotalPoints() + " pts";
            h.tvMeta.setText(meta);
            h.btnBuy.setOnClickListener(v -> {
                if (onBuy != null) onBuy.onBuy(p);
            });
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvName;
            final TextView tvMeta;
            final TextView btnBuy;
            VH(@NonNull View itemView, TextView tvName, TextView tvMeta, TextView btnBuy) {
                super(itemView);
                this.tvName = tvName;
                this.tvMeta = tvMeta;
                this.btnBuy = btnBuy;
            }
        }

        private static int dpStatic(ViewGroup parent, int v) {
            return (int) (v * parent.getResources().getDisplayMetrics().density);
        }
    }
}
