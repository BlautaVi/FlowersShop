package com.example.flowersshop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class manager_add_item : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var typeEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var itemImage: ImageView
    private lateinit var addButton: Button
    private lateinit var showAllButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manager_add_item)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nameEditText = findViewById(R.id.man_enter_name)
        typeEditText = findViewById(R.id.man_enter_type)
        priceEditText = findViewById(R.id.man_enter_price)
        descriptionEditText = findViewById(R.id.man_enter_decs)
        itemImage = findViewById(R.id.item_photo)
        addButton = findViewById(R.id.man_add_item_b)
        showAllButton = findViewById(R.id.man_show_all_items_b)

        val isManager = FirebaseAuth.getInstance().currentUser?.email == "manager@gmail.com"
        if (!isManager) {
            Toast.makeText(this, "Доступно лише для менеджера", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        addButton.setOnClickListener {
            saveProduct()
        }

        showAllButton.setOnClickListener {
            val intent = Intent(this, main_page::class.java)
            startActivity(intent)
        }
    }

    private fun saveProduct() {
        val name = nameEditText.text.toString().trim()
        val type = typeEditText.text.toString().trim()
        val price = priceEditText.text.toString().toDoubleOrNull()
        val description = descriptionEditText.text.toString().trim()

        if (name.isEmpty() || type.isEmpty() || price == null || description.isEmpty()) {
            Toast.makeText(this, "Заповніть усі поля", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val product = hashMapOf(
            "name" to name,
            "type" to type,
            "price" to price,
            "description" to description,
            "userId" to userId,
            "photoUrl" to ""
        )

        db.collection("items")
            .add(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Товар додано", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}