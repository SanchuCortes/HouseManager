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

// Adapter sencillo: nombre y posición
public class PlayersSimpleAdapter extends RecyclerView.Adapter<PlayersSimpleAdapter.VH> {

    private List<PlayerAPI> data = new ArrayList<>();

    public void submit(List<PlayerAPI> list) {
        data = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player_simple, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        PlayerAPI p = data.get(pos);
        h.tvName.setText(p.name);
        h.tvPos.setText(p.position != null ? p.position : "—");
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPos;
        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_player_name);
            tvPos  = v.findViewById(R.id.tv_player_pos);
        }
    }
}
