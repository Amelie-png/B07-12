package com.example.demoapp;

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
import java.util.Map;

public class ParentActivity extends AppCompatActivity {

    private final ArrayList<Child> childrenList = new ArrayList<>();
    private ChildAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_child);

        db = FirebaseFirestore.getInstance();

        RecyclerView recyclerView = findViewById(R.id.rv_children_list);
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

        FloatingActionButton btnAddChild = findViewById(R.id.btn_add_child);
        btnAddChild.setOnClickListener(v -> showAddChildDialog());
    }

    // ---------------------------
    // Username uniqueness check
    // ---------------------------
    private void checkUsernameUniqueForChild(String username, Runnable onUsernameAvailable) {
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

                                if (!childTask.getResult().isEmpty()) {
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

                    checkUsernameUniqueForChild(username, () -> {
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

                    checkUsernameUniqueForChild(newUsername, () -> {
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

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_generate_share_code_with_switches, null);

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

        // 默认权限可以先全部勾选
        switchSymptoms.setChecked(true);
        switchMedicines.setChecked(true);
        switchPEF.setChecked(true);
        switchTriage.setChecked(true);

        // 保存到 Firestore
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

            // 默认权限勾选
            switchSymptoms.setChecked(true);
            switchMedicines.setChecked(true);
            switchPEF.setChecked(true);
            switchTriage.setChecked(true);

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

        // 使用 dialog_share_code_with_permissions 布局
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_generate_share_code_with_switches, null);

        TextView tvShareTitle = dialogView.findViewById(R.id.tv_share_title);

        // 权限 Switch
        com.google.android.material.switchmaterial.SwitchMaterial switchSymptoms = dialogView.findViewById(R.id.switch_symptoms);
        com.google.android.material.switchmaterial.SwitchMaterial switchMedicines = dialogView.findViewById(R.id.switch_medicines);
        com.google.android.material.switchmaterial.SwitchMaterial switchPEF = dialogView.findViewById(R.id.switch_pef);
        com.google.android.material.switchmaterial.SwitchMaterial switchTriage = dialogView.findViewById(R.id.switch_triage);

        Button btnSaveSharing = dialogView.findViewById(R.id.btn_save_sharing);

        tvShareTitle.setText("Manage Providers Permissions");

        // 默认全选
        switchSymptoms.setChecked(true);
        switchMedicines.setChecked(true);
        switchPEF.setChecked(true);
        switchTriage.setChecked(true);

        // 如果 child 有 provider，展示第一个 provider 当前权限
        if (!child.getProviderIds().isEmpty()) {
            String pid = child.getProviderIds().iterator().next();
            for (Child.ShareCode sc : child.getShareCodes().values()) {
                if (pid.equals(sc.getProviderId()) && !sc.isRevoked()) {
                    Map<String, Boolean> perms = sc.getPermissions();
                    switchSymptoms.setChecked(perms.getOrDefault("symptoms", true));
                    switchMedicines.setChecked(perms.getOrDefault("medicines", true));
                    switchPEF.setChecked(perms.getOrDefault("pef", true));
                    switchTriage.setChecked(perms.getOrDefault("triage", true));
                    break;
                }
            }
        }

        btnSaveSharing.setOnClickListener(v -> {
            Map<String, Boolean> newPerms = new HashMap<>();
            newPerms.put("symptoms", switchSymptoms.isChecked());
            newPerms.put("medicines", switchMedicines.isChecked());
            newPerms.put("pef", switchPEF.isChecked());
            newPerms.put("triage", switchTriage.isChecked());

            // 更新所有 provider 的权限
            for (String pid : child.getProviderIds()) {
                child.updateProviderPermissions(pid, newPerms);
            }

            // 保存到 Firestore
            db.collection("children")
                    .document(child.getFirestoreId())
                    .set(childToMap(child));

            Toast.makeText(this, "Permissions updated for all providers!", Toast.LENGTH_SHORT).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Manage Providers")
                .setView(dialogView)
                .setNegativeButton("Close", null)
                .show();
    }




    // ---------------------------
// Provider 使用分享码绑定
// ---------------------------
    // ---------------------------
// Provider 使用分享码绑定
// ---------------------------
    private void showBindShareCodeDialog(Child child, String providerId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.pop_up_add_patient, null); // 你提供的布局文件名
        EditText etShareCode = dialogView.findViewById(R.id.enter_auth_code);
        Button btnSubmit = dialogView.findViewById(R.id.submit_code_button);
        Button btnClose = dialogView.findViewById(R.id.add_patient_close_button);
        TextView tvPrompt = dialogView.findViewById(R.id.auth_code);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Bind Share Code")
                .setView(dialogView)
                .create();

        btnSubmit.setOnClickListener(v -> {
            String codeInput = etShareCode.getText().toString().trim();
            if (TextUtils.isEmpty(codeInput)) {
                Toast.makeText(this, "Share code cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // 遍历 shareCodes 找到未绑定的分享码
            boolean success = false;
            for (Child.ShareCode sc : child.getShareCodes().values()) {
                if (!sc.isRevoked() &&
                        sc.getProviderId() == null && // 还未绑定 provider
                        sc.getCode().equals(codeInput) &&
                        System.currentTimeMillis() - sc.getTimestamp() <= 7L * 24 * 60 * 60 * 1000) {

                    // 绑定 provider
                    sc.setProviderId(providerId);

                    // 默认权限，可以根据需要修改
                    Map<String, Boolean> defaultPerms = new HashMap<>();
                    defaultPerms.put("symptoms", true);
                    defaultPerms.put("medicines", true);
                    defaultPerms.put("pef", true);
                    defaultPerms.put("triage", true);
                    sc.setPermissions(defaultPerms);

                    child.getProviderIds().add(providerId);

                    // 保存到 Firestore
                    db.collection("children")
                            .document(child.getFirestoreId())
                            .set(childToMap(child));

                    success = true;
                    Toast.makeText(this, "Provider bound successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    break;
                }
            }

            if (!success) {
                Toast.makeText(this, "Invalid or expired share code", Toast.LENGTH_SHORT).show();
            }
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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
