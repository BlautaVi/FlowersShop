package Activity

import Activity.MainActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.flowersshop.R
import com.example.flowersshop.models.ProductItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ItemPageActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_item_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val backBtn = findViewById<ImageButton>(R.id.back_b_confirmed)
        backBtn.setOnClickListener {
            finish()
        }
        val productId = intent.getStringExtra("productId") ?: return finish()
        loadProductDetails(productId)
    }

    private fun loadProductDetails(productId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val document = db.collection("items").document(productId)
                    .get()
                    .await()
                runOnUiThread {
                    if (document.exists()) {
                        val product = document.toObject(ProductItem::class.java)?.copy(id = document.id)
                        if (product != null) {
                            setupProductView(product)
                            findViewById<Button>(R.id.addToCart_b)?.setOnClickListener {
                                addToCart(product)
                            }
                        } else {
                            Toast.makeText(this@ItemPageActivity, "Помилка завантаження товару", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        Toast.makeText(this@ItemPageActivity, "Товар не знайдено", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ItemPageActivity, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun setupProductView(product: ProductItem) {
        val imageView = findViewById<ImageView>(R.id.items_image)
        val nameText = findViewById<TextView>(R.id.Name_l)
        val typeText = findViewById<TextView>(R.id.Type_l)
        val priceText = findViewById<TextView>(R.id.Price_l)
        val quantityText = findViewById<TextView>(R.id.Quantity_l)
        val descText = findViewById<TextView>(R.id.description_text)

        nameText.text = product.name
        typeText.text = product.type
        priceText.text = "${product.price} грн"
        quantityText.text = "Наявність: ${product.availableQuantity}"
        descText.text = product.description

        if (product.photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(product.photoUrl)
                .thumbnail(0.25f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imageView)
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun addToCart(product: ProductItem) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Увійдіть в акаунт", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        if (product.price <= 0) {
            Toast.makeText(this, "Товар не має ціни", Toast.LENGTH_SHORT).show()
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existingItem = db.collection("cart")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("productId", product.id)
                    .get()
                    .await()

                if (!existingItem.isEmpty) {
                    val doc = existingItem.documents[0]
                    val currentQuantity = doc.getLong("quantity")?.toInt() ?: 1
                    db.collection("cart").document(doc.id)
                        .update("quantity", currentQuantity + 1)
                        .await()
                } else {
                    val cartItem = hashMapOf(
                        "userId" to userId,
                        "productId" to product.id,
                        "productName" to product.name,
                        "productType" to product.type,
                        "productPrice" to product.price,
                        "productPhotoUrl" to product.photoUrl,
                        "quantity" to 1
                    )
                    Log.d("Item_page", "Creating new cart item: $cartItem")
                    if (userId != cartItem["userId"]) {
                        Log.e("Item_page", "Mismatch: auth.currentUser.uid=$userId, cartItem.userId=${cartItem["userId"]}")
                    }
                    db.collection("cart").add(cartItem).await()
                }

                runOnUiThread {
                    Toast.makeText(this@ItemPageActivity, "Додано до кошика", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ItemPageActivity, "Помилка додавання до кошика: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}