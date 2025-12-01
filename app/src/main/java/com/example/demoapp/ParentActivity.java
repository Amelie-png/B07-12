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
import java.util.Map;import java.util.HashSet;import java.util.List;import android.util.Log;import com.google.firebase.firestore.QueryDocumentSnapshot;import android.widget.ImageView;

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
        checkParentOnboarding();

        RecyclerView recyclerView = findViewById(R.id.rv_children_list);
        FloatingActionButton btnAddChild = findViewById(R.id.btn_add_child);

        ImageView ivUserIcon = findViewById(R.id.iv_user_icon);
        ivUserIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ParentActivity.this, ParentProfileActivity.class);
            startActivity(intent);
        });

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
    private void checkParentOnboarding() {
        String parentUid = UserUtils.getUid();

        db.collection("users")
                .document(parentUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Boolean hasSeen = doc.getBoolean("hasSeenOnboardingParent");

                    // Treat null as not seen
                    if (hasSeen == null || !hasSeen) {
                        showParentOnboardingPopup(parentUid);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("ParentActivity", "Failed to load onboarding flag", e));
    }
    private void showParentOnboardingPopup(String parentUid) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_parent_onboarding, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialog.show();

        // Get the button from the layout
        Button btnClose = dialogView.findViewById(R.id.closeParentPopup);

        btnClose.setOnClickListener(v -> {
            // Update Firestore flag
            db.collection("users")
                    .document(parentUid)
                    .update("hasSeenOnboardingParent", true)
                    .addOnFailureListener(e ->
                            Log.e("ParentOnboarding", "Failed updating onboarding flag", e));

            dialog.dismiss();
        });
    }

    private void checkParentOnboarding() {
        String parentUid = UserUtils.getUid();

        db.collection("users")
                .document(parentUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Boolean hasSeen = doc.getBoolean("hasSeenOnboardingParent");

                    // Treat null as not seen
                    if (hasSeen == null || !hasSeen) {
                        showParentOnboardingPopup(parentUid);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("ParentActivity", "Failed to load onboarding flag", e));
    }
    private void showParentOnboardingPopup(String parentUid) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_parent_onboarding, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialog.show();

        // Get the button from the layout
        Button btnClose = dialogView.findViewById(R.id.closeParentPopup);

        btnClose.setOnClickListener(v -> {
            // Update Firestore flag
            db.collection("users")
                    .document(parentUid)
                    .update("hasSeenOnboardingParent", true)
                    .addOnFailureListener(e ->
                            Log.e("ParentOnboarding", "Failed updating onboarding flag", e));

            dialog.dismiss();
        });
    }

    private void loadChildren() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String parentId = UserUtils.getUid();

        db.collection("children")
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        childrenList.clear(); // 先清空旧数据
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            try {
                                Child child = doc.toObject(Child.class);
                                if (child != null) {
                                    // 核心修改：把 Firestore 文档 ID 赋给 Child 对象
                                    child.setFirestoreId(doc.getId());

                                    if (child.getProviderIds() == null) {
                                        child.setProviderIds(new ArrayList<>());
                                    }

                                    // 确保 sharing map 不为 null
                                    if (child.getSharing() == null) {
                                        child.setSharing(new HashMap<>());
                                    }

                                    // 确保 shareCodes 不为 null
                                    if (child.getShareCodes() == null) {
                                        child.setShareCodes(new HashMap<>());
                                    }

                                    // 确保 providerBindings 不为 null
                                    if (child.getProviderBindings() == null) {
                                        child.setProviderBindings(new HashMap<>());
                                    }

                                    Log.d(TAG, "Loaded child: " + child.getUsername() +
                                            ", UID: " + child.getUid() +
                                            ", FirestoreId: " + child.getFirestoreId() +
                                            ", ProviderIds: " + child.getProviderIds() +
                                            ", ShareCodes: " + child.getShareCodes().keySet() +
                                            ", ProviderBindings: " + child.getProviderBindings());

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

    private void cleanupExpiredShareCodes() {
        for (Child child : childrenList) {
            // 调用 Child 内部方法清理过期且未绑定的分享码
            child.removeExpiredUnboundShareCodes();

            // 更新 Firestore
            if (child.getFirestoreId() != null) {
                db.collection("children")
                        .document(child.getFirestoreId())
                        .set(childToMap(child))
                        .addOnSuccessListener(aVoid -> Log.d(TAG,
                                "Expired share codes cleaned for " + child.getUsername()))
                        .addOnFailureListener(e -> Log.e(TAG,
                                "Failed cleaning share codes for " + child.getUsername(), e));
            }
        }
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

        // 权限 Switch
        com.google.android.material.switchmaterial.SwitchMaterial switchRescueLogs = dialogView.findViewById(R.id.switch_rescue_logs);
        com.google.android.material.switchmaterial.SwitchMaterial switchControllerAdherence = dialogView.findViewById(R.id.switch_controller_adherence);
        com.google.android.material.switchmaterial.SwitchMaterial switchSymptoms = dialogView.findViewById(R.id.switch_symptoms);
        com.google.android.material.switchmaterial.SwitchMaterial switchTriggers = dialogView.findViewById(R.id.switch_triggers);
        com.google.android.material.switchmaterial.SwitchMaterial switchPEF = dialogView.findViewById(R.id.switch_pef);
        com.google.android.material.switchmaterial.SwitchMaterial switchTriageIncidents = dialogView.findViewById(R.id.switch_triage_incidents);
        com.google.android.material.switchmaterial.SwitchMaterial switchSummaryCharts = dialogView.findViewById(R.id.switch_summary_charts);

        String code = child.generateOneTimeShareCode();
        tvShareCode.setText(code);
        tvCodeExpiry.setText("Valid for 7 days");

        Child.ShareCode shareCode = child.getShareCodes().get(code);
        if (shareCode != null) {
            Map<String, Boolean> perms = shareCode.getPermissions();
            switchRescueLogs.setChecked(perms.getOrDefault("rescueLogs", false));
            switchControllerAdherence.setChecked(perms.getOrDefault("controllerAdherence", false));
            switchSymptoms.setChecked(perms.getOrDefault("symptoms", false));
            switchTriggers.setChecked(perms.getOrDefault("triggers", false));
            switchPEF.setChecked(perms.getOrDefault("pef", false));
            switchTriageIncidents.setChecked(perms.getOrDefault("triageIncidents", false));
            switchSummaryCharts.setChecked(perms.getOrDefault("summaryCharts", false));
        } else {
            switchRescueLogs.setChecked(false);
            switchControllerAdherence.setChecked(false);
            switchSymptoms.setChecked(false);
            switchTriggers.setChecked(false);
            switchPEF.setChecked(false);
            switchTriageIncidents.setChecked(false);
            switchSummaryCharts.setChecked(false);
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

            // 初始化 Switch 状态
            Child.ShareCode newShareCode = child.getShareCodes().get(newCode);
            if (newShareCode != null) {
                Map<String, Boolean> perms = newShareCode.getPermissions();
                switchRescueLogs.setChecked(perms.getOrDefault("rescueLogs", false));
                switchControllerAdherence.setChecked(perms.getOrDefault("controllerAdherence", false));
                switchSymptoms.setChecked(perms.getOrDefault("symptoms", false));
                switchTriggers.setChecked(perms.getOrDefault("triggers", false));
                switchPEF.setChecked(perms.getOrDefault("pef", false));
                switchTriageIncidents.setChecked(perms.getOrDefault("triageIncidents", false));
                switchSummaryCharts.setChecked(perms.getOrDefault("summaryCharts", false));
            } else {
                switchRescueLogs.setChecked(false);
                switchControllerAdherence.setChecked(false);
                switchSymptoms.setChecked(false);
                switchTriggers.setChecked(false);
                switchPEF.setChecked(false);
                switchTriageIncidents.setChecked(false);
                switchSummaryCharts.setChecked(false);
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
                    perms.put("rescueLogs", switchRescueLogs.isChecked());
                    perms.put("controllerAdherence", switchControllerAdherence.isChecked());
                    perms.put("symptoms", switchSymptoms.isChecked());
                    perms.put("triggers", switchTriggers.isChecked());
                    perms.put("pef", switchPEF.isChecked());
                    perms.put("triageIncidents", switchTriageIncidents.isChecked());
                    perms.put("summaryCharts", switchSummaryCharts.isChecked());

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
        EditText etSearch = dialogView.findViewById(R.id.et_search);
        cleanupExpiredShareCodes();


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

        adapter.bindSearchBox(etSearch);

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

