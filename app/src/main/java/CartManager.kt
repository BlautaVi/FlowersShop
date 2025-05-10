package com.example.flowersshop
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.flowersshop.models.CartItem
import com.google.firebase.firestore.FirebaseFirestore
class CartManager(
    private val db: FirebaseFirestore,
    private val userId: String?,
    private val cartItems: MutableList<CartItem>,
    private val cartAdapter: CartItemsAdapter?,
    private val totalPriceText: TextView?
) {
    fun loadCartItems() {
        if (userId == null) return
        db.collection("cart")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                cartItems.clear()
                for (document in documents) {
                    val cartItem = CartItem(
                        id = document.id,
                        userId = document.getString("userId") ?: "",
                        productId = document.getString("productId") ?: "",
                        productName = document.getString("productName") ?: "",
                        productType = document.getString("productType") ?: "",
                        productPrice = document.getDouble("productPrice") ?: 0.0,
                        productPhotoUrl = document.getString("productPhotoUrl") ?: "",
                        quantity = document.getLong("quantity")?.toInt() ?: 1
                    )
                    cartItems.add(cartItem)
                    Log.d("CartItemLoad", "Loaded item: \${cartItem.productName}, ID: \${cartItem.id}, Quantity: \${cartItem.quantity}, ProductId: \${cartItem.productId}")
                }
                cartAdapter?.notifyDataSetChanged()
                updateTotalPrice()
                Log.d("CartLoad", "Loaded ${cartItems.size} unique items")
            }
            .addOnFailureListener {
                Log.e("CartLoad", "Error loading cart items: \${it.message}")
                Toast.makeText(db.app.applicationContext, "Помилка завантаження товарів: \${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    fun removeCartItem(cartItem: CartItem) {
        if (userId == null) return
        val currentQuantity = cartItem.quantity
        if (currentQuantity > 1) {
            val updatedItem = cartItem.copy(quantity = currentQuantity - 1)
            db.collection("cart")
                .document(cartItem.id)
                .set(updatedItem)
                .addOnSuccessListener {
                    val index = cartItems.indexOf(cartItem)
                    if (index != -1) {
                        cartItems[index] = updatedItem
                        cartAdapter?.notifyDataSetChanged()
                        updateTotalPrice()
                        Log.d("CartUpdate", "Quantity reduced for ${cartItem.productName} to ${updatedItem.quantity}")
                        Toast.makeText(db.app.applicationContext, "Кількість товару зменшено", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("CartUpdate", "Error updating quantity: ${e.message}")
                    Toast.makeText(db.app.applicationContext, "Помилка оновлення кількості: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            db.collection("cart")
                .document(cartItem.id)
                .delete()
                .addOnSuccessListener {
                    cartItems.remove(cartItem)
                    cartAdapter?.notifyDataSetChanged()
                    updateTotalPrice()
                    Log.d("CartUpdate", "Item ${cartItem.productName} removed from cart")
                    Toast.makeText(db.app.applicationContext, "Товар видалено з кошика", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("CartUpdate", "Error deleting item: ${e.message}")
                    Toast.makeText(db.app.applicationContext, "Помилка видалення товару: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    fun clearCart() {
        if (userId == null) return
        db.collection("cart")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    cartItems.clear()
                    cartAdapter?.notifyDataSetChanged()
                    updateTotalPrice()
                    return@addOnSuccessListener
                }
                db.runBatch { batch ->
                    for (document in documents) {
                        batch.delete(document.reference)
                    }
                }.addOnSuccessListener {
                    cartItems.clear()
                    cartAdapter?.notifyDataSetChanged()
                    updateTotalPrice()
                }.addOnFailureListener { e ->
                    Toast.makeText(db.app.applicationContext, "Помилка очищення кошика: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(db.app.applicationContext, "Помилка завантаження кошика: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    fun updateTotalPrice() {
        val total = cartItems.sumOf { it.productPrice * it.quantity }
        totalPriceText?.text = "Загальна сума: $total грн"
    }
}
