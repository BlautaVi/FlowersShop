package com.example.flowersshop
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flowersshop.models.ProductItem
import com.google.firebase.firestore.FirebaseFirestore

class ProductEditor(
    private val db: FirebaseFirestore,
    private val context: AppCompatActivity,
    private val product: ProductItem
) {
    fun updateProduct(name: String, type: String, priceStr: String, desc: String, quantity: Int, onSuccess: () -> Unit) {
        if (!validateInput(name, type, priceStr, desc, quantity)) return

        val updatedPrice = priceStr.toDoubleOrNull() ?: run {
            Toast.makeText(context, "Ціна має бути числом", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedProduct = hashMapOf(
            "name" to name,
            "type" to type,
            "price" to updatedPrice,
            "description" to desc,
            "photoUrl" to product.photoUrl,
            "userId" to product.userId,
            "availableQuantity" to quantity
        )

        db.collection("items").document(product.id)
            .set(updatedProduct)
            .addOnSuccessListener {
                Toast.makeText(context, "Товар оновлено", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun deleteProduct(onSuccess: () -> Unit) {
        db.collection("items").document(product.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Товар видалено", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateInput(name: String, type: String, priceStr: String, desc: String, quantity: Int): Boolean {
        if (name.isEmpty() || type.isEmpty() || priceStr.isEmpty() || desc.isEmpty() || quantity < 0) {
            Toast.makeText(context, "Заповніть усі поля коректно, кількість не може бути від’ємною", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
