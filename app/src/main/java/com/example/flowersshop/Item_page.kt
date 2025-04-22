package com.example.flowersshop

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.flowersshop.models.ProductItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Item_page : AppCompatActivity() {
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

        val productId = intent.getStringExtra("productId") ?: return finish()
        loadProductDetails(productId)

        val addToCartButton = findViewById<Button>(R.id.addToCart_b)
        addToCartButton.setOnClickListener {
        }
    }

    private fun loadProductDetails(productId: String) {
        db.collection("items").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val product = document.toObject(ProductItem::class.java)?.copy(id = document.id)
                    if (product != null) {
                        setupProductView(product)
                        findViewById<Button>(R.id.addToCart_b)?.setOnClickListener {
                            addToCart(product)
                        }
                    } else {
                        Toast.makeText(this, "Помилка завантаження товару", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Товар не знайдено", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun setupProductView(product: ProductItem) {
        val imageView = findViewById<ImageView>(R.id.items_image)
        val nameText = findViewById<TextView>(R.id.Name_l)
        val typeText = findViewById<TextView>(R.id.Type_l)
        val priceText = findViewById<TextView>(R.id.Price_l)
        val descText = findViewById<TextView>(R.id.description_text)

        nameText.text = product.name
        typeText.text = product.type
        priceText.text = "${product.price} грн"
        descText.text = product.description

        if (product.photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(product.photoUrl)
                .into(imageView)
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun addToCart(product: ProductItem) {
        val userId = auth.currentUser?.uid ?: return
        val cartItem = hashMapOf(
            "userId" to userId,
            "productId" to product.id,
            "productName" to product.name,
            "productPrice" to product.price,
            "quantity" to 1
        )

        db.collection("cart")
            .add(cartItem)
            .addOnSuccessListener {
                Toast.makeText(this, "Додано до кошика", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Помилка додавання до кошика: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}