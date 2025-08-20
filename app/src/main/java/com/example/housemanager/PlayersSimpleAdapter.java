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

    // Interface para manejar clicks en jugadores
    public interface OnPlayerClick {
        void onPlayerClick(PlayerAPI player);
    }

    private final List<PlayerAPI> jugadores = new ArrayList<>();
    private OnPlayerClick listener;
    private int capitanId = -1; // ID del capitán actual

    // Constructor sin listener
    public PlayersSimpleAdapter() {
        this.listener = null;
    }

    // Constructor con listener para clicks
    public PlayersSimpleAdapter(OnPlayerClick listener) {
        this.listener = listener;
    }

    // Actualizar la lista de jugadores
    public void submit(List<PlayerAPI> nuevosJugadores) {
        jugadores.clear();
        if (nuevosJugadores != null) {
            jugadores.addAll(nuevosJugadores);
        }
        notifyDataSetChanged();
    }

    // Cambiar quién es el capitán
    public void setCaptainId(int id) {
        capitanId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player_simple, parent, false);
        return new VH(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        PlayerAPI jugador = jugadores.get(position);

        // Nombre del jugador
        String nombre = jugador.getName() != null ? jugador.getName() : "Sin nombre";

        // Si es el capitán, le pongo una estrella
        if (jugador.getId() == capitanId) {
            nombre = "⭐ " + nombre;
        }

        holder.tvNombre.setText(nombre);

        // Posición del jugador
        String posicion = jugador.getPosition() != null ? jugador.getPosition() : "—";
        holder.tvPosicion.setText(posicion);

        // Click en el jugador
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlayerClick(jugador);
            }
        });
    }

    @Override
    public int getItemCount() {
        return jugadores.size();
    }

    // ViewHolder para cada item
    static class VH extends RecyclerView.ViewHolder {
        TextView tvNombre, tvPosicion;

        VH(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_name);
            tvPosicion = itemView.findViewById(R.id.tv_pos);
        }
    }
}