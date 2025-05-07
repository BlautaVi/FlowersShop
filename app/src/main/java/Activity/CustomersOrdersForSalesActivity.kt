package Activity

import Adapters.OrderAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import com.example.flowersshop.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.text.get
import com.example.flowersshop.R

class CustomersOrdersForSalesActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userId = auth.currentUser?.uid
    private lateinit var ordersRecyclerView: RecyclerView
    private val ordersList = mutableListOf<Order>()
    private lateinit var adapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customers_orders_for_sales)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        findViewById<TextView>(R.id.ur_orders_l).text = "Замовлення з вашими товарами"

        ordersRecyclerView = findViewById(R.id.listView) as RecyclerView
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        ordersRecyclerView.setHasFixedSize(true)

        adapter = OrderAdapter(ordersList, showUserId = true) { order ->
            showOrderDetailsDialog(order)
        }
        ordersRecyclerView.adapter = adapter

        loadOrdersWithUserItems()
        val backBtn = findViewById<ImageButton>(R.id.back_b_confirmed)
        backBtn.setOnClickListener() {
            finish()
        }
    }

    private fun loadOrdersWithUserItems() {
        if (userId != null) {
            db.collection("orders")
                .whereEqualTo("status", "confirmed")
                .get()
                .addOnSuccessListener { ordersSnapshot ->
                    val confirmedOrders = mutableListOf<Order>()
                    val userProductIds = mutableSetOf<String>()
                    db.collection("items")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { userItemsSnapshot ->
                            for (doc in userItemsSnapshot) {
                                val itemId = doc.getString("productId")
                                if (itemId != null) {
                                    userProductIds.add(itemId)
                                }
                            }
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
                                        confirmedOrders.add(
                                            Order(
                                                orderId,
                                                orderUserId,
                                                orderDateMillis,
                                                totalPrice,
                                                items,
                                                status
                                            )
                                        )
                                        itemFound = true
                                        break
                                    }
                                }
                                if (!itemFound) {
                                    Log.d("CustomersOrdersForSales", "Замовлення $orderId не містить товарів користувача")
                                }
                            }

                            ordersList.clear()
                            ordersList.addAll(confirmedOrders)

                            if (ordersList.isEmpty()) {
                                Toast.makeText(this, "Немає підтверджених замовлень з вашими товарами", Toast.LENGTH_SHORT).show()
                            }
                            adapter.notifyDataSetChanged()

                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Помилка завантаження товарів користувача: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Помилка завантаження підтверджених замовлень: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Користувач не автентифікований", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showOrderDetailsDialog(order: Order) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_order_details, null)
        val detailsTextView = dialogView.findViewById<TextView>(R.id.order_details_text)
        val deleteButton = dialogView.findViewById<ImageButton>(R.id.delete_order_button)
        deleteButton.visibility = View.GONE

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val orderDate = dateFormat.format(Date(order.orderDateMillis))

        val detailsBuilder = StringBuilder()
        val customerName = order.items.firstOrNull()?.get("customerName") as? String ?: "Невідомий замовник"
        detailsBuilder.append("Замовник: $customerName\n")
        detailsBuilder.append("Дата замовлення: $orderDate\n")
        detailsBuilder.append("Статус: ${order.status}\n")
        detailsBuilder.append("Загальна сума: ${order.totalPrice} грн\n\n")
        detailsBuilder.append("Товари:\n")

        for (item in order.items) {
            val productName = item["productName"] as? String ?: "Невідомий товар"
            val productType = item["productType"] as? String ?: "Невідомий тип"
            val productPrice = item["productPrice"] as? Double ?: 0.0
            val quantity = (item["quantity"] as? Long)?.toInt() ?: 1
            detailsBuilder.append("- $productName ($productType), Ціна: $productPrice грн, Кількість: $quantity\n")
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