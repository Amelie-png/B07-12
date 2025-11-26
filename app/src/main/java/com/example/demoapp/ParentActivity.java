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
import com.google.firebase.firestore.QuerySnapshot;

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
        setContentView(R.layout.add_child); // 布局确认

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

    private void showAddChildDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_child, null);
        EditText etName = dialogView.findViewById(R.id.et_child_name);
        EditText etDob = dialogView.findViewById(R.id.et_child_dob);
        EditText etNotes = dialogView.findViewById(R.id.et_child_notes);
        EditText etPassword = dialogView.findViewById(R.id.et_child_password);

        new AlertDialog.Builder(this)
                .setTitle("Add Child")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String dob = etDob.getText().toString().trim();
                    String notes = etNotes.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dob) || TextUtils.isEmpty(password)) {
                        Toast.makeText(this, "Name, DOB, and Password cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 检查是否有同名 child
                    db.collection("children")
                            .whereEqualTo("parentId", UserUtils.getUid())
                            .whereEqualTo("name", name)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    Toast.makeText(this, "Child name already exists", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                // 添加 child
                                String parentId = UserUtils.getUid();
                                Child child = new Child(name, dob, parentId, notes);
                                child.setPasswordHash(hashPassword(password));
                                addChildToFirestore(child);
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addChildToFirestore(Child child) {
        Map<String, Object> childMap = childToMap(child);
        db.collection("children")
                .add(childMap)
                .addOnSuccessListener(docRef -> {
                    child.setFirestoreId(docRef.getId());
                    childrenList.add(child);
                    adapter.notifyItemInserted(childrenList.size() - 1);
                    Toast.makeText(this, "Child saved to Firestore!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving child: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

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

    private void showEditChildDialog(int position) {
        Child child = childrenList.get(position);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_child, null);
        EditText etName = dialogView.findViewById(R.id.et_child_name);
        EditText etDob = dialogView.findViewById(R.id.et_child_dob);
        EditText etNotes = dialogView.findViewById(R.id.et_child_notes);

        etName.setText(child.getName());
        etDob.setText(child.getDob());
        etNotes.setText(child.getNotes());

        new AlertDialog.Builder(this)
                .setTitle("Edit Child")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newDob = etDob.getText().toString().trim();
                    String newNotes = etNotes.getText().toString().trim();

                    if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newDob)) {
                        Toast.makeText(this, "Name and DOB cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 检查 name 是否重复（排除自己）
                    db.collection("children")
                            .whereEqualTo("parentId", child.getParentId())
                            .whereEqualTo("name", newName)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    Toast.makeText(this, "Child name already exists", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                // 更新对象
                                child.setName(newName);
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

    private void showGenerateShareCodeDialog(int position) {
        Child child = childrenList.get(position);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_generate_share_code, null);
        TextView tvShareCode = dialogView.findViewById(R.id.tv_share_code);
        TextView tvCodeExpiry = dialogView.findViewById(R.id.tv_code_expiry);
        Button btnCopyCode = dialogView.findViewById(R.id.btn_copy_code);
        Button btnGenerateNew = dialogView.findViewById(R.id.btn_generate_new);

        // 打开对话框时立即生成一次性分享码
        String code = child.generateOneTimeShareCode();
        tvShareCode.setText(code);
        tvCodeExpiry.setText("Valid for 7 days");

        // 保存到 Firestore
        db.collection("children")
                .document(child.getFirestoreId())
                .set(childToMap(child))
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Share code generated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving share code: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // 复制按钮
        btnCopyCode.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Share Code", code);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        // 生成新码按钮
        btnGenerateNew.setOnClickListener(v -> {
            String newCode = child.generateOneTimeShareCode();
            tvShareCode.setText(newCode);
            tvCodeExpiry.setText("Valid for 7 days");
            db.collection("children")
                    .document(child.getFirestoreId())
                    .set(childToMap(child));
            Toast.makeText(this, "New share code generated!", Toast.LENGTH_SHORT).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("One-Time Share Code")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }



    private void showManageProviderDialog(int position) {
        Child child = childrenList.get(position);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_manage_provider, null);

        TextView tvProviders = dialogView.findViewById(R.id.tv_providers);
        Button btnRevoke = dialogView.findViewById(R.id.btn_revoke);
        Button btnUpdate = dialogView.findViewById(R.id.btn_update_permissions);

        tvProviders.setText("Providers: " + child.getProviderIds().toString());

        btnRevoke.setOnClickListener(v -> {
            for (String pid : child.getProviderIds()) {
                child.revokeProvider(pid);
            }
            db.collection("children").document(child.getFirestoreId()).set(childToMap(child));
            Toast.makeText(this, "All providers revoked", Toast.LENGTH_SHORT).show();
        });

        btnUpdate.setOnClickListener(v -> {
            for (String pid : child.getProviderIds()) {
                Map<String, Boolean> perms = new HashMap<>();
                perms.put("symptoms", true);
                perms.put("medicines", true);
                perms.put("pef", true);
                perms.put("triage", true);
                child.updateProviderPermissions(pid, perms);
            }
            db.collection("children").document(child.getFirestoreId()).set(childToMap(child));
            Toast.makeText(this, "Permissions updated", Toast.LENGTH_SHORT).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Manage Providers")
                .setView(dialogView)
                .setNegativeButton("Close", null)
                .show();
    }

    private Map<String, Object> childToMap(Child child) {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", child.getUid());
        map.put("name", child.getName());
        map.put("dob", child.getDob());
        map.put("parentId", child.getParentId());
        map.put("notes", child.getNotes());
        map.put("sharing", child.getSharing());
        map.put("providerIds", new ArrayList<>(child.getProviderIds()));
        map.put("hasSeenOnboardingChild", child.isHasSeenOnboardingChild());

        Map<String, Map<String, Object>> codesMap = new HashMap<>();
        for (Map.Entry<String, Child.ShareCode> entry : child.getShareCodes().entrySet()) {
            Map<String, Object> codeInfo = new HashMap<>();
            codeInfo.put("code", entry.getValue().getCode());
            codeInfo.put("timestamp", entry.getValue().getTimestamp());
            codeInfo.put("revoked", entry.getValue().isRevoked());
            codeInfo.put("permissions", entry.getValue().getPermissions());
            codesMap.put(entry.getKey(), codeInfo);
        }
        map.put("shareCodes", codesMap);
        return map;
    }
}
