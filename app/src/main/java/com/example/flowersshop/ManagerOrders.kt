package com.example.flowersshop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ManagerOrders : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var confirmedOrdersRecyclerView: RecyclerView
    private val confirmedOrdersList = mutableListOf<Order>()
    private lateinit var adapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manager_confirmed_orders)
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

        findViewById<TextView>(R.id.confirmed_orders_label).text = "Підтверджені замовлення"

        confirmedOrdersRecyclerView = findViewById(R.id.confirmed_orders_recycler_view)
        confirmedOrdersRecyclerView.layoutManager = LinearLayoutManager(this)
        confirmedOrdersRecyclerView.setHasFixedSize(true)

        adapter = OrderAdapter(confirmedOrdersList, showUserId = true) { order ->
            showConfirmedOrderDetails(order)
        }
        confirmedOrdersRecyclerView.adapter = adapter

        loadConfirmedOrders()

        val backButton = findViewById<ImageButton>(R.id.back_b_confirmed)
        backButton.setOnClickListener {
            finish()
        }

    }

    private fun loadConfirmedOrders() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val documents = db.collection("orders")
                    .whereEqualTo("status", "confirmed")
                    .get()
                    .await()

                runOnUiThread {
                    confirmedOrdersList.clear()
                    for (document in documents) {
                        val orderId = document.id
                        val userId = document.getString("userId")
                        val orderDateMillis = document.getLong("orderDate") ?: 0L
                        val totalPrice = document.getDouble("totalPrice") ?: 0.0
                        val items = document.get("items") as? List<Map<String, Any>> ?: emptyList()
                        confirmedOrdersList.add(Order(orderId, userId, orderDateMillis, totalPrice, items))
                    }
                    adapter.notifyDataSetChanged() }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ManagerOrders, "Помилка завантаження: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showConfirmedOrderDetails(order: Order) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_unconfirmed_order_details, null)

        val detailsTextView = dialogView.findViewById<TextView>(R.id.order_details_text)
        val confirmButton = dialogView.findViewById<ImageButton>(R.id.confirm_order_button)
        val cancelButton = dialogView.findViewById<ImageButton>(R.id.cancel_order_button)

        confirmButton.visibility = View.GONE

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val orderDate = dateFormat.format(Date(order.orderDateMillis))

        val detailsBuilder = StringBuilder()
        detailsBuilder.append("Користувач: ${order.userId}\n")
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

    private fun deleteOrder(orderId: String) {
        db.collection("orders").document(orderId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Замовлення видалено", Toast.LENGTH_SHORT).show()
                loadConfirmedOrders()
            }
            .addOnFailureListener { e ->
                Log.e("ManagerOrders", "Помилка видалення замовлення: ${e.message}", e)
                Toast.makeText(this, "Помилка видалення: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}