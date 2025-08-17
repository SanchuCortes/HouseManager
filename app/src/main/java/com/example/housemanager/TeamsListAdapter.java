package com.example.housemanager.ui.team;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.housemanager.R;
import com.example.housemanager.market.Team;

public class TeamListAdapter extends ListAdapter<Team, TeamListAdapter.VH> {

    public interface OnTeamClickListener {
        void onTeamClick(Team team);
    }

    private final OnTeamClickListener listener;

    public TeamListAdapter(OnTeamClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Team> DIFF = new DiffUtil.ItemCallback<Team>() {
        @Override public boolean areItemsTheSame(@NonNull Team a, @NonNull Team b) {
            return a.getTeamId() == b.getTeamId();
        }
        @Override public boolean areContentsTheSame(@NonNull Team a, @NonNull Team b) {
            return a.getName().equals(b.getName())
                    && ((a.getCrestUrl() == null && b.getCrestUrl() == null) ||
                    (a.getCrestUrl() != null && a.getCrestUrl().equals(b.getCrestUrl())));
        }
    };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Team t = getItem(position);
        h.name.setText(t.getName());
        Glide.with(h.logo.getContext())
                .load(t.getCrestUrl())
                .placeholder(android.R.drawable.ic_menu_report_image) // <- placeholder del sistema
                .error(android.R.drawable.ic_menu_report_image)
                .into(h.logo);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTeamClick(t);
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView logo;
        final TextView name;
        VH(@NonNull View itemView) {
            super(itemView);
            logo = itemView.findViewById(R.id.imgLogo);
            name = itemView.findViewById(R.id.txtName);
        }
    }
}
