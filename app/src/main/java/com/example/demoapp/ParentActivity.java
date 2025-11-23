package com.example.demoapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.demoapp.models.Child;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class ParentActivity extends AppCompatActivity {


    private final ArrayList<Child> childrenList = new ArrayList<>();
    private ChildAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 使用 Parent Home 布局
        setContentView(R.layout.add_child);

        RecyclerView recyclerView = findViewById(R.id.rv_children_list);
        adapter = new ChildAdapter(childrenList, new ChildAdapter.OnItemActionListener() {
            @Override
            public void onEdit(int position) {
                showEditChildDialog(position);
            }

            @Override
            public void onDelete(int position) {
                showDeleteChildDialog(position);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FloatingActionButton btnAddChild = findViewById(R.id.btn_add_child);
        btnAddChild.setOnClickListener(v -> showAddChildDialog());
    }

    // 弹出添加孩子对话框
    private void showAddChildDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_child, null);
        EditText etName = dialogView.findViewById(R.id.et_child_name);
        EditText etDob = dialogView.findViewById(R.id.et_child_dob);
        EditText etNotes = dialogView.findViewById(R.id.et_child_notes);
        ImageView avatarImage = dialogView.findViewById(R.id.iv_child_avatar_dialog);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Add Child")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String dob = etDob.getText().toString().trim();
                    String notes = etNotes.getText().toString().trim();

                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(dob)) {
                        // 获取当前用户 ID
                        String parentId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                        Child child = new Child(name, dob, parentId, notes);

                        // Firestore 实例
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        // 保存到 Firestore
                        db.collection("children")
                                .add(child)
                                .addOnSuccessListener(documentReference -> Toast.makeText(this, "Child saved to Firestore!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Error saving child: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                        // 更新本地列表和 RecyclerView
                        childrenList.add(child);
                        adapter.notifyItemInserted(childrenList.size() - 1);

                    } else {
                        Toast.makeText(this, "Name and DOB cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.show();
    }

    private void showEditChildDialog(int position) {
        Child child = childrenList.get(position);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_child, null);
        EditText etName = dialogView.findViewById(R.id.et_child_name);
        EditText etDob = dialogView.findViewById(R.id.et_child_dob);
        EditText etNotes = dialogView.findViewById(R.id.et_child_notes);
        ImageView avatarImage = dialogView.findViewById(R.id.iv_child_avatar_dialog);

        etName.setText(child.getName());
        etDob.setText(child.getDob());
        etNotes.setText(child.getNotes());

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Edit Child")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newDob = etDob.getText().toString().trim();
                    String newNotes = etNotes.getText().toString().trim();

                    if (!TextUtils.isEmpty(newName) && !TextUtils.isEmpty(newDob)) {
                        child.setName(newName);
                        child.setDob(newDob);
                        child.setNotes(newNotes);

                        // 更新本地列表
                        adapter.notifyItemChanged(position);

                        // Firestore 实例
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        // 查询该 child 文档并更新
                        db.collection("children")
                                .whereEqualTo("parentId", child.getParentId())
                                .whereEqualTo("name", child.getName())
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                        db.collection("children").document(doc.getId())
                                                .set(child)
                                                .addOnSuccessListener(aVoid ->
                                                        Toast.makeText(this, "Child updated in Firestore!", Toast.LENGTH_SHORT).show()
                                                )
                                                .addOnFailureListener(e ->
                                                        Toast.makeText(this, "Error updating child: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                                );
                                    }
                                });

                        Toast.makeText(this, "Child updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Name and DOB cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.show();
    }

    private void showDeleteChildDialog(int position) {
        Child child = childrenList.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Delete Child")
                .setMessage("Are you sure you want to delete this child?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Firestore 实例
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    // 查询并删除 Firestore 中的文档
                    db.collection("children")
                            .whereEqualTo("parentId", child.getParentId())
                            .whereEqualTo("name", child.getName())
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                for (DocumentSnapshot doc : querySnapshot) {
                                    db.collection("children").document(doc.getId())
                                            .delete()
                                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Child deleted from Firestore!", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e -> Toast.makeText(this, "Error deleting child: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                }
                            });

                    // 更新本地列表和 RecyclerView
                    childrenList.remove(position);
                    adapter.notifyItemRemoved(position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


}
