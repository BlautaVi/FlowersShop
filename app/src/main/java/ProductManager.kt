package com.example.flowersshop
import android.widget.Toast
import com.example.flowersshop.models.ProductItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductManager(private val db: FirebaseFirestore) {
    fun loadUserProducts(userId: String, onComplete: (List<ProductItem>) -> Unit) {
        db.collection("items")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val products = mutableListOf<ProductItem>()
                for (document in result) {
                    try {
                        val product = document.toObject(ProductItem::class.java).copy(id = document.id)
                        products.add(product)
                    } catch (e: Exception) {
                        Toast.makeText(db.app.applicationContext, "Помилка: \${document.id}", Toast.LENGTH_SHORT).show()
                    }
                }
                onComplete(products)
            }
            .addOnFailureListener { e ->
                Toast.makeText(db.app.applicationContext, "Помилка: \${e.message}", Toast.LENGTH_SHORT).show()
                onComplete(emptyList())
            }
    }
    suspend fun loadProductDetails(productId: String): ProductItem? {
        return try {
            val document = db.collection("items").document(productId).get().await()
            if (document.exists()) {
                document.toObject(ProductItem::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            Toast.makeText(db.app.applicationContext, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }
    fun loadAllProducts(onComplete: (List<ProductItem>) -> Unit) {
        db.collection("items")
            .get()
            .addOnSuccessListener { result ->
                val products = mutableListOf<ProductItem>()
                for (document in result) {
                    try {
                        val product = document.toObject(ProductItem::class.java).copy(id = document.id)
                        products.add(product)
                    } catch (e: Exception) {
                        Toast.makeText(db.app.applicationContext, "Помилка десеріалізації документа: \${document.id}", Toast.LENGTH_SHORT).show()
                    }
                }
                onComplete(products)
            }
            .addOnFailureListener { e ->
                Toast.makeText(db.app.applicationContext, "Помилка: \${e.message}", Toast.LENGTH_SHORT).show()
                onComplete(emptyList())
            }
    }
}
