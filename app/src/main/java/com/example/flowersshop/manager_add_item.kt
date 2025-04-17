package com.example.flowersshop

import android.content.Intent
import android.net.Uri
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
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class manager_add_item : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageUri: Uri
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manager_add_item)

        imageView = findViewById(R.id.item_photo)

        val addPhotoBtn = findViewById<Button>(R.id.add_photo_btn)
        val addItemBtn = findViewById<Button>(R.id.man_add_item_b)
        val showAllBtn = findViewById<Button>(R.id.man_show_all_items_b)
        addPhotoBtn.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Виберіть зображення"), PICK_IMAGE_REQUEST)
        }

        addItemBtn.setOnClickListener {
            if (::imageUri.isInitialized) {
                uploadImageAndSaveData()
            } else {
                Toast.makeText(this, "Спочатку оберіть фото", Toast.LENGTH_SHORT).show()
            }
        }
        showAllBtn.setOnClickListener{
            val intent = Intent(this, main_page::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!
            imageView.setImageURI(imageUri)
        }
    }

    private fun uploadImageAndSaveData() {
        val fileName = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("items/$fileName.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveItemData(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Помилка при завантаженні фото", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveItemData(photoUrl: String) {
        val name = findViewById<EditText>(R.id.man_enter_name).text.toString()
        val type = findViewById<EditText>(R.id.man_enter_type).text.toString()
        val price = findViewById<EditText>(R.id.man_enter_price).text.toString()
        val desc = findViewById<EditText>(R.id.man_enter_decs).text.toString()
        val uid = auth.currentUser?.uid ?: "невідомо"

        val item = hashMapOf(
            "name" to name,
            "type" to type,
            "price" to price.toDoubleOrNull(),
            "description" to desc,
            "photoUrl" to photoUrl,
            "userId" to uid
        )

        firestore.collection("items")
            .add(item)
            .addOnSuccessListener {
                Toast.makeText(this, "Товар додано", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Помилка при збереженні", Toast.LENGTH_SHORT).show()
            }
    }
}
