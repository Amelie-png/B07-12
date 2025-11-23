package com.example.demoapp;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private List<Badge> badgeList;

    public BadgeAdapter(List<Badge> badgeList) {
        this.badgeList = badgeList;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        holder.image.setImageResource(badgeList.get(position).iconResId);
    }

    @Override
    public int getItemCount() {
        return badgeList.size();
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        BadgeViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageBadge);
        }
    }
}

