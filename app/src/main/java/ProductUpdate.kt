package com.example.flowersshop
import com.google.firebase.firestore.FirebaseFirestore
data class ProductUpdate(
    val name: String,
    val type: String,
    val price: Double,
    val description: String,
    val availableQuantity: Int
) {
    fun updateInFirestore(db: FirebaseFirestore, productId: String, onComplete: (Boolean) -> Unit) {
        val updatedProduct = hashMapOf(
            "name" to name,
            "type" to type,
            "price" to price,
            "description" to description,
            "availableQuantity" to availableQuantity
        )
        db.collection("items").document(productId)
            .update(updatedProduct as Map<String, Any>)
        .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
    companion object {
        fun deleteFromFirestore(db: FirebaseFirestore, productId: String, onComplete: (Boolean) -> Unit) {
            db.collection("items").document(productId)
                .delete()
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        }
    }
}
