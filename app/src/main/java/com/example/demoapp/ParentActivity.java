package com.example.demoapp;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class ParentActivity extends AppCompatActivity {

    private ArrayList<Child> childrenList = new ArrayList<>();
    private ChildAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                        Child child = new Child(name, dob, android.R.drawable.ic_menu_camera, notes);
                        childrenList.add(child);
                        adapter.notifyItemInserted(childrenList.size() - 1);
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
                        adapter.notifyItemChanged(position);
                    }
                })
                .setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteChildDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Delete Child")
                .setMessage("Are you sure you want to delete this child?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    childrenList.remove(position);
                    adapter.notifyItemRemoved(position);
                })
                .setNegativeButton("Cancel", null);
        builder.show();
    }
}
