package com.example.housemanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.PlayerViewHolder> {

    private List<MyTeamActivity.Player> playersList;
    private Context context;

    public PlayersAdapter(List<MyTeamActivity.Player> playersList, Context context) {
        this.playersList = playersList;
        this.context = context;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_player, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        MyTeamActivity.Player player = playersList.get(position);

        // Datos básicos del jugador
        holder.tvPlayerName.setText(player.getName());
        holder.tvPlayerTeam.setText(player.getTeam());
        holder.tvPlayerPosition.setText(player.getPosition());
        holder.tvPlayerPrice.setText(String.format("%.1fM €", player.getPrice()));

        // Icono según la posición
        setPositionIcon(holder.ivPosition, player.getPosition());

        // Indicador si está jugando (titularidad)
        if (player.isPlaying()) {
            holder.ivPlaying.setVisibility(View.VISIBLE);
            holder.ivPlaying.setImageResource(R.drawable.ic_add);
        } else {
            holder.ivPlaying.setVisibility(View.INVISIBLE);
        }

        // Indicador si es capitán
        if (player.isCaptain()) {
            holder.ivCaptain.setVisibility(View.VISIBLE);
            holder.ivCaptain.setImageResource(R.drawable.ic_trophy);
        } else {
            holder.ivCaptain.setVisibility(View.INVISIBLE);
        }
    }

    // Asignamos el icono según la posición del jugador
    private void setPositionIcon(ImageView imageView, String position) {
        switch (position) {
            case "POR":
                imageView.setImageResource(R.drawable.ic_person);
                break;
            case "DEF":
                imageView.setImageResource(R.drawable.ic_home);
                break;
            case "CEN":
                imageView.setImageResource(R.drawable.ic_settings);
                break;
            case "DEL":
                imageView.setImageResource(R.drawable.ic_arrow_forward);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_person);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return playersList.size();
    }

    // ViewHolder para cada jugador
    public static class PlayerViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlayerName, tvPlayerTeam, tvPlayerPosition, tvPlayerPrice;
        ImageView ivPosition, ivPlaying, ivCaptain;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPlayerName = itemView.findViewById(R.id.tv_player_name);
            tvPlayerTeam = itemView.findViewById(R.id.tv_player_team);
            tvPlayerPosition = itemView.findViewById(R.id.tv_player_position);
            tvPlayerPrice = itemView.findViewById(R.id.tv_player_price);
            ivPosition = itemView.findViewById(R.id.iv_position);
            ivPlaying = itemView.findViewById(R.id.iv_playing);
            ivCaptain = itemView.findViewById(R.id.iv_captain);
        }
    }
}