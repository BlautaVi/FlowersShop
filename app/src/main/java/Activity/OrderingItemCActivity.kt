package Activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.flowersshop.models.CartItem
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.util.UUID
import java.util.regex.Pattern
import com.example.flowersshop.R
import com.example.flowersshop.CartItemsAdapter
import com.example.flowersshop.CartManager

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
    private val cartItems = mutableListOf<CartItem>()
    private lateinit var cartAdapter: CartItemsAdapter
    private lateinit var cartManager: CartManager
    private val PHONE_PATTERN = Pattern.compile("^\\+380\\d{9}\$")

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

        cartManager = CartManager(db, userId, cartItems, null, totalPriceText)

        cartAdapter = CartItemsAdapter(this, cartItems, cartManager)
        itemsListView.adapter = cartAdapter

        cartManager = CartManager(db, userId, cartItems, cartAdapter, totalPriceText)

        if (userId != null) {
            loadUserData()
            cartManager.loadCartItems()
        } else {
            Toast.makeText(this, "Користувач не автентифікований", Toast.LENGTH_SHORT).show()
            finish()
        }
        changeCityButton.setOnClickListener {
            val newCity = extractCityFromAddress(addressText.text.toString().trim())
            if (newCity.isEmpty()) {
                Toast.makeText(this, "Введіть місто", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loadWarehouses(newCity)
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
                    val city = extractCityFromAddress(address)
                    if (city.isNotEmpty()) {
                        loadWarehouses(city)
                    } else {
                        Toast.makeText(this, "Не вдалося визначити місто з адреси", Toast.LENGTH_SHORT).show()
                        setEmptySpinner()
                    }
                } else {
                    Toast.makeText(this, "Дані користувача не знайдено", Toast.LENGTH_SHORT).show()
                    setEmptySpinner()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Помилка при завантаженні профілю: ${e.message}", Toast.LENGTH_SHORT).show()
                setEmptySpinner()
            }
    }

    private fun handleOrderConfirmation() {
        val selectedPostOffice = postOfficeSpinner.selectedItem?.toString()
        if (selectedPostOffice.isNullOrEmpty() || selectedPostOffice == "Відділення не знайдено") {
            Toast.makeText(this, "Виберіть дійсне відділення", Toast.LENGTH_SHORT).show()
            return
        }

        val name = nameText.text.toString().trim()
        val address = addressText.text.toString().trim()
        val phone = phoneText.text.toString().trim()
        val city = extractCityFromAddress(address)

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, "Заповніть усі поля", Toast.LENGTH_SHORT).show()
            return
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            Toast.makeText(this, "Номер телефону має бути у форматі +380xxxxxxxxx", Toast.LENGTH_SHORT).show()
            return
        }
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Кошик порожній", Toast.LENGTH_SHORT).show()
            return
        }

        val itemsToUpdate = mutableListOf<Pair<String, Int>>()
        val tasks = mutableListOf<Task<DocumentSnapshot>>()

        for (cartItem in cartItems) {
            val task = db.collection("items").document(cartItem.productId).get()
            tasks.add(task)
        }
        progressBar.visibility = View.VISIBLE
        Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
            .addOnSuccessListener { documents ->
                var allItemsAvailable = true
                for (i in documents.indices) {
                    val document = documents[i]
                    val cartItem = cartItems[i]
                    if (document.exists()) {
                        val availableQuantity = document.getLong("availableQuantity")?.toInt() ?: 0
                        if (availableQuantity < cartItem.quantity) {
                            allItemsAvailable = false
                            Toast.makeText(this, "Недостатньо товару: ${cartItem.productName}", Toast.LENGTH_SHORT).show()
                            break
                        } else {
                            itemsToUpdate.add(Pair(cartItem.productId, cartItem.quantity))
                        }
                    } else {
                        allItemsAvailable = false
                        Toast.makeText(this, "Товар не знайдено: ${cartItem.productName}", Toast.LENGTH_SHORT).show()
                        break
                    }
                }

                if (allItemsAvailable && itemsToUpdate.size == cartItems.size) {
                    val orderItems = cartItems.map {
                        hashMapOf(
                            "productId" to UUID.randomUUID().toString(),
                            "productName" to it.productName,
                            "productType" to it.productType,
                            "productPrice" to it.productPrice,
                            "quantity" to it.quantity,
                            "productPhotoUrl" to it.productPhotoUrl
                        )
                    }

                    val order = hashMapOf(
                        "userId" to userId,
                        "name" to name,
                        "address" to address,
                        "phone" to phone,
                        "city" to city,
                        "postOffice" to selectedPostOffice,
                        "orderDate" to System.currentTimeMillis(),
                        "items" to orderItems,
                        "totalPrice" to cartItems.sumOf { it.productPrice * it.quantity },
                        "status" to "unconfirmed"
                    )

                    db.collection("orders").add(order)
                        .addOnSuccessListener {
                            db.runBatch { batch ->
                                for ((productId, quantity) in itemsToUpdate) {
                                    val docRef = db.collection("items").document(productId)
                                    batch.update(docRef, "availableQuantity", FieldValue.increment(-quantity.toLong()))
                                    batch.update(docRef, "name", FieldValue.serverTimestamp().let {
                                        val currentQuantity = documents.find { it.id == productId }?.getLong("availableQuantity")?.toInt() ?: 0
                                        if (currentQuantity - quantity <= 0) "Товар закінчився..." else FieldValue.delete()
                                    })
                                }
                            }.addOnSuccessListener {
                                cartManager.clearCart()
                                progressBar.visibility = View.GONE
                                Toast.makeText(this, "Замовлення оформлено!", Toast.LENGTH_SHORT).show()
                                finish()
                            }.addOnFailureListener { e ->
                                progressBar.visibility = View.GONE
                                Toast.makeText(this, "Помилка оновлення кількості: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "Помилка оформлення замовлення: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Помилка перевірки доступності: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun extractCityFromAddress(address: String): String {
        if (address.isEmpty()) return ""
        val parts = address.split(",").map { it.trim() }
        val cityPart = parts.find { part ->
            !part.contains("вул.", ignoreCase = true) &&
                    !part.contains("просп.", ignoreCase = true) &&
                    !part.matches(Regex(".*\\d+.*"))
        } ?: parts.lastOrNull() ?: ""
        return cityPart.replace(Regex("^(м\\.|с\\.)\\s*", RegexOption.IGNORE_CASE), "").trim()
    }

    private fun setEmptySpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.empty_post_offices,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        postOfficeSpinner.adapter = adapter
        postOfficeSpinner.isEnabled = false
    }

    private fun getCityRef(cityName: String, callback: (String?) -> Unit) {
        val requestBody = JSONObject().apply {
            put("apiKey", novaPoshtaApiKey)
            put("modelName", "Address")
            put("calledMethod", "searchSettlements")
            put("methodProperties", JSONObject().apply {
                put("CityName", cityName)
                put("Limit", 1)
            })
        }

        val request = JsonObjectRequest(
            Request.Method.POST, "https://api.novaposhta.ua/v2.0/json/", requestBody,
            { response ->
                try {
                    val dataArray = response.getJSONArray("data")
                    if (dataArray.length() > 0) {
                        val firstData = dataArray.getJSONObject(0)
                        val settlements = firstData.getJSONArray("Addresses")
                        if (settlements.length() > 0) {
                            val deliveryCity = settlements.getJSONObject(0).getString("DeliveryCity")
                            callback(deliveryCity)
                        } else {
                            callback(null)
                        }
                    } else {
                        callback(null)
                    }
                } catch (e: Exception) {
                    callback(null)
                }
            },
            { error ->
                callback(null)
            })

        requestQueue.add(request)
    }

    private fun getWarehouses(cityRef: String, callback: (List<String>) -> Unit) {
        val requestBody = JSONObject().apply {
            put("apiKey", novaPoshtaApiKey)
            put("modelName", "AddressGeneral")
            put("calledMethod", "getWarehouses")
            put("methodProperties", JSONObject().apply {
                put("CityRef", cityRef)
                put("Limit", 50)
            })
        }

        val request = JsonObjectRequest(
            Request.Method.POST, "https://api.novaposhta.ua/v2.0/json/", requestBody,
            { response ->
                try {
                    val data = response.getJSONArray("data")
                    val warehouses = mutableListOf<String>()
                    for (i in 0 until data.length()) {
                        val warehouse = data.getJSONObject(i).getString("Description")
                        warehouses.add(warehouse)
                    }
                    warehouses.sortBy { it.split("№").getOrNull(1)?.toIntOrNull() ?: 0 }
                    callback(warehouses)
                } catch (e: Exception) {
                    callback(emptyList())
                }
            },
            { error ->
                callback(emptyList())
            })

        requestQueue.add(request)
    }

    private fun loadWarehouses(city: String) {
        progressBar.visibility = View.VISIBLE
        getCityRef(city) { cityRef ->
            if (cityRef != null) {
                getWarehouses(cityRef) { warehouses ->
                    progressBar.visibility = View.GONE
                    if (warehouses.isNotEmpty()) {
                        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, warehouses)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        postOfficeSpinner.adapter = adapter
                        postOfficeSpinner.isEnabled = true
                    } else {
                        Toast.makeText(this, "Відділення не знайдено", Toast.LENGTH_SHORT).show()
                        setEmptySpinner()
                    }
                }
            } else {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Не знайдено місто", Toast.LENGTH_SHORT).show()
                setEmptySpinner()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requestQueue.cancelAll { true }
    }
}