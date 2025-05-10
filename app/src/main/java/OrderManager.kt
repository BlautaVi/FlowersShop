package com.example.flowersshop

import Adapters.OrderAdapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderManager(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val context: AppCompatActivity,
    private val ordersList: MutableList<Order>
) {
    private lateinit var adapter: OrderAdapter

    fun isManager(): Boolean = auth.currentUser?.email == "manager@gmail.com"

    fun setupOrderAdapter(recyclerView: RecyclerView, onOrderClick: (Order) -> Unit) {
        adapter = OrderAdapter(ordersList, isManager(), onOrderClick)
        recyclerView.adapter = adapter
    }

    fun loadOrders() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val query = if (isManager()) {
                db.collection("orders").get()
            } else {
                db.collection("orders")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", "confirmed")
                    .get()
            }

            query.addOnSuccessListener { documents ->
                Log.d("CustomersOrders", "Found ${documents.size()} orders")
                ordersList.clear()
                if (documents.isEmpty) {
                    Toast.makeText(
                        context,
                        if (isManager()) "Немає замовлень" else "У вас немає підтверджених замовлень",
                        Toast.LENGTH_SHORT
                    ).show()
                    adapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }
                for (document in documents) {
                    val orderId = document.id
                    val userId = document.getString("userId")
                    val orderDateMillis = document.getLong("orderDate") ?: 0L
                    val totalPrice = document.getDouble("totalPrice") ?: 0.0
                    val items = document.get("items") as? List<Map<String, Any>> ?: emptyList()
                    val status = document.getString("status") ?: "unconfirmed"
                    ordersList.add(Order(orderId, userId, orderDateMillis, totalPrice, items, status))
                }
                adapter.notifyDataSetChanged()
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Помилка завантаження: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Користувач не автентифікований", Toast.LENGTH_SHORT).show()
            context.finish()
        }
    }

    fun loadUnconfirmedOrders() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val documents = db.collection("orders")
                    .whereEqualTo("status", "unconfirmed")
                    .get()
                    .await()

                context.runOnUiThread {
                    ordersList.clear()
                    for (document in documents) {
                        val orderId = document.id
                        val userId = document.getString("userId")
                        val orderDateMillis = document.getLong("orderDate") ?: 0L
                        val totalPrice = document.getDouble("totalPrice") ?: 0.0
                        val items = document.get("items") as? List<Map<String, Any>> ?: emptyList()
                        val status = document.getString("status") ?: "unconfirmed"
                        ordersList.add(Order(orderId, userId, orderDateMillis, totalPrice, items, status))
                    }
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                context.runOnUiThread {
                    Toast.makeText(context, "Помилка завантаження: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun loadOrdersWithUserItems(userId: String, onComplete: (List<Order>) -> Unit) {
        db.collection("items")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { userItemsSnapshot ->
                val userProductIds = mutableSetOf<String>()
                for (doc in userItemsSnapshot) {
                    val itemId = doc.getString("productId")
                    if (itemId != null) userProductIds.add(itemId)
                }

                db.collection("orders")
                    .whereEqualTo("status", "confirmed")
                    .get()
                    .addOnSuccessListener { ordersSnapshot ->
                        val confirmedOrders = mutableListOf<Order>()
                        for (document in ordersSnapshot) {
                            val orderId = document.id
                            val orderUserId = document.getString("userId") ?: "невідомий_користувач"
                            val orderDateMillis = document.getLong("orderDate") ?: 0L
                            val totalPrice = document.getDouble("totalPrice") ?: 0.0
                            val items = document.get("items") as? List<Map<String, Any>> ?: emptyList()
                            val status = document.getString("status") ?: "непідтверджено"
                            var itemFound = false

                            for (item in items) {
                                val productId = item["productId"] as? String
                                if (productId != null && userProductIds.contains(productId)) {
                                    confirmedOrders.add(Order(orderId, orderUserId, orderDateMillis, totalPrice, items, status))
                                    itemFound = true
                                    break
                                }
                            }
                            if (!itemFound) {
                                Log.d("CustomersOrdersForSales", "Замовлення $orderId не містить товарів користувача")
                            }
                        }
                        onComplete(confirmedOrders)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Помилка завантаження підтверджених замовлень: ${e.message}", Toast.LENGTH_SHORT).show()
                        onComplete(emptyList())
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Помилка завантаження товарів користувача: ${e.message}", Toast.LENGTH_SHORT).show()
                onComplete(emptyList())
            }
    }

    fun showOrderDetailsDialog(order: Order) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_order_details, null)
        val detailsTextView = dialogView.findViewById<TextView>(R.id.order_details_text)
        val deleteButton = dialogView.findViewById<ImageButton>(R.id.delete_order_button)
        deleteButton.visibility = View.GONE

        val detailsBuilder = StringBuilder()
        detailsBuilder.append("Дата замовлення: ${order.getFormattedOrderDate()}\n")
        detailsBuilder.append("Загальна сума: ${order.totalPrice} грн\n\n")
        detailsBuilder.append("Товари:\n")

        for (item in order.items) {
            val productName = item["productName"] as? String ?: "Невідомий товар"
            val productType = item["productType"] as? String ?: "Невідомий тип"
            val productPrice = item["productPrice"] as? Double ?: 0.0
            val quantity = (item["quantity"] as? Long)?.toInt() ?: 1
            detailsBuilder.append("- $productName ($productType): $productPrice грн x $quantity\n")
        }

        detailsTextView.text = detailsBuilder.toString()

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Деталі замовлення")
            .setNegativeButton("Закрити") { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()
    }

    fun showUnconfirmedOrderDetails(order: Order) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_unconfirmed_order_details, null)
        val detailsTextView = dialogView.findViewById<TextView>(R.id.order_details_text)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirm_order_button)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancel_order_button)

        val detailsBuilder = StringBuilder()
        detailsBuilder.append("Користувач ID: ${order.userId}\n")
        detailsBuilder.append("Дата замовлення: ${order.getFormattedOrderDate()}\n")
        detailsBuilder.append("Загальна сума: ${order.totalPrice} грн\n\n")
        detailsBuilder.append("Товари:\n")

        for (item in order.items) {
            val productName = item["productName"] as? String ?: "Невідомий товар"
            val productType = item["productType"] as? String ?: "Невідомий тип"
            val productPrice = item["productPrice"] as? Double ?: 0.0
            val quantity = (item["quantity"] as? Long)?.toInt() ?: 1
            detailsBuilder.append("- $productName ($productType): $productPrice грн x $quantity\n")
        }

        detailsTextView.text = detailsBuilder.toString()

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Деталі замовлення")
            .setNegativeButton("Закрити") { dialog, _ -> dialog.dismiss() }
            .create()

        confirmButton.setOnClickListener {
            updateOrderStatus(order.id, "confirmed")
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            updateOrderStatus(order.id, "cancelled")
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showConfirmedOrderDetails(order: Order) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_unconfirmed_order_details, null)
        val detailsTextView = dialogView.findViewById<TextView>(R.id.order_details_text)
        val confirmButton = dialogView.findViewById<ImageButton>(R.id.confirm_order_button)
        val cancelButton = dialogView.findViewById<ImageButton>(R.id.cancel_order_button)

        confirmButton.visibility = View.GONE
        val detailsBuilder = StringBuilder()
        detailsBuilder.append("Користувач ID: ${order.userId}\n")
        detailsBuilder.append("Дата: ${order.getFormattedOrderDate()}\n")
        detailsBuilder.append("Сума: ${order.totalPrice} грн\n\n")
        detailsBuilder.append("Товари:\n")

        for (item in order.items) {
            val productName = item["productName"] as? String ?: "Невідомий товар"
            val productType = item["productType"] as? String ?: "Невідомий тип"
            val productPrice = item["productPrice"] as? Double ?: 0.0
            val quantity = (item["quantity"] as? Long)?.toInt() ?: 1
            detailsBuilder.append("- $productName ($productType): $productPrice грн x $quantity\n")
        }

        detailsTextView.text = detailsBuilder.toString()

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Деталі замовлення")
            .setNegativeButton("Закрити") { dialog, _ -> dialog.dismiss() }
            .create()

        cancelButton.setOnClickListener {
            deleteOrder(order.id)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateOrderStatus(orderId: String, status: String) {
        db.collection("orders").document(orderId)
            .update("status", status)
            .addOnSuccessListener {
                Toast.makeText(context, "Замовлення оновлено на '$status'", Toast.LENGTH_SHORT).show()
                loadOrders()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteOrder(orderId: String) {
        db.collection("orders").document(orderId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Замовлення видалено", Toast.LENGTH_SHORT).show()
                loadOrders()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Помилка видалення: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}