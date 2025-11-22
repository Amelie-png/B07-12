package com.example.demoapp.repository

import com.example.demoapp.models.Child
import com.google.firebase.firestore.FirebaseFirestore

class ChildRepository {

    private val db = FirebaseFirestore.getInstance()

    fun addChild(child: Child, onComplete: (Boolean) -> Unit) {
        val childId = db.collection("children").document().id

        db.collection("children")
            .document(childId)
            .set(child)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getChildren(parentId: String, callback: (List<Child>) -> Unit) {
        db.collection("children")
            .whereEqualTo("parentId", parentId)
            .get()
            .addOnSuccessListener { snapshot ->
                val result = snapshot.toObjects(Child::class.java)
                callback(result)
            }
    }

    fun updateSharing(childId: String, field: String, value: Boolean) {
        db.collection("children")
            .document(childId)
            .update("sharing.$field", value)
    }
}
