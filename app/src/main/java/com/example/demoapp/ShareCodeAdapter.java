package com.example.demoapp;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.models.Child;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShareCodeAdapter extends RecyclerView.Adapter<ShareCodeAdapter.ShareCodeViewHolder> {

    private final List<Child.ShareCode> allShareCodes; // 保存完整列表
    private List<Child.ShareCode> shareCodes;          // 当前显示列表
    private final OnShareCodeChangeListener listener;
    private String currentQuery = ""; // 当前搜索关键字

    public interface OnShareCodeChangeListener {
        void onShareCodeUpdated(Child.ShareCode updatedCode);
    }

    public ShareCodeAdapter(List<Child.ShareCode> shareCodes, OnShareCodeChangeListener listener) {
        this.allShareCodes = new ArrayList<>(shareCodes);
        this.shareCodes = new ArrayList<>(shareCodes);
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
        final Child.ShareCode sc = shareCodes.get(position);

        // 点击分享码文本展开/收起权限面板
        holder.tvCode.setOnClickListener(v -> {
            sc.setExpanded(!sc.isExpanded());
            notifyItemChanged(position);
        });

        // 高亮搜索匹配部分
        String code = sc.getCode();
        if (!currentQuery.isEmpty()) {
            int startIndex = code.toLowerCase().indexOf(currentQuery.toLowerCase());
            if (startIndex >= 0) {
                int endIndex = startIndex + currentQuery.length();
                SpannableString spannable = new SpannableString(code);
                int highlightColor = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_light);
                spannable.setSpan(new ForegroundColorSpan(highlightColor),
                        startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.tvCode.setText(spannable);
            } else {
                holder.tvCode.setText(code);
            }
        } else {
            holder.tvCode.setText(code);
        }

        // 设置过期时间
        long expiryMs = sc.getTimestamp() + 7L * 24 * 3600 * 1000;
        long remainingMs = expiryMs - System.currentTimeMillis();
        if (remainingMs > 0) {
            long hours = remainingMs / (1000 * 3600);
            long minutes = (remainingMs / (1000 * 60)) % 60;
            holder.tvExpiry.setText(String.format(Locale.getDefault(), "Valid: %dh %dm", hours, minutes));
        } else {
            holder.tvExpiry.setText("Expired");
        }

        // 控制权限面板显示
        holder.permissionsContainer.setVisibility(sc.isExpanded() ? View.VISIBLE : View.GONE);
        holder.llHeader.setOnClickListener(v -> {
            sc.setExpanded(!sc.isExpanded());
            notifyItemChanged(position);
        });

        final Map<String, Boolean> perms = sc.getPermissions();

        // 7 个权限 + 撤销开关保持原逻辑
        setSwitch(holder.switchSymptoms, perms, "symptoms", sc);
        setSwitch(holder.switchMedicines, perms, "medicines", sc);
        setSwitch(holder.switchPEF, perms, "pef", sc);
        setSwitch(holder.switchTriage, perms, "triageIncidents", sc);
        setSwitch(holder.switchRescueLogs, perms, "rescueLogs", sc);
        setSwitch(holder.switchControllerAdherence, perms, "controllerAdherence", sc);
        setSwitch(holder.switchSummaryCharts, perms, "summaryCharts", sc);

        // 撤销开关
        holder.switchRevoked.setOnCheckedChangeListener(null);
        holder.switchRevoked.setChecked(sc.isRevoked());
        holder.switchRevoked.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sc.setRevoked(isChecked);
            listener.onShareCodeUpdated(sc);
        });
    }

    private void setSwitch(SwitchMaterial sw, Map<String, Boolean> perms, String key, Child.ShareCode sc) {
        sw.setOnCheckedChangeListener(null);
        sw.setChecked(perms.getOrDefault(key, false));
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            perms.put(key, isChecked);
            listener.onShareCodeUpdated(sc);
        });
    }

    @Override
    public int getItemCount() {
        return shareCodes.size();
    }

    // 🔹 搜索方法：匹配任意部分（忽略大小写）
    public void filter(String query) {
        currentQuery = query != null ? query.trim() : "";
        if (currentQuery.isEmpty()) {
            shareCodes = new ArrayList<>(allShareCodes);
        } else {
            List<Child.ShareCode> filtered = new ArrayList<>();
            for (Child.ShareCode sc : allShareCodes) {
                if (sc.getCode().toLowerCase().contains(currentQuery.toLowerCase())) {
                    filtered.add(sc);
                }
            }
            shareCodes = filtered;
        }
        notifyDataSetChanged();
    }

    // 🔹 绑定搜索框
    public void bindSearchBox(EditText etSearch) {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    public static class ShareCodeViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llHeader, permissionsContainer;
        TextView tvCode, tvExpiry;
        SwitchMaterial switchSymptoms, switchMedicines, switchPEF, switchTriage, switchRevoked,
                switchRescueLogs, switchControllerAdherence, switchSummaryCharts;

        public ShareCodeViewHolder(@NonNull View itemView) {
            super(itemView);
            llHeader = itemView.findViewById(R.id.ll_header);
            permissionsContainer = itemView.findViewById(R.id.permissions_container);
            tvCode = itemView.findViewById(R.id.tv_code);
            tvExpiry = itemView.findViewById(R.id.tv_expiry);
            switchSymptoms = itemView.findViewById(R.id.cb_symptoms);
            switchMedicines = itemView.findViewById(R.id.cb_medicines);
            switchPEF = itemView.findViewById(R.id.cb_pef);
            switchTriage = itemView.findViewById(R.id.cb_triage);
            switchRevoked = itemView.findViewById(R.id.cb_revoked);
            switchRescueLogs = itemView.findViewById(R.id.cb_rescue_logs);
            switchControllerAdherence = itemView.findViewById(R.id.cb_controller_adherence);
            switchSummaryCharts = itemView.findViewById(R.id.cb_summary_charts);
        }
    }
}
