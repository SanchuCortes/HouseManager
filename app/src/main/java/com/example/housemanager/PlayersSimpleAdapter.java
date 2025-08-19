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

    public interface OnPlayerClick { void onPlayerClick(PlayerAPI player); }

    private final List<PlayerAPI> data = new ArrayList<>();
    private OnPlayerClick listener;
    private int captainId = -1;

    public PlayersSimpleAdapter() { this.listener = null; }
    public PlayersSimpleAdapter(OnPlayerClick listener) { this.listener = listener; }

    public void submit(List<PlayerAPI> players) {
        data.clear();
        if (players != null) data.addAll(players);
        notifyDataSetChanged();
    }

    public void setCaptainId(int id) { captainId = id; notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player_simple, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PlayerAPI p = data.get(position);
        String name = p.getName() != null ? p.getName() : "-";
        if (p.getId() == captainId) name = "★ " + name; // marcador opcional
        h.tvName.setText(name);
        h.tvPos.setText(p.getPosition() != null ? p.getPosition() : "–");
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onPlayerClick(p); });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPos;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPos  = itemView.findViewById(R.id.tv_pos);
        }
    }
}
