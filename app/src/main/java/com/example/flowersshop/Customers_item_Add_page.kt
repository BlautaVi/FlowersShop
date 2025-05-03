package com.example.flowersshop

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class Customers_item_Add_page : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var typeEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var itemImage: ImageView
    private lateinit var addButton: Button
    private lateinit var addPhotoButton: ImageButton
    private lateinit var showAllButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var accountButton: ImageButton
    private lateinit var quantityEditText: EditText
    private var selectedImageUri: Uri? = null
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(this, "Дозвіл на доступ до галереї не надано", Toast.LENGTH_SHORT).show()
        }
    }
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .into(itemImage)
        }
    }

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
        storage = FirebaseStorage.getInstance()

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
        accountButton = findViewById(R.id.button2)
        quantityEditText = findViewById(R.id.enter_quantity_c)
        accountButton.setOnClickListener {
            val intent = Intent(this, Customers_acc::class.java)
            startActivity(intent)
        }

        addButton.setOnClickListener {
            saveProduct()
        }

        addPhotoButton.setOnClickListener {
            requestStoragePermission()
        }

        showAllButton.setOnClickListener {
            startActivity(Intent(this, main_page::class.java))
        }
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        requestPermissionLauncher.launch(permission)
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun saveProduct() {
        val name = nameEditText.text.toString().trim()
        val type = typeEditText.text.toString().trim()
        val price = priceEditText.text.toString().toDoubleOrNull()
        val description = descriptionEditText.text.toString().trim()
        val quantity = quantityEditText.text.toString().toIntOrNull() ?: 0

        if (name.isEmpty() || type.isEmpty() || price == null || description.isEmpty() || quantity <= 0) {
            Toast.makeText(this, "Заповніть усі поля коректно, кількість має бути більше 0", Toast.LENGTH_SHORT).show()
            return
        }

        val productId = UUID.randomUUID().toString()

        if (selectedImageUri != null) {
            val storageRef = storage.reference.child("items/$productId.jpg")
            storageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveProductToFirestore(productId, name, type, price, description, uri.toString(), quantity)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Помилка завантаження фото: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveProductToFirestore(productId, name, type, price, description, "", quantity)
        }
    }

    private fun saveProductToFirestore(
        productId: String,
        name: String,
        type: String,
        price: Double,
        description: String,
        photoUrl: String,
        quantity: Int
    ) {
        val product = hashMapOf(
            "id" to productId,
            "name" to name,
            "type" to type,
            "price" to price,
            "description" to description,
            "userId" to auth.currentUser!!.uid,
            "photoUrl" to photoUrl,
            "availableQuantity" to quantity
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