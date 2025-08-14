package com.example.housemanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransferMarketAdapter extends RecyclerView.Adapter<TransferMarketAdapter.PlayerViewHolder> {

    private Context context;
    private List<Player> players = new ArrayList<>();
    private OnPlayerClickListener listener;

    public interface OnPlayerClickListener {
        void onPlayerClick(Player player);
        void onBuyPlayerClick(Player player);
    }

    public TransferMarketAdapter(Context context, OnPlayerClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void updatePlayers(List<Player> newPlayers) {
        this.players.clear();
        if (newPlayers != null) {
            this.players.addAll(newPlayers);
        }
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
        holder.bind(player);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    class PlayerViewHolder extends RecyclerView.ViewHolder {
        private TextView textPlayerName;
        private TextView textTeamName;
        private TextView textPosition;
        private TextView textNationality;
        private TextView textShirtNumber;
        private TextView textPrice;
        private TextView textPoints;
        private TextView textMatches;
        private TextView textAverage;
        private TextView textUnavailable;
        private ImageView imagePlayer;
        private Button btnBuyPlayer;
        private View layoutPositionStripe;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);

            // Inicializar vistas
            textPlayerName = itemView.findViewById(R.id.textPlayerName);
            textTeamName = itemView.findViewById(R.id.textTeamName);
            textPosition = itemView.findViewById(R.id.textPosition);
            textNationality = itemView.findViewById(R.id.textNationality);
            textShirtNumber = itemView.findViewById(R.id.textShirtNumber);
            textPrice = itemView.findViewById(R.id.textPrice);
            textPoints = itemView.findViewById(R.id.textPoints);
            textMatches = itemView.findViewById(R.id.textMatches);
            textAverage = itemView.findViewById(R.id.textAverage);
            textUnavailable = itemView.findViewById(R.id.textUnavailable);
            imagePlayer = itemView.findViewById(R.id.imagePlayer);
            btnBuyPlayer = itemView.findViewById(R.id.btnBuyPlayer);
            layoutPositionStripe = itemView.findViewById(R.id.layoutPositionStripe);
        }

        public void bind(Player player) {
            // Información básica del jugador
            if (textPlayerName != null) {
                textPlayerName.setText(player.getName());
            }

            if (textTeamName != null) {
                textTeamName.setText(player.getTeamName());
            }

            if (textPosition != null) {
                textPosition.setText(getPositionDisplayName(player.getPosition()));
            }

            if (textNationality != null) {
                textNationality.setText(player.getNationality());
            }

            // Número de camiseta
            if (textShirtNumber != null) {
                if (player.getShirtNumber() != null && player.getShirtNumber() > 0) {
                    textShirtNumber.setText(String.valueOf(player.getShirtNumber()));
                    textShirtNumber.setVisibility(View.VISIBLE);
                } else {
                    textShirtNumber.setVisibility(View.GONE);
                }
            }

            // Precio del jugador
            if (textPrice != null) {
                textPrice.setText(String.format(Locale.getDefault(), "%.1f M€", player.getCurrentPrice()));
            }

            // Puntos totales
            if (textPoints != null) {
                textPoints.setText(String.format(Locale.getDefault(), "%d pts", player.getTotalPoints()));
            }

            // Partidos jugados
            if (textMatches != null) {
                textMatches.setText(String.format(Locale.getDefault(), "%d PJ", player.getMatchesPlayed()));
            }

            // Promedio por partido
            if (textAverage != null) {
                double average = player.getMatchesPlayed() > 0 ?
                        (double) player.getTotalPoints() / player.getMatchesPlayed() : 0.0;
                textAverage.setText(String.format(Locale.getDefault(), "%.1f", average));
            }

            // Color de fondo según posición
            setPositionBackgroundColor(player.getPosition());

            // Estado de disponibilidad
            if (player.isAvailable()) {
                itemView.setAlpha(1.0f);
                if (btnBuyPlayer != null) {
                    btnBuyPlayer.setEnabled(true);
                }
                if (textUnavailable != null) {
                    textUnavailable.setVisibility(View.GONE);
                }
            } else {
                itemView.setAlpha(0.6f);
                if (btnBuyPlayer != null) {
                    btnBuyPlayer.setEnabled(false);
                }
                if (textUnavailable != null) {
                    textUnavailable.setVisibility(View.VISIBLE);
                }
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayerClick(player);
                }
            });

            if (btnBuyPlayer != null) {
                btnBuyPlayer.setOnClickListener(v -> {
                    if (listener != null && player.isAvailable()) {
                        listener.onBuyPlayerClick(player);
                    }
                });
            }

            // Foto del jugador (placeholder por ahora)
            loadPlayerImage(player);
        }

        private String getPositionDisplayName(String position) {
            if (position == null) return "?";

            switch (position) {
                case "GK": return "Portero";
                case "DEF": return "Defensa";
                case "MID": return "Medio";
                case "FWD": return "Delantero";
                default: return position;
            }
        }

        private void setPositionBackgroundColor(String position) {
            if (layoutPositionStripe == null || position == null) return;

            int colorRes;
            switch (position) {
                case "GK":
                    colorRes = R.color.position_gk_light;
                    break;
                case "DEF":
                    colorRes = R.color.position_def_light;
                    break;
                case "MID":
                    colorRes = R.color.position_mid_light;
                    break;
                case "FWD":
                    colorRes = R.color.position_fwd_light;
                    break;
                default:
                    colorRes = R.color.card_background;
                    break;
            }

            try {
                int color = ContextCompat.getColor(context, colorRes);
                layoutPositionStripe.setBackgroundColor(color);
            } catch (Exception e) {
                // Si no existe el color, usar un color por defecto
                layoutPositionStripe.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_medium));
            }
        }

        private void loadPlayerImage(Player player) {
            if (imagePlayer == null) return;

            // Por ahora usar placeholder basado en posición
            int placeholderRes = getPositionPlaceholder(player.getPosition());
            imagePlayer.setImageResource(placeholderRes);

            // En el futuro se puede integrar con APIs de imágenes de jugadores
            // usando Glide o similar
        }

        private int getPositionPlaceholder(String position) {
            if (position == null) return R.drawable.ic_player_placeholder;

            switch (position) {
                case "GK": return R.drawable.ic_goalkeeper;
                case "DEF": return R.drawable.ic_defender;
                case "MID": return R.drawable.ic_midfielder;
                case "FWD": return R.drawable.ic_forward;
                default: return R.drawable.ic_player_placeholder;
            }
        }
    }
}