package com.example.housemanager;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LeaguesAdapter extends RecyclerView.Adapter<LeaguesAdapter.LeagueViewHolder> {

    private List<LeagueManager.League> leagues;
    private Context context;

    public LeaguesAdapter(List<LeagueManager.League> leagues, Context context) {
        this.leagues = leagues;
        this.context = context;
    }

    @NonNull
    @Override
    public LeagueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_league, parent, false);
        return new LeagueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeagueViewHolder holder, int position) {
        LeagueManager.League league = leagues.get(position);
        Context context = holder.itemView.getContext();

        holder.tvLeagueName.setText(league.getName());
        holder.tvLeagueDescription.setText(league.getType()); // Tipo de liga como descripción
        holder.tvParticipants.setText(league.getParticipants());
        holder.tvStatus.setText(league.getStatus());

        // Cambiar icono según el tipo de liga
        if (league.isPrivate()) {
            holder.ivLeagueIcon.setImageResource(R.drawable.ic_person);
            // Tint verde para ligas privadas
            holder.ivLeagueIcon.setColorFilter(ContextCompat.getColor(context, R.color.secondary_green));
        } else {
            holder.ivLeagueIcon.setImageResource(R.drawable.ic_group_add);
            // Tint dorado para ligas comunitarias
            holder.ivLeagueIcon.setColorFilter(ContextCompat.getColor(context, R.color.accent_gold));
        }

        // Cambiar color según el estado
        if ("Activa".equals(league.getStatus())) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.secondary_green));
        } else {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.gray_medium));
        }

        // Click listener para navegar a detalles de la liga
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, LeagueDetailActivity.class);
            intent.putExtra("league_name", league.getName());
            intent.putExtra("league_type", league.getType());
            intent.putExtra("is_private", league.isPrivate());
            intent.putExtra("budget", league.getBudget());
            intent.putExtra("participants", league.getParticipants());
            intent.putExtra("market_hour", league.getMarketHour());
            intent.putExtra("team_type", league.getTeamType());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return leagues.size();
    }

    static class LeagueViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLeagueIcon;
        TextView tvLeagueName;
        TextView tvLeagueDescription;
        TextView tvParticipants;
        TextView tvStatus;

        public LeagueViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLeagueIcon = itemView.findViewById(R.id.iv_league_icon);
            tvLeagueName = itemView.findViewById(R.id.tv_league_name);
            tvLeagueDescription = itemView.findViewById(R.id.tv_league_description);
            tvParticipants = itemView.findViewById(R.id.tv_participants);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}