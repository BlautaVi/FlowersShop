package com.example.flowersshop

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.flowersshop.models.ProductItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class customer_edit_item : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var product: ProductItem
    private lateinit var quantityEditText: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_edit_item)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        product = intent.getParcelableExtra("product") ?: run {
            Toast.makeText(this, "Товар не знайдено", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        quantityEditText = findViewById(R.id.customer_enter_quantity)
        quantityEditText.setText(product.availableQuantity.toString())

        val currentUser = auth.currentUser
        if (currentUser == null || currentUser.uid != product.userId) {
            Toast.makeText(this, "Ви не можете редагувати цей товар", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val itemPhoto = findViewById<ImageView>(R.id.item_photo)
        val enterName = findViewById<EditText>(R.id.customer_enter_name)
        val enterType = findViewById<EditText>(R.id.customer_enter_type)
        val enterPrice = findViewById<EditText>(R.id.customer_enter_price)
        val enterDesc = findViewById<EditText>(R.id.customer_enter_desc)
        val updateButton = findViewById<Button>(R.id.customer_update_b)
        val deleteButton = findViewById<Button>(R.id.customer_delete_b)
        val backBtn = findViewById<ImageButton>(R.id.back_b_confirmed)
        backBtn.setOnClickListener() {
            finish()
        }
        Glide.with(this)
            .load(product.photoUrl)
            .placeholder(R.drawable.icon)
            .into(itemPhoto)
        enterName.setText(product.name)
        enterType.setText(product.type)
        enterPrice.setText(product.price?.toString() ?: "0.0")
        enterDesc.setText(product.description)

        updateButton.setOnClickListener {
            val updatedName = enterName.text.toString().trim()
            val updatedType = enterType.text.toString().trim()
            val updatedPrice = enterPrice.text.toString().toDoubleOrNull()
            val updatedDesc = enterDesc.text.toString().trim()
            val updatedQuantity = quantityEditText.text.toString().toIntOrNull() ?: 0
            if (updatedName.isEmpty() || updatedType.isEmpty() || updatedPrice == null || updatedDesc.isEmpty() || updatedQuantity < 0) {
                Toast.makeText(this, "Заповніть усі поля коректно, кількість не може бути від’ємною", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedProduct = hashMapOf(
                "name" to updatedName,
                "type" to updatedType,
                "price" to updatedPrice,
                "description" to updatedDesc,
                "photoUrl" to product.photoUrl,
                "userId" to product.userId,
                "availableQuantity" to updatedQuantity
            )

            db.collection("items").document(product.id)
                .update(updatedProduct as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Товар оновлено", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        deleteButton.setOnClickListener {
            db.collection("items").document(product.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Товар видалено", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}