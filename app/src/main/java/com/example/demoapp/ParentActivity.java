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
    private void showGenerateShareCodeDialog(int position) {
        Child child = childrenList.get(position);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_generate_share_code, null);
        TextView tvShareCode = dialogView.findViewById(R.id.tv_share_code);
        TextView tvCodeExpiry = dialogView.findViewById(R.id.tv_code_expiry);
        Button btnCopyCode = dialogView.findViewById(R.id.btn_copy_code);
        Button btnGenerateNew = dialogView.findViewById(R.id.btn_generate_new);

        String code = child.generateOneTimeShareCode();
        tvShareCode.setText(code);
        tvCodeExpiry.setText("Valid for 7 days");

        db.collection("children")
                .document(child.getFirestoreId())
                .set(childToMap(child))
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Share code generated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving share code: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        btnCopyCode.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Share Code", code);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        });

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


    // ---------------------------
    // Manage Providers Dialog
    // ---------------------------
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
        map.put("hasSeenOnboardingChild", child.isHasSeenOnboardingChild());
        map.put("passwordHash", child.getPasswordHash());
        map.put("pb", child.getPb());

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
