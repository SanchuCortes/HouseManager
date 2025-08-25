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

// Adapter sencillo para mostrar nombre, equipo, precio y botón "Fichar"
public class TransferMarketAdapter extends RecyclerView.Adapter<TransferMarketAdapter.VH> {

    public interface OnPlayerClickListener {
        void onPlayerClick(Player player);
        void onBuyPlayerClick(Player player);
    }

    private final OnPlayerClickListener listener;
    private List<Player> players = new ArrayList<>();

    public TransferMarketAdapter(OnPlayerClickListener listener) {
        this.listener = listener;
    }

    public void updatePlayers(List<Player> newPlayers) {
        players = newPlayers != null ? newPlayers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transfer_player, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Player p = players.get(pos);
        // Mostrar nombre solo y los puntos a la derecha
        h.tvName.setText(p.getName());
        h.tvTeam.setText(p.getTeamName());
        h.tvPrice.setText(String.format("€ %.1f M", p.getCurrentPrice()));
        h.tvPos.setText(p.getPosition());
        h.tvPoints.setText(String.format("%d pts", p.getTotalPoints()));

        h.itemView.setOnClickListener(v -> listener.onPlayerClick(p));
        h.btnBuy.setOnClickListener(v -> listener.onBuyPlayerClick(p));
    }

    @Override public int getItemCount() { return players.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvTeam, tvPrice, tvPos, tvPoints;
        Button btnBuy;
        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvTeam = v.findViewById(R.id.tv_team);
            tvPrice = v.findViewById(R.id.tv_price);
            tvPos = v.findViewById(R.id.tv_pos);
            tvPoints = v.findViewById(R.id.tv_points);
            btnBuy = v.findViewById(R.id.btn_buy);
        }
    }
}
