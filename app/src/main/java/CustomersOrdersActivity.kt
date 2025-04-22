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
    private val confirmedOrdersList = mutableListOf<Order>()
    private lateinit var adapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customers_orders_for_sales) // Використовуємо існуючий layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.ur_orders_l).text = "Ваші замовлення"

        ordersRecyclerView = findViewById(R.id.listView) as RecyclerView
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        ordersRecyclerView.setHasFixedSize(true)

        adapter = OrderAdapter(confirmedOrdersList) { order ->
            showOrderDetailsDialog(order)
        }
        ordersRecyclerView.adapter = adapter

        loadConfirmedUserOrders()
    }

    private fun loadConfirmedUserOrders() {
        if (userId != null) {
            db.collection("orders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "confirmed")
                .get()
                .addOnSuccessListener { documents ->
                    confirmedOrdersList.clear()
                    if (documents.isEmpty) {
                        Toast.makeText(this, "У вас немає підтверджених замовлень", Toast.LENGTH_SHORT).show()
                        adapter.notifyDataSetChanged()
                        return@addOnSuccessListener
                    }
                    for (document in documents) {
                        val orderId = document.id
                        val orderDateMillis = document.getLong("orderDate") ?: 0L
                        val totalPrice = document.getDouble("totalPrice") ?: 0.0
                        val items = document.get("items") as? List<Map<String, Any>> ?: emptyList()
                        confirmedOrdersList.add(Order(orderId, userId, orderDateMillis, totalPrice, items))
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("CustomersOrders", "Помилка завантаження замовлень: ${e.message}", e)
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
}