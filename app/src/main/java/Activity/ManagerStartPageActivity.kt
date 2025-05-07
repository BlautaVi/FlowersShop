package Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.flowersshop.R

class ManagerStartPageActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manager_start_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val add_item = findViewById<Button>(R.id.man_add_items_b)
        val see_items = findViewById<Button>(R.id.man_see_all_b)
        val unconfirmed_orders = findViewById<Button>(R.id.man_unconfirmed_orders_b)
        val confirmed_orders = findViewById<Button>(R.id.man_confirmed_orders_b)
        val exit_b = findViewById<Button>(R.id. man_exit_b)
        val analyticsButton = findViewById<ImageButton>(R.id.analyz_items_button)
        analyticsButton.setOnClickListener {
        val intent = Intent(this, manager_sales_analyst::class.java)
            startActivity(intent)
        }
        exit_b.setOnClickListener {
            auth.signOut()
            if (auth.currentUser == null) {
                Toast.makeText(this, "Ви успішно вийшли з акаунта", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Помилка при виході", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        add_item.setOnClickListener {
            startActivity(Intent(this, ManagerAddItemActivity::class.java))
        }
        see_items.setOnClickListener {
            startActivity(Intent(this, MainPageActivity::class.java))
        }
        unconfirmed_orders.setOnClickListener {
            startActivity(Intent(this, ManagerUnconfirmedOrdersActivity::class.java))
        }
        confirmed_orders.setOnClickListener {
            startActivity(Intent(this, ManagerOrders::class.java))
        }
    }
}