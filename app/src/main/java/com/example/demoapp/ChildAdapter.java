package com.example.demoapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.demoapp.models.Child;

import java.util.List;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.VH> {

    private final List<Child> children;
    private final OnItemActionListener listener;
    private OnChildClickListener childClickListener;

    public interface OnItemActionListener {
        void onEdit(int position);
        void onDelete(int position);
        void onGenerateShareCode(int position);
        void onManageProvider(int position);
    }

    public interface OnChildClickListener {
        void onChildClick(Child child);
    }

    public void setOnChildClick(OnChildClickListener listener) {
        this.childClickListener = listener;
    }

    public ChildAdapter(List<Child> children, OnItemActionListener listener) {
        this.children = children;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.child_item, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Child child = children.get(position);
        holder.tvName.setText(child.getUsername());
        holder.tvDob.setText(child.getDob());
        holder.tvNotes.setText(child.getNotes());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(holder.getAdapterPosition()));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(holder.getAdapterPosition()));
        holder.btnShare.setOnClickListener(v -> listener.onGenerateShareCode(holder.getAdapterPosition()));
        holder.btnManageProvider.setOnClickListener(v -> listener.onManageProvider(holder.getAdapterPosition()));

        holder.itemView.setOnClickListener(v -> {
            if (childClickListener != null) {
                childClickListener.onChildClick(child);
            }
        });
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        public TextView tvName, tvDob, tvNotes;
        public Button btnEdit, btnDelete, btnShare, btnManageProvider;

        public VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_child_name);
            tvDob = itemView.findViewById(R.id.tv_child_dob);
            tvNotes = itemView.findViewById(R.id.tv_child_notes);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnShare = itemView.findViewById(R.id.btn_share);
            btnManageProvider = itemView.findViewById(R.id.btn_manage_provider);
        }
    }
}
