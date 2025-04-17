package com.example.flowersshop

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_item_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val product = intent.getParcelableExtra<ProductItem>("product")

        if (product == null) {
            Toast.makeText(this, "Товар не знайдено", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val itemImage = findViewById<ImageView>(R.id.items_image)
        val itemName = findViewById<TextView>(R.id.Name_l)
        val itemType = findViewById<TextView>(R.id.Type_l)
        val itemPrice = findViewById<TextView>(R.id.Price_l)
        val scrollView = findViewById<androidx.core.widget.NestedScrollView>(R.id.scrollView4)
        val itemDesc = scrollView.findViewById<TextView>(android.R.id.text1) ?: TextView(this).apply {
            id = android.R.id.text1
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            scrollView.findViewById<LinearLayout>(android.R.id.list).addView(this)
        }
        val addToCartBtn = findViewById<Button>(R.id.addToCart_b)

        itemName.text = product.name
        itemType.text = product.type
        itemPrice.text = "${product.price ?: 0.0} грн"
        itemDesc.text = product.description
        Glide.with(this)
            .load(product.photoUrl)
            .placeholder(R.drawable.icon)
            .into(itemImage)

        addToCartBtn.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(this, "Будь ласка, увійдіть у систему", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val cartItem = hashMapOf(
                "userId" to user.uid,
                "productName" to product.name,
                "productPrice" to product.price,
                "productPhotoUrl" to product.photoUrl
            )
            FirebaseFirestore.getInstance().collection("cart")
                .add(cartItem)
                .addOnSuccessListener {
                    Toast.makeText(this, "Додано в кошик", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Помилка: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}