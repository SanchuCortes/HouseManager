package com.example.housemanager.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.housemanager.R;
import com.example.housemanager.api.models.TeamAPI;

import java.util.ArrayList;
import java.util.List;

public class TeamsListAdapter extends RecyclerView.Adapter<TeamsListAdapter.VH> {

    public interface OnTeamClickListener {
        void onTeamClick(TeamAPI team);
    }

    private final List<TeamAPI> data = new ArrayList<>();
    private final OnTeamClickListener listener;

    public TeamsListAdapter(OnTeamClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<TeamAPI> teams) {
        data.clear();
        if (teams != null) data.addAll(teams);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TeamAPI t = data.get(position);
        h.name.setText(t.getName() != null ? t.getName() : "");

        if (t.getCrest() != null && !t.getCrest().isEmpty()) {
            Glide.with(h.logo.getContext()).load(t.getCrest()).into(h.logo);
        } else {
            h.logo.setImageResource(R.drawable.ic_player_placeholder);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTeamClick(t);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView logo;
        final TextView name;
        VH(@NonNull View itemView) {
            super(itemView);
            logo = itemView.findViewById(R.id.iv_crest);
            name = itemView.findViewById(R.id.tv_name);
        }
    }
}
