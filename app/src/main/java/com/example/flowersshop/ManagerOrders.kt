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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ManagerOrders : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userId = auth.currentUser?.uid
    private lateinit var ordersRecyclerView: RecyclerView
    private val ordersList = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customers_orders)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.your_orders_l).text = "Всі замовлення"

        ordersRecyclerView = findViewById(R.id.orders_recycler_view)
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        ordersRecyclerView.setHasFixedSize(true)

        val backButton = findViewById<Button>(R.id.back_b)
        backButton.setOnClickListener {
            finish()
        }

        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.e("ManagerOrders", "Google Play Services unavailable: $resultCode")
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, 9000)?.show()
            } else {
                Toast.makeText(this, "Google Play Services is not available on this device", Toast.LENGTH_LONG).show()
                finish()
            }
            return
        }

        if (userId != null) {
            checkUserRoleAndLoadOrders()
        } else {
            Toast.makeText(this, "Користувач не автентифікований", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun checkUserRoleAndLoadOrders() {
        val userEmail = auth.currentUser?.email
        Log.d("ManagerOrders", "User email (before loadAllOrders): $userEmail")
        if (userEmail == "manager@gmail.com") {
            loadAllOrders()
        } else {
            Toast.makeText(this, "Доступно лише для менеджерів", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadAllOrders() {
        db.collection("orders")
            .get()
            .addOnSuccessListener { documents ->
                ordersList.clear()
                if (documents.isEmpty) {
                    Toast.makeText(this, "Замовлень немає", Toast.LENGTH_SHORT).show()
                    ordersRecyclerView.adapter = OrderAdapter(ordersList, showUserId = true) { /* No-op */ }
                    return@addOnSuccessListener
                }

                for (document in documents) {
                    val orderId = document.id
                    val userName = document.getString("name") ?: "Невідомий користувач"
                    val orderDateMillis = document.getLong("orderDate") ?: 0L
                    val totalPrice = document.getDouble("totalPrice") ?: 0.0
                    val items = document.get("items") as? List<Map<String, Any>> ?: emptyList()
                    ordersList.add(Order(orderId, userName, orderDateMillis, totalPrice, items))
                }

                ordersRecyclerView.adapter = OrderAdapter(ordersList, showUserId = true) { order ->
                    showOrderDetails(order)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ManagerOrders", "Помилка завантаження замовлень: ${e.message}", e)
                Toast.makeText(this, "Помилка завантаження: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showOrderDetails(order: Order) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_order_details, null)

        val detailsTextView = dialogView.findViewById<TextView>(R.id.order_details_text)
        val deleteButton = dialogView.findViewById<Button>(R.id.delete_order_button)

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val orderDate = dateFormat.format(Date(order.orderDateMillis))

        val detailsBuilder = StringBuilder()
        detailsBuilder.append("${order.userId}\n")
        detailsBuilder.append("Дата: $orderDate\n")
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
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Деталі замовлення")
            .setNegativeButton("Закрити") { dialog, _ -> dialog.dismiss() }
            .create()
        deleteButton.setOnClickListener {
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
                loadAllOrders()
            }
            .addOnFailureListener { e ->
                Log.e("ManagerOrders", "Помилка видалення замовлення: ${e.message}", e)
                Toast.makeText(this, "Помилка видалення: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}