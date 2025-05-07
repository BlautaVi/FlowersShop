package com.example.flowersshop

import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flowersshop.models.CartItem
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import java.util.regex.Pattern

class OrderProcessor(
    private val db: FirebaseFirestore,
    private val userId: String?,
    private val cartItems: List<CartItem>,
    private val context: AppCompatActivity,
    private val progressBar: ProgressBar,
    private val cartAdapter: CartItemsAdapter
) {
    private val PHONE_PATTERN = Pattern.compile("^\\+380d{9}\$")
    fun handleOrderConfirmation(postOffice: String?, name: String, address: String, phone: String) {
        if (postOffice.isNullOrEmpty() || postOffice == "Відділення не знайдено") {
            Toast.makeText(context, "Виберіть дійсне відділення", Toast.LENGTH_SHORT).show()
            return
        }

        val city = extractCityFromAddress(address)
        if (!validateInput(name, address, phone, city)) return
        if (cartItems.isEmpty()) {
            Toast.makeText(context, "Кошик порожній", Toast.LENGTH_SHORT).show()
            return
        }

        checkItemsAvailability { itemsToUpdate ->
            if (itemsToUpdate.size == cartItems.size) {
                createOrder(name, address, phone, city, postOffice, itemsToUpdate)
            } else {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun validateInput(name: String, address: String, phone: String, city: String): Boolean {
        if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || city.isEmpty()) {
            Toast.makeText(context, "Заповніть усі поля", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            Toast.makeText(context, "Номер телефону має бути у форматі +380xxxxxxxxx", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun checkItemsAvailability(onSuccess: (List<Pair<String, Int>>) -> Unit) {
        val itemsToUpdate = mutableListOf<Pair<String, Int>>()
        val tasks = cartItems.map { cartItem ->
            db.collection("items").document(cartItem.productId).get()
        }

        progressBar.visibility = View.VISIBLE
        com.google.android.gms.tasks.Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
            .addOnSuccessListener { documents ->
                var allItemsAvailable = true
                for (i in documents.indices) {
                    val document = documents[i]
                    val cartItem = cartItems[i]
                    if (document.exists()) {
                        val availableQuantity = document.getLong("availableQuantity")?.toInt() ?: 0
                        val isAvailable = document.getBoolean("isAvailable") ?: true
                        if (!isAvailable) {
                            allItemsAvailable = false
                            Toast.makeText(context, "Товар закінчився: ${cartItem.productName}", Toast.LENGTH_SHORT).show()
                            break
                        }
                        if (availableQuantity < cartItem.quantity) {
                            allItemsAvailable = false
                            Toast.makeText(context, "Недостатньо товару: ${cartItem.productName}", Toast.LENGTH_SHORT).show()
                            break
                        } else {
                            itemsToUpdate.add(Pair(cartItem.productId, cartItem.quantity))
                        }
                    } else {
                        allItemsAvailable = false
                        Toast.makeText(context, "Товар не знайдено: ${cartItem.productName}", Toast.LENGTH_SHORT).show()
                        break
                    }
                }

                if (allItemsAvailable) onSuccess(itemsToUpdate)
                else progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Помилка перевірки доступності: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createOrder(name: String, address: String, phone: String, city: String, postOffice: String, itemsToUpdate: List<Pair<String, Int>>) {
        val orderItems = cartItems.map {
            hashMapOf(
                "productId" to UUID.randomUUID().toString(),
                "productName" to it.productName,
                "productType" to it.productType,
                "productPrice" to it.productPrice,
                "quantity" to it.quantity,
                "productPhotoUrl" to it.productPhotoUrl
            )
        }

        val order = hashMapOf(
            "userId" to userId,
            "name" to name,
            "address" to address,
            "phone" to phone,
            "city" to city,
            "postOffice" to postOffice,
            "orderDate" to System.currentTimeMillis(),
            "items" to orderItems,
            "totalPrice" to cartItems.sumOf { it.productPrice * it.quantity },
            "status" to "unconfirmed"
        )

        db.collection("orders").add(order)
            .addOnSuccessListener {
                db.runTransaction { transaction ->
                    for ((productId, quantity) in itemsToUpdate) {
                        val docRef = db.collection("items").document(productId)
                        val snapshot = transaction.get(docRef)
                        val currentQuantity = snapshot.getLong("availableQuantity")?.toInt() ?: 0
                        val newQuantity = currentQuantity - quantity
                        transaction.update(docRef, "availableQuantity", newQuantity)
                        if (newQuantity <= 0) {
                            transaction.update(docRef, "isAvailable", false)
                        }
                    }
                }.addOnSuccessListener {
                    CartManager(db, userId, cartItems as MutableList<CartItem>, cartAdapter, null).clearCart()
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Замовлення оформлено!", Toast.LENGTH_SHORT).show()
                    context.finish()
                }.addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Помилка оновлення кількості: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Помилка оформлення замовлення: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun extractCityFromAddress(address: String): String {
        if (address.isEmpty()) return ""
        val parts = address.split(",").map { it.trim() }
        val cityPart = parts.find { part ->
            !part.contains("вул.", ignoreCase = true) &&
                    !part.contains("просп.", ignoreCase = true) &&
                    !part.matches(Regex(".*\\d+.*"))
        } ?: parts.lastOrNull() ?: ""
        return cityPart.replace(Regex("^(м\\.|с\\.)\\s*", RegexOption.IGNORE_CASE), "").trim()
    }
}
