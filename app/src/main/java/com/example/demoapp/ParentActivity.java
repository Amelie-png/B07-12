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
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import android.util.Log;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ParentActivity extends AppCompatActivity {

    private static final String TAG = "ParentActivity";
    private final ArrayList<Child> childrenList = new ArrayList<>();
    private ChildAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_child);

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

        // ---------------------------------------------------------
        // ✔ FIXED: Correct role and correct child/parent data passing
        // ---------------------------------------------------------
        adapter.setOnChildClick(child -> {
            Intent intent = new Intent(ParentActivity.this, MainNavActivity.class);

            // Parent is the viewer
            intent.putExtra("role", "parent");

            // Child being viewed
            intent.putExtra("uid", child.getUid());

            // Logged-in parent
            intent.putExtra("parentUid", UserUtils.getUid());

            startActivity(intent);
        });
        // ---------------------------------------------------------

        btnAddChild.setOnClickListener(v -> showAddChildDialog());

        loadChildren();
    }

    private void loadChildren() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String parentId = UserUtils.getUid();

        db.collection("children")
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        childrenList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            try {
                                Child child = doc.toObject(Child.class);
                                if (child != null) {
                                    child.setFirestoreId(doc.getId());

                                    if (child.getProviderIds() == null) {
                                        child.setProviderIds(new ArrayList<>());
                                    }
                                    if (child.getSharing() == null) {
                                        child.setSharing(new HashMap<>());
                                    }

                                    childrenList.add(child);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing child doc: " + doc.getId(), e);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "Error getting children", task.getException());
                    }
                });
    }

    // username uniqueness check
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
                                    if (currentChildUid == null || !uid.equals(currentChildUid)) {
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

    private void showAddChildDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_child, null);

        EditText etUsername = dialogView.findViewById(R.id.et_child_name);
        EditText etFirstName = dialogView.findViewById(R.id.et_child_first_name);
        EditText etLastName = dialogView.findViewById(R.id.et_child_last_name);
        EditText etDob = dialogView.findViewById(R.id.et_child_dob);
        EditText etNotes = dialogView.findViewById(R.id.et_child_notes);
        EditText etPassword = dialogView.findViewById(R.id.et_child_password);

        new AlertDialog.Builder(this)
                .setTitle("Add Child")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String username = etUsername.getText().toString().trim();
                    String firstName = etFirstName.getText().toString().trim();
                    String lastName = etLastName.getText().toString().trim();
                    String dob = etDob.getText().toString().trim();
                    String notes = etNotes.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();

                    if (TextUtils.isEmpty(username) || TextUtils.isEmpty(firstName)
                            || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(dob)
                            || TextUtils.isEmpty(password)) {
                        Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    checkUsernameUniqueForChild(username, null, () -> {
                        String parentId = UserUtils.getUid();
                        Child child = new Child(username, firstName, lastName, dob, parentId, notes);
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
                    String docId = docRef.getId();
                    child.setFirestoreId(docId);
                    child.setUid(docId);

                    docRef.update("uid", docId);

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
        if (position < 0 || position >= childrenList.size()) return;

        Child child = childrenList.get(position);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_child, null);

        EditText etUsername = dialogView.findViewById(R.id.et_child_name);
        EditText etFirstName = dialogView.findViewById(R.id.et_child_first_name);
        EditText etLastName = dialogView.findViewById(R.id.et_child_last_name);
        EditText etDob = dialogView.findViewById(R.id.et_child_dob);
        EditText etNotes = dialogView.findViewById(R.id.et_child_notes);

        etUsername.setText(child.getUsername());
        etFirstName.setText(child.getFirstName());
        etLastName.setText(child.getLastName());
        etDob.setText(child.getDob());
        etNotes.setText(child.getNotes());

        new AlertDialog.Builder(this)
                .setTitle("Edit Child")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newUsername = etUsername.getText().toString().trim();
                    String newFirstName = etFirstName.getText().toString().trim();
                    String newLastName = etLastName.getText().toString().trim();
                    String newDob = etDob.getText().toString().trim();
                    String newNotes = etNotes.getText().toString().trim();

                    if (TextUtils.isEmpty(newUsername) || TextUtils.isEmpty(newFirstName)
                            || TextUtils.isEmpty(newLastName) || TextUtils.isEmpty(newDob)) {
                        Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    checkUsernameUniqueForChild(newUsername, child.getUid(), () -> {
                        child.setUsername(newUsername);
                        child.setFirstName(newFirstName);
                        child.setLastName(newLastName);
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
        if (position < 0 || position >= childrenList.size()) return;

        Child child = childrenList.get(position);

        if (child.getFirestoreId() == null || child.getFirestoreId().isEmpty()) {
            Toast.makeText(ParentActivity.this, "Invalid child ID", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(ParentActivity.this)
                .setTitle("Delete Child")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("children")
                            .document(child.getFirestoreId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                childrenList.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(ParentActivity.this, "Child deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(ParentActivity.this, "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showGenerateShareCodeDialog(int position) {
        Child child = childrenList.get(position);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_generate_share_code_with_switches, null);

        TextView tvShareCode = dialogView.findViewById(R.id.tv_share_code);
        TextView tvCodeExpiry = dialogView.findViewById(R.id.tv_code_expiry);
        Button btnCopyCode = dialogView.findViewById(R.id.btn_copy_code);
        Button btnGenerateNew = dialogView.findViewById(R.id.btn_generate_new);

        SwitchMaterial switchSymptoms = dialogView.findViewById(R.id.switch_symptoms);
        SwitchMaterial switchMedicines = dialogView.findViewById(R.id.switch_medicines);
        SwitchMaterial switchPEF = dialogView.findViewById(R.id.switch_pef);
        SwitchMaterial switchTriage = dialogView.findViewById(R.id.switch_triage);

        String code = child.generateOneTimeShareCode();
        tvShareCode.setText(code);
        tvCodeExpiry.setText("Valid for 7 days");

        Child.ShareCode shareCode = child.getShareCodes().get(code);
        if (shareCode != null) {
            Map<String, Boolean> perms = shareCode.getPermissions();
            switchSymptoms.setChecked(perms.getOrDefault("symptoms", false));
            switchMedicines.setChecked(perms.getOrDefault("medicines", false));
            switchPEF.setChecked(perms.getOrDefault("pef", false));
            switchTriage.setChecked(perms.getOrDefault("triage", false));
        }

        db.collection("children")
                .document(child.getFirestoreId())
                .set(childToMap(child));

        btnCopyCode.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Share Code", code);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show();
        });

        btnGenerateNew.setOnClickListener(v -> {
            String newCode = child.generateOneTimeShareCode();
            tvShareCode.setText(newCode);
            tvCodeExpiry.setText("Valid for 7 days");

            Child.ShareCode sc = child.getShareCodes().get(newCode);
            if (sc != null) {
                Map<String, Boolean> perms = sc.getPermissions();
                switchSymptoms.setChecked(perms.getOrDefault("symptoms", false));
                switchMedicines.setChecked(perms.getOrDefault("medicines", false));
                switchPEF.setChecked(perms.getOrDefault("pef", false));
                switchTriage.setChecked(perms.getOrDefault("triage", false));
            }

            db.collection("children")
                    .document(child.getFirestoreId())
                    .set(childToMap(child));
            Toast.makeText(this, "New code generated!", Toast.LENGTH_SHORT).show();
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

    private void showManageProviderDialog(int position) {
        Child child = childrenList.get(position);
        Child finalChild = child;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_manage_providers, null);
        RecyclerView rvShareCodes = dialogView.findViewById(R.id.rv_share_codes);

        List<Child.ShareCode> codes = new ArrayList<>(finalChild.getShareCodes().values());
        ShareCodeAdapter adapter = new ShareCodeAdapter(codes, updatedCode -> {

            if (finalChild.getShareCodes() == null) finalChild.setShareCodes(new HashMap<>());
            finalChild.getShareCodes().put(updatedCode.getCode(), updatedCode);

            db.collection("children")
                    .document(finalChild.getFirestoreId())
                    .set(childToMap(finalChild))
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(ParentActivity.this, "Share code updated!", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(ParentActivity.this, "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        rvShareCodes.setLayoutManager(new LinearLayoutManager(this));
        rvShareCodes.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Manage Providers")
                .setView(dialogView)
                .setNegativeButton("Close", null)
                .show();
    }

    private Map<String, Object> childToMap(Child child) {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", child.getUid());
        map.put("username", child.getUsername());
        map.put("dob", child.getDob());
        map.put("parentId", child.getParentId());
        map.put("notes", child.getNotes());
        map.put("sharing", child.getSharing());
        map.put("providerIds", new ArrayList<>(child.getProviderIds()));
        map.put("providerBindings", child.getProviderBindings());
        map.put("hasSeenOnboardingChild", child.isHasSeenOnboardingChild());
        map.put("passwordHash", child.getPasswordHash());
        map.put("firstName", child.getFirstName());
        map.put("lastName", child.getLastName());
        map.put("pb", child.getPb());

        Map<String, Map<String, Object>> codesMap = new HashMap<>();
        for (Map.Entry<String, Child.ShareCode> entry : child.getShareCodes().entrySet()) {
            Map<String, Object> codeInfo = new HashMap<>();
            codeInfo.put("code", entry.getValue().getCode());
            codeInfo.put("timestamp", entry.getValue().getTimestamp());
            codeInfo.put("revoked", entry.getValue().isRevoked());
            codeInfo.put("permissions", entry.getValue().getPermissions());
            codeInfo.put("providerId", entry.getValue().getProviderId());
            codesMap.put(entry.getKey(), codeInfo);
        }
        map.put("shareCodes", codesMap);

        return map;
    }
}
