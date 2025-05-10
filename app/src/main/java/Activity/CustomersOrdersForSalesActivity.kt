package Activity

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flowersshop.Order
import com.example.flowersshop.OrderManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.flowersshop.R

class CustomersOrdersForSalesActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var ordersRecyclerView: RecyclerView
    private val ordersList = mutableListOf<Order>()
    private lateinit var orderManager: OrderManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customers_orders_for_sales)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        orderManager = OrderManager(auth, db, this, ordersList)

        findViewById<TextView>(R.id.ur_orders_l).text = "Замовлення з вашими товарами"

        ordersRecyclerView = findViewById(R.id.listView) as RecyclerView
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        ordersRecyclerView.setHasFixedSize(true)

        orderManager.setupOrderAdapter(ordersRecyclerView) { order ->
            orderManager.showOrderDetailsDialog(order)
        }

        val backBtn = findViewById<ImageButton>(R.id.back_b_confirmed)
        backBtn.setOnClickListener { finish() }

        loadOrdersWithUserItems()
    }

    private fun loadOrdersWithUserItems() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            orderManager.loadOrdersWithUserItems(userId) { orders ->
                ordersList.clear()
                ordersList.addAll(orders)
                if (ordersList.isEmpty()) {
                    Toast.makeText(this, "Немає підтверджених замовлень з вашими товарами", Toast.LENGTH_SHORT).show()
                }
                ordersRecyclerView.adapter?.notifyDataSetChanged()
            }
        } else {
            Toast.makeText(this, "Користувач не автентифікований", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}