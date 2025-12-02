package com.example.demoapp;

import com.example.demoapp.models.*;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.demoapp.card_view.CardItem;
import com.google.api.OAuthRequirementsOrBuilder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddPatientPopup extends DialogFragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String providerId;
    private OnDataChangedListener listener;

    public AddPatientPopup(String providerId){
        this.providerId = providerId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate popup layout
        View view = inflater.inflate(R.layout.pop_up_add_patient, container, false);

        // Enter code TextEdit
        EditText authCodeEditText = view.findViewById(R.id.enter_auth_code);

        // Close button inside pop-up
        Button closeButton = view.findViewById(R.id.add_patient_close_button);
        closeButton.setOnClickListener(v -> dismiss());

        // Submit button
        Button submitButton = view.findViewById(R.id.submit_code_button);
        submitButton.setOnClickListener(v -> {
            // Get the text entered by the user
            String enteredCode = authCodeEditText.getText().toString().trim();

            // Reset TextEdit
            authCodeEditText.setText("");
            authCodeEditText.setHint("Enter code");

            // Check DB when submitted
            db.collection("children")
                    .get()
                    .addOnSuccessListener(childrenSnapshot -> {
                        List<DocumentSnapshot> childrenDocs = childrenSnapshot.getDocuments();
                        boolean isFound = false;
                        for (DocumentSnapshot individualChildDoc : childrenDocs) {
                            // Check if the child doc contain code entered
                            HashMap<String, Object> shareCodes = (HashMap<String, Object>) individualChildDoc.get("shareCodes");
                            if(shareCodes == null){
                                continue;
                            }
                            if (shareCodes.containsKey(enteredCode)) {
                                Map<String, Object> codeData = (Map<String, Object>) shareCodes.get(enteredCode);
                                Boolean revoked = (Boolean) codeData.get("revoked");
                                if (revoked != null && !revoked) {
                                    updateChildProviderId(individualChildDoc.getId(), enteredCode);
                                } else {
                                    Toast.makeText(requireContext(), "Cannot Link", Toast.LENGTH_SHORT).show();
                                }
                                isFound = true;
                                Toast.makeText(requireContext(), "Linked Successfully", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                        if(!isFound){
                            Toast.makeText(requireContext(), "No Valid Code Found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Error fetching children collection", Toast.LENGTH_SHORT).show());
        });

        return view;
    }

    private void updateChildProviderId(String childID, String enteredCode){
        db.collection("children")
                .document(childID)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Child child = doc.toObject(Child.class);
                    if (child == null) return;

                    // Try to bind
                    boolean success = child.bindProviderWithShareCode(providerId, enteredCode);

                    if (!success) {
                        Toast.makeText(requireContext(), "Code expired or already used", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Write updated child back to Firestore
                    Map<String, Object> updatedMap = child.childToMap(child);

                    db.collection("children")
                            .document(childID)
                            .set(updatedMap)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireContext(), "Linked Successfully", Toast.LENGTH_SHORT).show();
                                if (listener != null) listener.onDataChanged();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(requireContext(), "Failed to save link: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load child: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            // Make dialogue background fully transparent
            getDialog().getWindow().setBackgroundDrawable(
                    new InsetDrawable(new ColorDrawable(Color.TRANSPARENT), 0)
            );
        }
    }

    public void setOnDataChangedListener(OnDataChangedListener listener){
        this.listener = listener;
    }

    public interface OnDataChangedListener {
        void onDataChanged();
    }
}
