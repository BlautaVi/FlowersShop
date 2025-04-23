package com.example.flowersshop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomersOrdersActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userId = auth.currentUser?.uid
    private lateinit var ordersRecyclerView: RecyclerView
    private val ordersList = mutableListOf<Order>()
    private lateinit var adapter: OrderAdapter
    private var isManager = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customers_orders)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        isManager = auth.currentUser?.email == "manager@gmail.com"
        findViewById<TextView>(R.id.your_orders_l).text = if (isManager) "Всі замовлення" else "Ваші замовлення"

        ordersRecyclerView = findViewById(R.id.orders_recycler_view)
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        ordersRecyclerView.setHasFixedSize(true)

        adapter = OrderAdapter(ordersList, showUserId = isManager) { order ->
            if (isManager) {
                if (order.status == "unconfirmed") {
                    showUnconfirmedOrderDetails(order)
                } else {
                    showConfirmedOrderDetails(order)
                }
            } else {
                showOrderDetailsDialog(order)
            }
        }
        ordersRecyclerView.adapter = adapter

        loadOrders()

        findViewById<Button>(R.id.back_b).setOnClickListener {
            finish()
        }
    }

    private fun loadOrders() {
        if (userId != null) {
            val query = if (isManager) {
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
                        this,
                        if (isManager) "Немає замовлень" else "У вас немає підтверджених замовлень",
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
                Toast.makeText(this, "Помилка завантаження: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Користувач не автентифікований", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showOrderDetailsDialog(order: Order) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_order_details, null)
        val detailsTextView = dialogView.findViewById<TextView>(R.id.order_details_text)
        val deleteButton = dialogView.findViewById<Button>(R.id.delete_order_button)
        deleteButton.visibility = View.GONE

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val orderDate = dateFormat.format(Date(order.orderDateMillis))

        val detailsBuilder = StringBuilder()
        detailsBuilder.append("Дата замовлення: $orderDate\n")
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

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Деталі замовлення")
            .setNegativeButton("Закрити") { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()
    }

    private fun showUnconfirmedOrderDetails(order: Order) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_unconfirmed_order_details, null)

        val detailsTextView = dialogView.findViewById<TextView>(R.id.order_details_text)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirm_order_button)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancel_order_button)

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val orderDate = dateFormat.format(Date(order.orderDateMillis))

        val detailsBuilder = StringBuilder()
        detailsBuilder.append("Користувач ID: ${order.userId}\n")
        detailsBuilder.append("Дата замовлення: $orderDate\n")
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

        val dialog = AlertDialog.Builder(this)
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

    private fun showConfirmedOrderDetails(order: Order) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_unconfirmed_order_details, null)

        val detailsTextView = dialogView.findViewById<TextView>(R.id.order_details_text)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirm_order_button)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancel_order_button)

        confirmButton.visibility = View.GONE
        cancelButton.text = "Видалити"

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val orderDate = dateFormat.format(Date(order.orderDateMillis))

        val detailsBuilder = StringBuilder()
        detailsBuilder.append("Користувач ID: ${order.userId}\n")
        detailsBuilder.append("Дата: $orderDate\n")
        detailsBuilder.append("Сума: ${order.totalPrice} грн\n\n")
        detailsBuilder.append("Товари:\n")

        for (item in order.items) {
            val productName = item["productName"] as? String ?: "Невідомий товар"
            val productType = item["productType"] as? String ?: "Неввідомий тип"
            val productPrice = item["productPrice"] as? Double ?: 0.0
            val quantity = (item["quantity"] as? Long)?.toInt() ?: 1
            detailsBuilder.append("- $productName ($productType): $productPrice грн x $quantity\n")
        }

        detailsTextView.text = detailsBuilder.toString()

        val dialog = AlertDialog.Builder(this)
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
                Toast.makeText(this, "Замовлення оновлено на '$status'", Toast.LENGTH_SHORT).show()
                loadOrders()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteOrder(orderId: String) {
        db.collection("orders").document(orderId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Замовлення видалено", Toast.LENGTH_SHORT).show()
                loadOrders()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Помилка видалення: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}