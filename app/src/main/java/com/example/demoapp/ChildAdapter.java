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

    // ------------------- 接口 -------------------
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

    // ------------------- 构造 -------------------
    public ChildAdapter(List<Child> children, OnItemActionListener listener) {
        this.children = children;
        this.listener = listener;
    }

    // ------------------- 创建 ViewHolder -------------------
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.child_item, parent, false); // 确保布局名正确
        return new VH(view);
    }

    // ------------------- 绑定数据 -------------------
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Child child = children.get(position);

        // 显示内容，不为 null
        holder.tvName.setText(child.getUsername() != null ? child.getUsername() : "");
        holder.tvFirstName.setText(child.getFirstName() != null ? child.getFirstName() : "");
        holder.tvLastName.setText(child.getLastName() != null ? child.getLastName() : "");
        holder.tvDob.setText(child.getDob() != null ? child.getDob() : "");
        holder.tvNotes.setText(child.getNotes() != null ? child.getNotes() : "");

        // 按钮点击事件
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(holder.getAdapterPosition()));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(holder.getAdapterPosition()));
        holder.btnShare.setOnClickListener(v -> listener.onGenerateShareCode(holder.getAdapterPosition()));
        holder.btnManageProvider.setOnClickListener(v -> listener.onManageProvider(holder.getAdapterPosition()));

        // 整个 item 点击
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

    // ------------------- ViewHolder -------------------
    public static class VH extends RecyclerView.ViewHolder {
        public TextView tvName, tvFirstName, tvLastName, tvDob, tvNotes;
        public Button btnEdit, btnDelete, btnShare, btnManageProvider;

        public VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_child_name);
            tvFirstName = itemView.findViewById(R.id.tvFirstName);
            tvLastName = itemView.findViewById(R.id.tvLastName);
            tvDob = itemView.findViewById(R.id.tv_child_dob);
            tvNotes = itemView.findViewById(R.id.tv_child_notes);

            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnShare = itemView.findViewById(R.id.btn_share);
            btnManageProvider = itemView.findViewById(R.id.btn_manage_provider);
        }
    }
}
