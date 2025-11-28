package com.example.demoapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.models.Child;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShareCodeAdapter extends RecyclerView.Adapter<ShareCodeAdapter.ShareCodeViewHolder> {

    private final List<Child.ShareCode> shareCodes;
    private final OnShareCodeChangeListener listener;

    public interface OnShareCodeChangeListener {
        void onShareCodeUpdated(Child.ShareCode updatedCode);
    }

    public ShareCodeAdapter(List<Child.ShareCode> shareCodes, OnShareCodeChangeListener listener) {
        this.shareCodes = shareCodes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShareCodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_share_code, parent, false);
        return new ShareCodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShareCodeViewHolder holder, int position) {
        final Child.ShareCode sc = shareCodes.get(position); // 🔹 final
        holder.tvCode.setText(sc.getCode());

        // 剩余有效时间
        long expiryMs = sc.getTimestamp() + 7L * 24 * 3600 * 1000;
        long remainingMs = expiryMs - System.currentTimeMillis();
        if (remainingMs > 0) {
            long hours = remainingMs / (1000 * 3600);
            long minutes = (remainingMs / (1000 * 60)) % 60;
            holder.tvExpiry.setText(String.format(Locale.getDefault(), "Valid: %dh %dm", hours, minutes));
        } else {
            holder.tvExpiry.setText("Expired");
        }

        final Map<String, Boolean> perms = sc.getPermissions(); // 🔹 final

        // 🔹 设置 SwitchMaterial 并安全监听
        holder.switchSymptoms.setOnCheckedChangeListener(null);
        holder.switchSymptoms.setChecked(perms.getOrDefault("symptoms", false));
        holder.switchSymptoms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            perms.put("symptoms", isChecked);
            listener.onShareCodeUpdated(sc);
        });

        holder.switchMedicines.setOnCheckedChangeListener(null);
        holder.switchMedicines.setChecked(perms.getOrDefault("medicines", false));
        holder.switchMedicines.setOnCheckedChangeListener((buttonView, isChecked) -> {
            perms.put("medicines", isChecked);
            listener.onShareCodeUpdated(sc);
        });

        holder.switchPEF.setOnCheckedChangeListener(null);
        holder.switchPEF.setChecked(perms.getOrDefault("pef", false));
        holder.switchPEF.setOnCheckedChangeListener((buttonView, isChecked) -> {
            perms.put("pef", isChecked);
            listener.onShareCodeUpdated(sc);
        });

        holder.switchTriage.setOnCheckedChangeListener(null);
        holder.switchTriage.setChecked(perms.getOrDefault("triage", false));
        holder.switchTriage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            perms.put("triage", isChecked);
            listener.onShareCodeUpdated(sc);
        });

        holder.switchRevoked.setOnCheckedChangeListener(null);
        holder.switchRevoked.setChecked(sc.isRevoked());
        holder.switchRevoked.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sc.setRevoked(isChecked);
            listener.onShareCodeUpdated(sc);
        });
    }

    @Override
    public int getItemCount() {
        return shareCodes.size();
    }

    public static class ShareCodeViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvExpiry;
        SwitchMaterial switchSymptoms, switchMedicines, switchPEF, switchTriage, switchRevoked;

        public ShareCodeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tv_code);
            tvExpiry = itemView.findViewById(R.id.tv_expiry);
            switchSymptoms = itemView.findViewById(R.id.cb_symptoms);
            switchMedicines = itemView.findViewById(R.id.cb_medicines);
            switchPEF = itemView.findViewById(R.id.cb_pef);
            switchTriage = itemView.findViewById(R.id.cb_triage);
            switchRevoked = itemView.findViewById(R.id.cb_revoked);
        }
    }
}
