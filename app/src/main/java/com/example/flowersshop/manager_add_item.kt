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

class manager_add_item : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var typeEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var itemImage: ImageView
    private lateinit var addButton: Button
    private lateinit var showAllButton: Button
    private lateinit var addPhotoButton: ImageButton
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null
    private lateinit var quantityEditText: EditText
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
        setContentView(R.layout.activity_manager_add_item)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val isManager = FirebaseAuth.getInstance().currentUser?.email == "manager@gmail.com"
        if (!isManager) {
            Toast.makeText(this, "Доступно лише для менеджера", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        nameEditText = findViewById(R.id.man_enter_name)
        typeEditText = findViewById(R.id.man_enter_type)
        priceEditText = findViewById(R.id.man_enter_price)
        descriptionEditText = findViewById(R.id.man_enter_decs)
        itemImage = findViewById(R.id.item_photo)
        addButton = findViewById(R.id.man_add_item_b)
        showAllButton = findViewById(R.id.man_show_all_items_b)
        addPhotoButton = findViewById(R.id.add_photo_btn)
        quantityEditText = findViewById(R.id.enter_quantity_c)
        addButton.setOnClickListener {
            saveProduct()
        }
        addPhotoButton.setOnClickListener {
            requestStoragePermission()
        }

        showAllButton.setOnClickListener {
            val intent = Intent(this, main_page::class.java)
            startActivity(intent)
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
        if (name.isEmpty() || type.isEmpty() || price == null || description.isEmpty()|| quantity <= 0) {
            Toast.makeText(this, "Заповніть усі поля", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val productId = UUID.randomUUID().toString()

        if (selectedImageUri != null) {
            val storageRef = storage.reference.child("items/$productId.jpg")
            storageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveProductToFirestore(productId, name, type, price, description, userId, uri.toString(), quantity)                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Помилка завантаження фото: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveProductToFirestore(productId, name, type, price, description, userId, "", quantity)
        }
    }

    private fun saveProductToFirestore(
        productId: String,
        name: String,
        type: String,
        price: Double,
        description: String,
        userId: String,
        photoUrl: String,
        quantity: Int
    ) {
        val product = hashMapOf(
            "id" to productId,
            "name" to name,
            "type" to type,
            "price" to price,
            "description" to description,
            "userId" to userId,
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