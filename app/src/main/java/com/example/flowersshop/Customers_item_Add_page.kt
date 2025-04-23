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
import java.util.UUID

class Customers_item_Add_page : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var typeEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var itemImage: ImageView
    private lateinit var addButton: Button
    private lateinit var addPhotoButton: Button
    private lateinit var showAllButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customers_item_add_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser == null) {
            Toast.makeText(this, "Увійдіть в акаунт", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (auth.currentUser?.email == "manager@gmail.com") {
            Toast.makeText(this, "Менеджер не може додавати товари тут", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        nameEditText = findViewById(R.id.enter_name_c)
        typeEditText = findViewById(R.id.enter_type_c)
        priceEditText = findViewById(R.id.enter_price_c)
        descriptionEditText = findViewById(R.id.editTextTextMultiLine)
        itemImage = findViewById(R.id.cust_img)
        addButton = findViewById(R.id.add_cust_item_b)
        addPhotoButton = findViewById(R.id.add_items_photo_C)
        showAllButton = findViewById(R.id.All_cust_items_b)
        val AccountButton = findViewById<Button>(R.id.button2)
        AccountButton.setOnClickListener{
            val intent = Intent(this, Customers_acc::class.java)
        }
        addButton.setOnClickListener {
            saveProduct()
        }

        addPhotoButton.setOnClickListener {
            Toast.makeText(this, "Функція завантаження фото ще не реалізована", Toast.LENGTH_SHORT).show()
        }

        showAllButton.setOnClickListener {
            startActivity(Intent(this, main_page::class.java))
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

        val productId = UUID.randomUUID().toString()
        val product = hashMapOf(
            "id" to productId,
            "name" to name,
            "type" to type,
            "price" to price,
            "description" to description,
            "userId" to auth.currentUser!!.uid,
            "photoUrl" to ""
        )

        db.collection("items")
            .document(productId)
            .set(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Товар додано", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}