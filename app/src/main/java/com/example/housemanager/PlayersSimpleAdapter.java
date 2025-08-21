package com.example.housemanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.housemanager.api.models.PlayerAPI;

import java.util.ArrayList;
import java.util.List;

public class PlayersSimpleAdapter extends RecyclerView.Adapter<PlayersSimpleAdapter.VH> {

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

    // ViewHolder que guarda las referencias a las vistas
    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvPos;

        VH(@NonNull View itemView) {
            super(itemView);
            // estos IDs tienen que coincidir con item_player_simple.xml
            tvName = itemView.findViewById(R.id.tv_player_name);
            tvPos = itemView.findViewById(R.id.tv_player_pos);
        }
    }
}