package Activity

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.example.flowersshop.R
import com.example.flowersshop.CartItemsAdapter
import com.example.flowersshop.CartManager
import com.example.flowersshop.NovaPoshtaService
import com.example.flowersshop.OrderProcessor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OrderingItemCActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userId = auth.currentUser?.uid
    private lateinit var requestQueue: RequestQueue
    private val novaPoshtaApiKey = "51662651de0f1827a0501a581708f343"
    private lateinit var postOfficeSpinner: Spinner
    private lateinit var nameText: EditText
    private lateinit var addressText: EditText
    private lateinit var phoneText: EditText
    private lateinit var changeCityButton: ImageButton
    private lateinit var confirmOrderButton: Button
    private lateinit var cancelOrderButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var itemsListView: ListView
    private lateinit var totalPriceText: TextView
    private val cartItems = mutableListOf<com.example.flowersshop.models.CartItem>()
    private lateinit var cartAdapter: CartItemsAdapter
    private lateinit var cartManager: CartManager
    private lateinit var novaPoshtaService: NovaPoshtaService
    private lateinit var orderProcessor: OrderProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ordering_item_c)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backBtn = findViewById<ImageButton>(R.id.back_b_confirmed)
        backBtn.setOnClickListener { finish() }

        postOfficeSpinner = findViewById(R.id.spinner)
        nameText = findViewById(R.id.name_text)
        addressText = findViewById(R.id.address_text)
        phoneText = findViewById(R.id.phone_text)
        changeCityButton = findViewById(R.id.change_city_button)
        confirmOrderButton = findViewById(R.id.confirm_order_b)
        cancelOrderButton = findViewById(R.id.cancel_order_b)
        progressBar = findViewById(R.id.progressBar)
        itemsListView = findViewById(R.id.items_for_order_c_list)
        totalPriceText = findViewById(R.id.total_price_text)
        requestQueue = Volley.newRequestQueue(this)
        novaPoshtaService = NovaPoshtaService(requestQueue, novaPoshtaApiKey, progressBar, this, postOfficeSpinner)
        cartManager = CartManager(db, userId, cartItems, null, totalPriceText)
        cartAdapter = CartItemsAdapter(this, cartItems, cartManager)
        itemsListView.adapter = cartAdapter
        cartManager = CartManager(db, userId, cartItems, cartAdapter, totalPriceText)
        orderProcessor = OrderProcessor(db, userId, cartItems, this, progressBar, cartAdapter)

        if (userId != null) {
            loadUserData()
            cartManager.loadCartItems()
        } else {
            Toast.makeText(this, "Користувач не автентифікований", Toast.LENGTH_SHORT).show()
            finish()
        }

        changeCityButton.setOnClickListener {
            val newCity = orderProcessor.extractCityFromAddress(addressText.text.toString().trim())
            if (newCity.isEmpty()) {
                Toast.makeText(this, "Введіть місто", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            novaPoshtaService.loadWarehouses(newCity)
        }

        confirmOrderButton.setOnClickListener {
            handleOrderConfirmation()
        }

        cancelOrderButton.setOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        db.collection("users").document(userId!!).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: ""
                    val address = document.getString("address") ?: ""
                    val phone = document.getString("phoneNumber") ?: ""
                    nameText.setText(name)
                    addressText.setText(address)
                    phoneText.setText(phone)
                    val city = orderProcessor.extractCityFromAddress(address)
                    if (city.isNotEmpty()) {
                        novaPoshtaService.loadWarehouses(city)
                    } else {
                        Toast.makeText(this, "Не вдалося визначити місто з адреси", Toast.LENGTH_SHORT).show()
                        novaPoshtaService.setEmptySpinner()
                    }
                } else {
                    Toast.makeText(this, "Дані користувача не знайдено", Toast.LENGTH_SHORT).show()
                    novaPoshtaService.setEmptySpinner()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Помилка при завантаженні профілю: ${e.message}", Toast.LENGTH_SHORT).show()
                novaPoshtaService.setEmptySpinner()
            }
    }

    private fun handleOrderConfirmation() {
        val selectedPostOffice = postOfficeSpinner.selectedItem?.toString()
        val name = nameText.text.toString().trim()
        val address = addressText.text.toString().trim()
        val phone = phoneText.text.toString().trim()
        orderProcessor.handleOrderConfirmation(selectedPostOffice, name, address, phone)
    }

    override fun onDestroy() {
        super.onDestroy()
        requestQueue.cancelAll { true }
    }
}