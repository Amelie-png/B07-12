package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.models.Child;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;import java.util.HashSet;import java.util.List;


public class ParentActivity extends AppCompatActivity {

    private final ArrayList<Child> childrenList = new ArrayList<>();
    private ChildAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_child); // 布局必须有 rv_children_list 和 btn_add_child

        db = FirebaseFirestore.getInstance();

        RecyclerView recyclerView = findViewById(R.id.rv_children_list);
        FloatingActionButton btnAddChild = findViewById(R.id.btn_add_child);

        adapter = new ChildAdapter(childrenList, new ChildAdapter.OnItemActionListener() {
            @Override
            public void onEdit(int position) { showEditChildDialog(position); }

            @Override
            public void onDelete(int position) { showDeleteChildDialog(position); }

            @Override
            public void onGenerateShareCode(int position) { showGenerateShareCodeDialog(position); }

            @Override
            public void onManageProvider(int position) { showManageProviderDialog(position); }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 点击跳转
        adapter.setOnChildClick(child -> {
            Intent intent = new Intent(ParentActivity.this, MainNavActivity.class);
            intent.putExtra("uid", child.getUid());
            intent.putExtra("role", "child");
            startActivity(intent);
        });

        btnAddChild.setOnClickListener(v -> showAddChildDialog());

        loadChildren(); // 加载孩子列表
    }

    private void loadChildren() {
        // 获取当前父账户 UID
        String parentId = UserUtils.getUid();
        if (parentId == null) {
            Toast.makeText(this, "Error: parent UID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // 查询 Firestore
        db.collection("children")
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Failed to load children: " + task.getException(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    childrenList.clear(); // 清空已有列表

                    for (var doc : task.getResult()) {
                        try {
                            // 打印文档调试
                            System.out.println("Child doc: " + doc.getData());

                            // 安全映射到 Child 对象
                            Child child = doc.toObject(Child.class);
                            if (child == null) {
                                System.out.println("Warning: doc.toObject returned null for " + doc.getId());
                                continue;
                            }

                            // 设置 Firestore ID 和 UID
                            child.setFirestoreId(doc.getId());
                            if (child.getUid() == null || child.getUid().isEmpty()) {
                                child.setUid(doc.getId());
                            }

                            // 避免 null 导致 Adapter 崩溃
                            if (child.getUsername() == null) child.setUsername("");
                            if (child.getDob() == null) child.setDob("");
                            if (child.getNotes() == null) child.setNotes("");
                            if (child.getSharing() == null) child.setSharing(new HashMap<>());
                            if (child.getProviderIds() == null) child.setProviderIds(new HashSet<>());
                            if (child.getShareCodes() == null) child.setShareCodes(new HashMap<>());

                            childrenList.add(child);

                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error parsing child doc: " + doc.getId());
                        }
                    }

                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Loaded " + childrenList.size() + " children", Toast.LENGTH_SHORT).show();
                });
    }


    // ---------------------------
// Username uniqueness check (for add & edit)
// ---------------------------
    private void checkUsernameUniqueForChild(String username, String currentChildUid, Runnable onUsernameAvailable) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(userTask -> {
                    if (!userTask.isSuccessful()) {
                        Toast.makeText(this, "Error checking username: " + userTask.getException(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!userTask.getResult().isEmpty()) {
                        Toast.makeText(this, "Username already exists in users.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("children")
                            .whereEqualTo("username", username)
                            .get()
                            .addOnCompleteListener(childTask -> {
                                if (!childTask.isSuccessful()) {
                                    Toast.makeText(this, "Error checking username.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                boolean exists = false;
                                for (var doc : childTask.getResult()) {
                                    String uid = doc.getString("uid");
                                    if (currentChildUid == null || !uid.equals(currentChildUid)) { // 排除当前 child
                                        exists = true;
                                        break;
                                    }
                                }

                                if (exists) {
                                    Toast.makeText(this, "Username already exists in children.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                onUsernameAvailable.run();
                            });
                });
    }


    // ---------------------------
    // Add Child Dialog
    // ---------------------------
    private void showAddChildDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_child, null);
        EditText etUsername = dialogView.findViewById(R.id.et_child_name);
        EditText etDob = dialogView.findViewById(R.id.et_child_dob);
        EditText etNotes = dialogView.findViewById(R.id.et_child_notes);
        EditText etPassword = dialogView.findViewById(R.id.et_child_password);

        new AlertDialog.Builder(this)
                .setTitle("Add Child")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String username = etUsername.getText().toString().trim();
                    String dob = etDob.getText().toString().trim();
                    String notes = etNotes.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();

                    if (TextUtils.isEmpty(username) || TextUtils.isEmpty(dob) || TextUtils.isEmpty(password)) {
                        Toast.makeText(this, "Username, DOB, and Password cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 新增时传 null，表示不排除任何 child
                    checkUsernameUniqueForChild(username, null, () -> {
                        String parentId = UserUtils.getUid();
                        Child child = new Child(username, dob, parentId, notes);
                        child.setPasswordHash(hashPassword(password));

                        addChildToFirestore(child);
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }



    // ---------------------------
    // Firestore Add Child (Fixed UID)
    // ---------------------------
    private void addChildToFirestore(Child child) {
        Map<String, Object> childMap = childToMap(child);

        db.collection("children")
                .add(childMap)
                .addOnSuccessListener(docRef -> {

                    String docId = docRef.getId();

                    // 🔥 Use Firestore ID as UID
                    child.setFirestoreId(docId);
                    child.setUid(docId);

                    // 🔥 Update Firestore with correct UID
                    docRef.update("uid", docId);

                    childrenList.add(child);
                    adapter.notifyItemInserted(childrenList.size() - 1);

                    Toast.makeText(this, "Child saved to Firestore!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving child: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    // ---------------------------
    // SHA-256
    // ---------------------------
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return password;
        }
    }


    // ---------------------------
    // Edit Child
    // ---------------------------
    // ---------------------------
// Edit Child Dialog
// ---------------------------
    private void showEditChildDialog(int position) {
        Child child = childrenList.get(position);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_child, null);

        EditText etUsername = dialogView.findViewById(R.id.et_child_name);
        EditText etDob = dialogView.findViewById(R.id.et_child_dob);
        EditText etNotes = dialogView.findViewById(R.id.et_child_notes);

        etUsername.setText(child.getUsername());
        etDob.setText(child.getDob());
        etNotes.setText(child.getNotes());

        new AlertDialog.Builder(this)
                .setTitle("Edit Child")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newUsername = etUsername.getText().toString().trim();
                    String newDob = etDob.getText().toString().trim();
                    String newNotes = etNotes.getText().toString().trim();

                    if (TextUtils.isEmpty(newUsername) || TextUtils.isEmpty(newDob)) {
                        Toast.makeText(this, "Username and DOB cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 使用修改后的 checkUsernameUniqueForChild，排除当前 child
                    checkUsernameUniqueForChild(newUsername, child.getUid(), () -> {
                        child.setUsername(newUsername);
                        child.setDob(newDob);
                        child.setNotes(newNotes);

                        db.collection("children")
                                .document(child.getFirestoreId())
                                .set(childToMap(child))
                                .addOnSuccessListener(aVoid -> {
                                    childrenList.set(position, child);
                                    adapter.notifyItemChanged(position);
                                    Toast.makeText(this, "Child updated", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error updating child: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    // ---------------------------
    // Delete Child
    // ---------------------------
    private void showDeleteChildDialog(int position) {
        Child child = childrenList.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Delete Child")
                .setMessage("Are you sure you want to delete this child?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("children")
                            .document(child.getFirestoreId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                childrenList.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(this, "Child deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    // ---------------------------
    // Share Code Dialog
    // ---------------------------
    // ---------------------------
// Share Code Dialog (generate without binding)
// ---------------------------
    // ---------------------------
// Share Code + Permission Dialog
// ---------------------------
    private void showGenerateShareCodeDialog(int position) {
        Child child = childrenList.get(position);

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_generate_share_code_with_switches, null);

        TextView tvShareCode = dialogView.findViewById(R.id.tv_share_code);
        TextView tvCodeExpiry = dialogView.findViewById(R.id.tv_code_expiry);
        Button btnCopyCode = dialogView.findViewById(R.id.btn_copy_code);
        Button btnGenerateNew = dialogView.findViewById(R.id.btn_generate_new);

        // 权限 Switch
        com.google.android.material.switchmaterial.SwitchMaterial switchSymptoms = dialogView.findViewById(R.id.switch_symptoms);
        com.google.android.material.switchmaterial.SwitchMaterial switchMedicines = dialogView.findViewById(R.id.switch_medicines);
        com.google.android.material.switchmaterial.SwitchMaterial switchPEF = dialogView.findViewById(R.id.switch_pef);
        com.google.android.material.switchmaterial.SwitchMaterial switchTriage = dialogView.findViewById(R.id.switch_triage);

        // 🔹 生成一次性分享码（不绑定 provider）
        String code = child.generateOneTimeShareCode();
        tvShareCode.setText(code);
        tvCodeExpiry.setText("Valid for 7 days");

        // ✅ 初始化 Switch 状态：从 ShareCode 中读取权限，如果不存在则默认 false
        Child.ShareCode shareCode = child.getShareCodes().get(code);
        if (shareCode != null) {
            Map<String, Boolean> perms = shareCode.getPermissions();
            switchSymptoms.setChecked(perms.getOrDefault("symptoms", false));
            switchMedicines.setChecked(perms.getOrDefault("medicines", false));
            switchPEF.setChecked(perms.getOrDefault("pef", false));
            switchTriage.setChecked(perms.getOrDefault("triage", false));
        } else {
            switchSymptoms.setChecked(false);
            switchMedicines.setChecked(false);
            switchPEF.setChecked(false);
            switchTriage.setChecked(false);
        }

        // 保存到 Firestore（生成新 code）
        db.collection("children")
                .document(child.getFirestoreId())
                .set(childToMap(child))
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Share code generated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error saving share code: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // 复制到剪贴板
        btnCopyCode.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Share Code", code);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        // 生成新分享码
        btnGenerateNew.setOnClickListener(v -> {
            String newCode = child.generateOneTimeShareCode();
            tvShareCode.setText(newCode);
            tvCodeExpiry.setText("Valid for 7 days");

            // 初始化 Switch 状态
            Child.ShareCode newShareCode = child.getShareCodes().get(newCode);
            if (newShareCode != null) {
                Map<String, Boolean> perms = newShareCode.getPermissions();
                switchSymptoms.setChecked(perms.getOrDefault("symptoms", false));
                switchMedicines.setChecked(perms.getOrDefault("medicines", false));
                switchPEF.setChecked(perms.getOrDefault("pef", false));
                switchTriage.setChecked(perms.getOrDefault("triage", false));
            } else {
                switchSymptoms.setChecked(false);
                switchMedicines.setChecked(false);
                switchPEF.setChecked(false);
                switchTriage.setChecked(false);
            }

            db.collection("children")
                    .document(child.getFirestoreId())
                    .set(childToMap(child));
            Toast.makeText(this, "New share code generated!", Toast.LENGTH_SHORT).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Share Code & Permissions")
                .setView(dialogView)
                .setPositiveButton("Save Permissions", (dialog, which) -> {
                    Map<String, Boolean> perms = new HashMap<>();
                    perms.put("symptoms", switchSymptoms.isChecked());
                    perms.put("medicines", switchMedicines.isChecked());
                    perms.put("pef", switchPEF.isChecked());
                    perms.put("triage", switchTriage.isChecked());

                    // 更新最新生成的 share code 权限
                    Child.ShareCode latest = child.getShareCodes().get(code);
                    if (latest != null) latest.setPermissions(perms);

                    db.collection("children")
                            .document(child.getFirestoreId())
                            .set(childToMap(child));
                    Toast.makeText(this, "Permissions saved!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
    }



    // ---------------------------
    // Manage Providers Dialog
    // ---------------------------

    // ---------------------------
// Manage Providers Dialog
// ---------------------------
    private void showManageProviderDialog(int position) {
        Child child = childrenList.get(position);
        Child finalChild = child; // 🔹 使用 final 引用，避免 lambda 捕获报错

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_manage_providers, null);
        RecyclerView rvShareCodes = dialogView.findViewById(R.id.rv_share_codes);

        List<Child.ShareCode> codes = new ArrayList<>(finalChild.getShareCodes().values());
        ShareCodeAdapter adapter = new ShareCodeAdapter(codes, updatedCode -> {
            // 🔹 使用 finalChild 替代 child
            if (finalChild.getShareCodes() == null) finalChild.setShareCodes(new HashMap<>());
            finalChild.getShareCodes().put(updatedCode.getCode(), updatedCode);

            if (finalChild.getFirestoreId() != null) {
                db.collection("children")
                        .document(finalChild.getFirestoreId())
                        .set(childToMap(finalChild))
                        .addOnSuccessListener(aVoid -> {
                            // 可选：刷新 UI
                            Toast.makeText(ParentActivity.this, "Share code updated!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ParentActivity.this, "Error updating share code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        rvShareCodes.setLayoutManager(new LinearLayoutManager(this));
        rvShareCodes.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Manage Share Codes & Permissions")
                .setView(dialogView)
                .setNegativeButton("Close", null)
                .show();
    }








    // ---------------------------
    // Convert Child → Firestore Map
    // ---------------------------
    private Map<String, Object> childToMap(Child child) {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", child.getUid());
        map.put("username", child.getUsername());
        map.put("dob", child.getDob());
        map.put("parentId", child.getParentId());
        map.put("notes", child.getNotes());
        map.put("sharing", child.getSharing());
        map.put("providerIds", new ArrayList<>(child.getProviderIds()));
        map.put("providerBindings", child.getProviderBindings()); // 🔹 新增
        map.put("hasSeenOnboardingChild", child.isHasSeenOnboardingChild());
        map.put("passwordHash", child.getPasswordHash());

        Map<String, Map<String, Object>> codesMap = new HashMap<>();
        for (Map.Entry<String, Child.ShareCode> entry : child.getShareCodes().entrySet()) {
            Map<String, Object> codeInfo = new HashMap<>();
            codeInfo.put("code", entry.getValue().getCode());
            codeInfo.put("timestamp", entry.getValue().getTimestamp());
            codeInfo.put("revoked", entry.getValue().isRevoked());
            codeInfo.put("permissions", entry.getValue().getPermissions());
            codeInfo.put("providerId", entry.getValue().getProviderId()); // 🔹 保存绑定情况
            codesMap.put(entry.getKey(), codeInfo);
        }
        map.put("shareCodes", codesMap);

        return map;
    }




}
