package Activity

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
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

class CustomersOrdersActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var ordersRecyclerView: RecyclerView
    private val ordersList = mutableListOf<Order>()
    private lateinit var orderManager: OrderManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customers_orders)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        orderManager = OrderManager(auth, db, this, ordersList)

        ordersRecyclerView = findViewById(R.id.orders_recycler_view)
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        ordersRecyclerView.setHasFixedSize(true)

        orderManager.setupOrderAdapter(ordersRecyclerView) { order ->
            orderManager.showOrderDetailsDialog(order)
        }

        findViewById<TextView>(R.id.your_orders_l).text = "Ваші замовлення"

        val backButton = findViewById<ImageButton>(R.id.back_b_confirmed)
        backButton.setOnClickListener { finish() }

        orderManager.loadOrders()
    }
}