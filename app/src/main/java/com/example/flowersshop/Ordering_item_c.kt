package com.example.flowersshop

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.util.UUID

class Ordering_item_c : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userId = auth.currentUser?.uid
    private lateinit var requestQueue: RequestQueue
    private val novaPoshtaApiKey = "51662651de0f1827a0501a581708f343"
    private lateinit var postOfficeSpinner: Spinner
    private lateinit var nameText: EditText
    private lateinit var addressText: EditText
    private lateinit var phoneText: EditText
    private lateinit var confirmOrderButton: Button
    private lateinit var cancelOrderButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var itemsListView: ListView
    private lateinit var totalPriceText: TextView
    private val cartItems = mutableListOf<CartItem>()
    private lateinit var cartAdapter: CartItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ordering_item_c)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        postOfficeSpinner = findViewById(R.id.spinner)
        nameText = findViewById(R.id.name_text)
        addressText = findViewById(R.id.address_text)
        phoneText = findViewById(R.id.phone_text)
        confirmOrderButton = findViewById(R.id.confirm_order_b)
        cancelOrderButton = findViewById(R.id.cancel_order_b)
        progressBar = findViewById(R.id.progressBar)
        itemsListView = findViewById(R.id.items_for_order_c_list)
        totalPriceText = findViewById(R.id.total_price_text)
        requestQueue = Volley.newRequestQueue(this)
        cartAdapter = CartItemsAdapter()
        itemsListView.adapter = cartAdapter

        if (userId != null) {
            loadUserData()
            loadCartItems()
        } else {
            Toast.makeText(this, "Користувач не автентифікований", Toast.LENGTH_SHORT).show()
            finish()
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
                Log.e("Ordering_item_c", "Помилка при завантаженні профілю: ${e.message}", e)
                Toast.makeText(this, "Помилка при завантаженні профілю: ${e.message}", Toast.LENGTH_SHORT).show()
                setEmptySpinner()
            }
    }

    private fun loadCartItems() {
        if (userId == null) return

        db.collection("cart")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                cartItems.clear()
                for (document in documents) {
                    val cartItem = CartItem(
                        id = document.id,
                        userId = document.getString("userId") ?: "",
                        productName = document.getString("productName") ?: "",
                        productType = document.getString("productType") ?: "",
                        productPrice = document.getDouble("productPrice") ?: 0.0,
                        productPhotoUrl = document.getString("productPhotoUrl") ?: "",
                        quantity = document.getLong("quantity")?.toInt() ?: 1
                    )
                    cartItems.add(cartItem)
                }
                cartAdapter.notifyDataSetChanged()
                updateTotalPrice()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Помилка завантаження товарів: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTotalPrice() {
        val total = cartItems.sumOf { it.productPrice * it.quantity }
        totalPriceText.text = "Загальна сума: $total грн"
    }

    private fun clearCart() {
        if (userId == null) return

        db.collection("cart")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    cartItems.clear()
                    cartAdapter.notifyDataSetChanged()
                    updateTotalPrice()
                    return@addOnSuccessListener
                }

                db.runBatch { batch ->
                    for (document in documents) {
                        batch.delete(document.reference)
                    }
                }.addOnSuccessListener {
                    cartItems.clear()
                    cartAdapter.notifyDataSetChanged()
                    updateTotalPrice()
                    Log.d("Ordering_item_c", "Кошик успішно очищено")
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Помилка очищення кошика: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Ordering_item_c", "Помилка очищення кошика: ${e.message}", e)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Помилка завантаження кошика: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Ordering_item_c", "Помилка завантаження кошика: ${e.message}", e)
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

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Заповніть усі поля", Toast.LENGTH_SHORT).show()
            return
        }

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Кошик порожній", Toast.LENGTH_SHORT).show()
            return
        }

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
            "postOffice" to selectedPostOffice,
            "orderDate" to System.currentTimeMillis(),
            "items" to orderItems,
            "totalPrice" to cartItems.sumOf { it.productPrice * it.quantity },
            "status" to "unconfirmed"
        )

        progressBar.visibility = View.VISIBLE
        db.collection("orders").add(order)
            .addOnSuccessListener {
                clearCart()
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Замовлення оформлено!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Помилка оформлення замовлення: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Ordering_item_c", "Помилка оформлення замовлення: ${e.message}", e)
            }
    }

    inner class CartItemsAdapter : BaseAdapter() {
        override fun getCount(): Int = cartItems.size

        override fun getItem(position: Int): Any = cartItems[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(this@Ordering_item_c)
                .inflate(R.layout.cart_item_simple_layout, parent, false)
            val cartItem = cartItems[position]

            val itemImage = view.findViewById<ImageView>(R.id.cart_item_image)
            val itemType = view.findViewById<TextView>(R.id.cart_item_type)
            val itemName = view.findViewById<TextView>(R.id.cart_item_name)
            val itemPrice = view.findViewById<TextView>(R.id.cart_item_price)
            val itemQuantity = view.findViewById<TextView>(R.id.cart_item_quantity)

            itemType.text = "Вид: ${cartItem.productType}"
            itemName.text = cartItem.productName
            itemPrice.text = "Ціна: ${cartItem.productPrice} грн"
            itemQuantity.text = "Кількість: ${cartItem.quantity}"

            Glide.with(this@Ordering_item_c)
                .load(cartItem.productPhotoUrl)
                .placeholder(R.drawable.icon)
                .into(itemImage)

            return view
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
                    Log.d("Ordering_item_c", "Відповідь API (cityRef): $response")
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
                    Log.e("Ordering_item_c", "Помилка парсингу cityRef: ${e.message}", e)
                    callback(null)
                }
            },
            { error ->
                Log.e("Ordering_item_c", "Помилка запиту cityRef: ${error.message}", error)
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
                    Log.d("Ordering_item_c", "Відповідь API (warehouses): $response")
                    val data = response.getJSONArray("data")
                    val warehouses = mutableListOf<String>()
                    for (i in 0 until data.length()) {
                        val warehouse = data.getJSONObject(i).getString("Description")
                        warehouses.add(warehouse)
                    }
                    warehouses.sortBy { it.split("№").getOrNull(1)?.toIntOrNull() ?: 0 }
                    callback(warehouses)
                } catch (e: Exception) {
                    Log.e("Ordering_item_c", "Помилка парсингу відділень: ${e.message}", e)
                    callback(emptyList())
                }
            },
            { error ->
                Log.e("Ordering_item_c", "Помилка запиту відділень: ${error.message}", error)
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