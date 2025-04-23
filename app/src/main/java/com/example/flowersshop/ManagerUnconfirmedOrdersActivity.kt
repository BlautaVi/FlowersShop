package com.example.flowersshop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ManagerUnconfirmedOrdersActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var ordersRecyclerView: RecyclerView
    private val unconfirmedOrdersList = mutableListOf<Order>()
    private lateinit var adapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manager_unconfirmed_orders)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (auth.currentUser?.email != "manager@gmail.com") {
            Toast.makeText(this, "Доступно лише для менеджера", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        ordersRecyclerView = findViewById(R.id.unconfirmed_orders_recycler_view)
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        ordersRecyclerView.setHasFixedSize(true)

        adapter = OrderAdapter(unconfirmedOrdersList, showUserId = true) { order ->
            showUnconfirmedOrderDetails(order)
        }
        ordersRecyclerView.adapter = adapter

        loadUnconfirmedOrders()

        val backButton = findViewById<Button>(R.id.back_b_unconfirmed)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadUnconfirmedOrders() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val documents = db.collection("orders")
                    .whereEqualTo("status", "unconfirmed")
                    .get()
                    .await()

                runOnUiThread {
                    unconfirmedOrdersList.clear()
                    for (document in documents) {
                        val orderId = document.id
                        val userId = document.getString("userId")
                        val orderDateMillis = document.getLong("orderDate") ?: 0L
                        val totalPrice = document.getDouble("totalPrice") ?: 0.0
                        val items = document.get("items") as? List<Map<String, Any>> ?: emptyList()
                        unconfirmedOrdersList.add(Order(orderId, userId, orderDateMillis, totalPrice, items))
                    }
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ManagerUnconfirmedOrdersActivity, "Помилка завантаження: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

    private fun updateOrderStatus(orderId: String, status: String) {
        val user = auth.currentUser
        Log.d("ManagerUnconfirmed", "Спроба оновити статус замовлення. Поточний User: ${user?.uid}, Email: ${user?.email}")
        if (user == null || user.email != "manager@gmail.com") {
            Toast.makeText(this, "Помилка: Ви не автентифіковані як менеджер", Toast.LENGTH_SHORT).show()
            return
        }

        user.getIdToken(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                db.collection("orders").document(orderId)
                    .update("status", status)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Замовлення оновлено!", Toast.LENGTH_SHORT).show()
                        loadUnconfirmedOrders()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Помилка автентифікації: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}