package com.example.flowersshop

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    constructor() : this(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isManager(): Boolean = auth.currentUser?.email == "manager@gmail.com"

    fun signOut(): Boolean {
        auth.signOut()
        return auth.currentUser == null
    }
    fun saveProduct(productId: String, product: Map<String, Any>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("items").document(productId).set(product)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun deleteProduct(productId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("items").document(productId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun saveUser(userId: String, userData: Map<String, String>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(userId).set(userData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}