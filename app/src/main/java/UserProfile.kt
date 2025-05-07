package com.example.flowersshop

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfile(private val auth: FirebaseAuth, private val db: FirebaseFirestore) {
    fun loadProfile(userId: String, onComplete: (String, String, String) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onComplete(
                        document.getString("name") ?: "",
                        document.getString("address") ?: "",
                        document.getString("phoneNumber") ?: ""
                    )
                } else {
                    Toast.makeText(auth.app.applicationContext, "Дані користувача не знайдено", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(auth.app.applicationContext, "Помилка при завантаженні профілю", Toast.LENGTH_SHORT).show()
            }
    }
    fun updateProfile(userId: String, name: String, address: String, phone: String, onComplete: (Boolean) -> Unit) {
        val updatedUser = hashMapOf(
            "name" to name,
            "address" to address,
            "phoneNumber" to phone,
            "email" to auth.currentUser?.email
        )
        db.collection("users").document(userId)
            .set(updatedUser)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Помилка при оновленні даних: ${e.message}", e)
                onComplete(false)
            }
    }

    fun signOut(onComplete: (Boolean) -> Unit) {
        auth.signOut()
        onComplete(auth.currentUser == null)
    }

    fun deleteAccount(userId: String, onComplete: (Boolean) -> Unit) {
        db.collection("users").document(userId)
            .delete()
            .addOnSuccessListener {
                auth.currentUser?.delete()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onComplete(true)
                        } else {
                            onComplete(false)
                        }
                    }
            }
            .addOnFailureListener { e ->
                onComplete(false)
            }
    }
}
