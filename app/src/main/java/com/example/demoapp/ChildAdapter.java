package com.example.demoapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.demoapp.models.Child;
import java.util.List;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ViewHolder> {

    public interface OnItemActionListener {
        void onEdit(int position);
        void onDelete(int position);
    }

    private List<Child> childList;
    private OnItemActionListener listener;

    public ChildAdapter(List<Child> childList, OnItemActionListener listener) {
        this.childList = childList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Child child = childList.get(position);
        holder.nameText.setText(child.getName());
        holder.dobText.setText(child.getDob());

        holder.editButton.setOnClickListener(v -> listener.onEdit(position));
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(position));
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, dobText;
        ImageView editButton, deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.tv_child_name);
            dobText = itemView.findViewById(R.id.tv_child_dob);
            editButton = itemView.findViewById(R.id.iv_edit_child);
            deleteButton = itemView.findViewById(R.id.iv_delete_child);
        }
    }
}
