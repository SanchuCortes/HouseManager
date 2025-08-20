package com.example.housemanager.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.housemanager.R;
import com.example.housemanager.market.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter simple para mostrar jugadores en el mercado de fichajes
 */
public class MarketAdapter extends RecyclerView.Adapter<MarketAdapter.PlayerViewHolder> {

    public interface OnPlayerClickListener {
        void onPlayerClick(Player player);
        void onBuyPlayerClick(Player player);
    }

    private final OnPlayerClickListener listener;
    private List<Player> players = new ArrayList<>();

    public MarketAdapter(OnPlayerClickListener listener) {
        this.listener = listener;
    }

    public void updatePlayers(List<Player> newPlayers) {
        players = newPlayers != null ? new ArrayList<>(newPlayers) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transfer_player, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Player player = players.get(position);
        holder.bind(player, listener);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    static class PlayerViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvTeam;
        private final TextView tvPosition;
        private final TextView tvPrice;
        private final TextView tvPoints;
        private final Button btnBuy;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTeam = itemView.findViewById(R.id.tv_team);
            tvPosition = itemView.findViewById(R.id.tv_pos);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvPoints = itemView.findViewById(R.id.tv_points); // Añadiremos este campo al layout
            btnBuy = itemView.findViewById(R.id.btn_buy);
        }

        public void bind(Player player, OnPlayerClickListener listener) {
            // Información básica del jugador
            tvName.setText(player.getName());
            tvTeam.setText(player.getTeamName());
            tvPosition.setText(getPositionShort(player.getPosition()));
            tvPrice.setText(String.format("%.1fM €", player.getCurrentPrice()));

            // Mostrar puntos si está disponible el TextView
            if (tvPoints != null) {
                tvPoints.setText(player.getTotalPoints() + " pts");
            }

            // Click en el item completo para ver detalles
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayerClick(player);
                }
            });

            // Click en el botón para fichar directamente
            btnBuy.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBuyPlayerClick(player);
                }
            });
        }

        /**
         * Convierte posición completa a abreviatura
         */
        private String getPositionShort(String position) {
            if (position == null) return "MED";

            switch (position) {
                case "Portero": return "POR";
                case "Defensa": return "DEF";
                case "Medio": return "MED";
                case "Delantero": return "DEL";
                default: return position.substring(0, Math.min(3, position.length())).toUpperCase();
            }
        }
    }
}