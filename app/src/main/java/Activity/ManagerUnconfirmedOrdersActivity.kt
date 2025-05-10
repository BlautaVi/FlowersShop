package Activity

import android.os.Bundle
import android.widget.ImageButton
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

class ManagerUnconfirmedOrdersActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var ordersRecyclerView: RecyclerView
    private val unconfirmedOrdersList = mutableListOf<Order>()
    private lateinit var orderManager: OrderManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manager_unconfirmed_orders)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        orderManager = OrderManager(auth, db, this, unconfirmedOrdersList)

        if (!orderManager.isManager()) {
            Toast.makeText(this, "Доступно лише для менеджера", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        ordersRecyclerView = findViewById(R.id.unconfirmed_orders_recycler_view)
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        ordersRecyclerView.setHasFixedSize(true)

        orderManager.setupOrderAdapter(ordersRecyclerView) { order ->
            orderManager.showUnconfirmedOrderDetails(order)
        }

        val backButton = findViewById<ImageButton>(R.id.back_b_unconfirmed)
        backButton.setOnClickListener { finish() }

        loadUnconfirmedOrders()
    }

    private fun loadUnconfirmedOrders() {
        orderManager.loadUnconfirmedOrders()
    }
}