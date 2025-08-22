package com.example.housemanager.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.housemanager.R;
import com.example.housemanager.repository.models.ManagerScore;

import java.util.ArrayList;
import java.util.List;

public class ManagerScoreAdapter extends RecyclerView.Adapter<ManagerScoreAdapter.VH> {

    private final List<ManagerScore> data = new ArrayList<>();

    public void submit(List<ManagerScore> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manager_score, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ManagerScore row = data.get(position);
        holder.tvUser.setText("Manager " + row.getUserId());
        holder.tvPoints.setText(row.getTotalPoints() + " pts");
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvUser, tvPoints;
        VH(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tv_user);
            tvPoints = itemView.findViewById(R.id.tv_points);
        }
    }
}
