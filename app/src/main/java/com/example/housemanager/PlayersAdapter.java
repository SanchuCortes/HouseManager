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

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.VH> {

    public interface OnPlayerClick {
        void onPlayerClick(PlayerAPI player);
    }

    private final List<PlayerAPI> players = new ArrayList<>();
    private final OnPlayerClick listener;
    private int captainId = -1;

    public PlayersAdapter(OnPlayerClick listener) {
        this.listener = listener;
    }

    public void submit(List<PlayerAPI> data) {
        players.clear();
        if (data != null) players.addAll(data);
        notifyDataSetChanged();
    }

    public void setCaptainId(int captainId) {
        this.captainId = captainId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player_simple, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PlayerAPI p = players.get(position);

        String name = p.getName() != null ? p.getName() : "-";
        if (p.getId() == captainId) name = "★ " + name; // opcional para marcar capitán
        h.tvName.setText(name);

        String pos = p.getPosition() != null ? p.getPosition() : "–";
        h.tvPos.setText(pos);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPlayerClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPos;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPos  = itemView.findViewById(R.id.tv_pos);
        }
    }
}
