package com.example.flowersshop
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class SalesAnalyzer(private val db: FirebaseFirestore) {
    fun analyzeSales(onComplete: (Map<String, Float>) -> Unit) {
        db.collection("orders")
            .get()
            .addOnSuccessListener { documents ->
                val sales = mutableMapOf<String, Float>()
                for (document in documents) {
                    val items = document.get("items") as? List<Map<String, Any>> ?: continue
                    for (item in items) {
                        val productName = item["productName"] as? String ?: continue
                        val quantity = (item["quantity"] as? Long)?.toFloat() ?: (item["quantity"] as? Double)?.toFloat() ?: 0f
                        sales[productName] = sales.getOrDefault(productName, 0f) + quantity
                    }
                }
                onComplete(sales)
            }
            .addOnFailureListener { e ->
                Toast.makeText(db.app.applicationContext, "Помилка завантаження даних: \${e.message}", Toast.LENGTH_SHORT).show()
                onComplete(emptyMap())
            }
    }
}