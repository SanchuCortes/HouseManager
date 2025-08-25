package com.example.housemanager.ui.team;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.housemanager.R;
import com.example.housemanager.api.models.PlayerAPI;

import java.util.ArrayList;
import java.util.List;

public class PlayersSimpleAdapter extends RecyclerView.Adapter<PlayersSimpleAdapter.VH> {

    private boolean clauseEnabled = false;
    private int clauseBlockDays = 14;
    private final java.util.Map<Integer, Long> ownershipTimes = new java.util.HashMap<>();

    public void setClauseRules(boolean enabled, int blockDays) {
        this.clauseEnabled = enabled;
        this.clauseBlockDays = blockDays;
        notifyDataSetChanged();
    }

    public void setOwnershipTimes(java.util.List<com.example.housemanager.database.pojo.OwnershipTime> times) {
        ownershipTimes.clear();
        if (times != null) {
            for (com.example.housemanager.database.pojo.OwnershipTime t : times) {
                ownershipTimes.put((int) t.playerId, t.acquiredAtMillis);
            }
        }
        notifyDataSetChanged();
    }

    // interfaz para cuando tocan un jugador
    public interface OnPlayerClick {
        void onPlayerClick(PlayerAPI player);
    }

    private final List<PlayerAPI> data = new ArrayList<>();
    private OnPlayerClick listener;
    private int captainId = -1; // para marcar quien es el capitan

    // constructor sin listener por si no necesitamos clicks
    public PlayersSimpleAdapter() {
        this.listener = null;
    }

    // constructor con listener para manejar clicks
    public PlayersSimpleAdapter(OnPlayerClick listener) {
        this.listener = listener;
    }

    // actualiza la lista de jugadores
    public void submit(List<PlayerAPI> players) {
        data.clear();
        if (players != null) {
            data.addAll(players);
        }
        notifyDataSetChanged(); // avisa al recycler que hay datos nuevos
    }

    // marca quien es el capitan para ponerle la estrella
    public void setCaptainId(int id) {
        captainId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflamos el layout simple de jugador
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player_simple, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        PlayerAPI player = data.get(position);

        // ponemos el nombre y si es capitan le añadimos la estrella
        String name = player.getName() != null ? player.getName() : "Jugador sin nombre";
        if (player.getId() == captainId) {
            name = "⭐ " + name; // estrella para el capitan
        }
        holder.tvName.setText(name);

        // ponemos la posicion del jugador
        String pos = player.getPosition() != null ? player.getPosition() : "–";
        holder.tvPos.setText(pos);

        // puntos del jugador (mapeado desde PlayerEntity.totalPoints)
        int pts = player.getPoints();
        holder.tvPoints.setText(pts + " pts");

        // Clausular: mostrar botón y contador si la regla está activa
        if (holder.btnClause != null) {
            if (!clauseEnabled) {
                holder.btnClause.setVisibility(View.GONE);
            } else {
                holder.btnClause.setVisibility(View.VISIBLE);
                holder.btnClause.setEnabled(false); // en Mi Equipo es informativo
                Long acquired = ownershipTimes.get(player.getId());
                long now = System.currentTimeMillis();
                long blockMillis = clauseBlockDays * 24L * 60L * 60L * 1000L;
                long remaining = -1;
                if (acquired != null && acquired > 0) {
                    remaining = (acquired + blockMillis) - now;
                }
                if (remaining > 0) {
                    holder.btnClause.setText(formatRemaining(remaining));
                    holder.btnClause.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            holder.btnClause.getResources().getColor(R.color.gray_light)));
                } else {
                    holder.btnClause.setText("Clausular");
                    holder.btnClause.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            holder.btnClause.getResources().getColor(R.color.accent_gold)));
                }
                holder.btnClause.setOnClickListener(v -> {
                    com.google.android.material.snackbar.Snackbar.make(v,
                            "Este botón muestra el estado de la cláusula. Otros managers podrán clausularte cuando caduque el bloqueo.",
                            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                });
            }
        }

        // si hay listener, manejamos el click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlayerClick(player);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private String formatRemaining(long millis) {
        long totalSec = Math.max(millis, 0) / 1000L;
        long days = totalSec / (24 * 3600);
        long rem = totalSec % (24 * 3600);
        long hours = rem / 3600;
        long minutes = (rem % 3600) / 60;
        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }

    // ViewHolder que guarda las referencias a las vistas
    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvPos;
        TextView tvPoints;
        com.google.android.material.button.MaterialButton btnClause;

        VH(@NonNull View itemView) {
            super(itemView);
            // estos IDs tienen que coincidir con item_player_simple.xml
            tvName = itemView.findViewById(R.id.tv_player_name);
            tvPos = itemView.findViewById(R.id.tv_player_pos);
            tvPoints = itemView.findViewById(R.id.tv_player_points);
            btnClause = itemView.findViewById(R.id.btn_clause);
        }
    }
}