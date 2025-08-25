package com.example.housemanager.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.housemanager.R;
import com.example.housemanager.ui.leagues.LeagueDetailActivity;
import com.example.housemanager.ui.leagues.LeagueManager;

import java.util.List;

// Muestra nombre, tipo, participantes y estado. Al pulsar, abre el detalle.
public class LeaguesAdapter extends RecyclerView.Adapter<LeaguesAdapter.LeagueViewHolder> {

    private final List<LeagueManager.League> leagues;
    private final List<Integer> leagueIds;
    private final Context context;

    public LeaguesAdapter(List<LeagueManager.League> leagues, List<Integer> leagueIds, Context context) {
        this.leagues = leagues;
        this.leagueIds = leagueIds;
        this.context = context;
    }

    @NonNull
    @Override
    public LeagueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_league, parent, false);
        return new LeagueViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LeagueViewHolder h, int position) {
        LeagueManager.League league = leagues.get(position);

        h.tvLeagueName.setText(league.getName());
        h.tvLeagueDescription.setText(league.getType());
        h.tvParticipants.setText(league.getParticipants() + " participantes");
        h.tvStatus.setText(league.getStatus());

        // Al pulsar, abro detalle con datos mínimos.
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, LeagueDetailActivity.class);
            i.putExtra("EXTRA_NAME", league.getName());
            i.putExtra("EXTRA_TYPE", league.getType());
            i.putExtra("EXTRA_BUDGET", league.getBudget());
            i.putExtra("EXTRA_MARKET_HOUR", league.getMarketHour());
            i.putExtra("EXTRA_TEAM_TYPE", league.getTeamType());
            i.putExtra("EXTRA_PARTICIPANTS", league.getParticipants());
            // Pasar el id real de la liga si está disponible
            if (leagueIds != null && position < leagueIds.size()) {
                i.putExtra(LeagueDetailActivity.EXTRA_LEAGUE_ID, leagueIds.get(position).longValue());
            }
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() { return leagues.size(); }

    static class LeagueViewHolder extends RecyclerView.ViewHolder {
        TextView tvLeagueName, tvLeagueDescription, tvParticipants, tvStatus;
        LeagueViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLeagueName = itemView.findViewById(R.id.tv_league_name);
            tvLeagueDescription = itemView.findViewById(R.id.tv_league_description);
            tvParticipants = itemView.findViewById(R.id.tv_participants);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}
