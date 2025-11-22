package com.example.demoapp;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder> {

    public interface OnItemActionListener {
        void onEdit(int position);
        void onDelete(int position);
    }

    private List<Child> children;
    private OnItemActionListener listener;

    public ChildAdapter(List<Child> children, OnItemActionListener listener) {
        this.children = children;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.child_item, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Child child = children.get(position);
        holder.nameText.setText(child.getName());
        holder.dobText.setText("DOB: " + child.getDob());
        holder.avatarImage.setImageResource(child.getAvatarRes());

        holder.itemView.setOnClickListener(v -> listener.onEdit(position));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onDelete(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage;
        TextView nameText;
        TextView dobText;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.iv_child_avatar);
            nameText = itemView.findViewById(R.id.tv_child_name);
            dobText = itemView.findViewById(R.id.tv_child_dob);
        }
    }
}
