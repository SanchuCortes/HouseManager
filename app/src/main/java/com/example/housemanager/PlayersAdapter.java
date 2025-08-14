package com.example.housemanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Muestra nombre, posición y precio. Sin iconos para no depender de drawables.
public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.PlayerVH> {

    private final List<MyTeamActivity.PlayerLite> players;

    public PlayersAdapter(List<MyTeamActivity.PlayerLite> players) {
        this.players = players;
    }

    @NonNull
    @Override
    public PlayerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player, parent, false);
        return new PlayerVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerVH h, int position) {
        MyTeamActivity.PlayerLite p = players.get(position);
        h.tvName.setText(p.getName());
        h.tvPosition.setText(p.getPosition());
        h.tvPrice.setText(p.getPrice() + "M €");
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    static class PlayerVH extends RecyclerView.ViewHolder {
        TextView tvName, tvPosition, tvPrice;
        PlayerVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_player_name);
            tvPosition = itemView.findViewById(R.id.tv_player_position);
            tvPrice = itemView.findViewById(R.id.tv_player_price);
        }
    }
}
