package com.example.flowersshop

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class Customers_acc : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var nameField: EditText
    private lateinit var addressField: EditText
    private lateinit var phoneField: EditText
    private lateinit var ordersRecyclerView: RecyclerView
    private val ordersList = mutableListOf<Order>()
    private val PHONE_PATTERN = Pattern.compile("^\\+380\\d{9}$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customers_acc)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        nameField = findViewById(R.id.name_field)
        addressField = findViewById(R.id.adress_field)
        phoneField = findViewById(R.id.phone_lab)
        ordersRecyclerView = findViewById(R.id.orders_c_view)
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        ordersRecyclerView.setHasFixedSize(true)
        val backBtn = findViewById<ImageButton>(R.id.back_b_confirmed)
        backBtn.setOnClickListener() {
            finish()
        }
        if (auth.currentUser?.email == "manager@gmail.com") {
            Toast.makeText(this, "Менеджерський акаунт не має профілю", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, manager_start_page::class.java))
            finish()
            return
        }

        val ownItems = findViewById<Button>(R.id.CustomerItems_b)
        ownItems.setOnClickListener {
            val intent = Intent(this, customers_items_main_page::class.java)
            startActivity(intent)
        }

        val ordersButton = findViewById<Button>(R.id.your_orders_button)
        ordersButton.setOnClickListener {
            val intent = Intent(this, CustomersOrdersActivity::class.java)
            startActivity(intent)
        }
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        nameField.setText(document.getString("name") ?: "")
                        addressField.setText(document.getString("address") ?: "")
                        phoneField.setText(document.getString("phoneNumber") ?: "")
                    } else {
                        Toast.makeText(this, "Дані користувача не знайдено", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Помилка при завантаженні профілю", Toast.LENGTH_SHORT).show()
                }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val changeButton = findViewById<ImageButton>(R.id.change_b)
        changeButton.setOnClickListener {
            val newName = nameField.text.toString().trim()
            val newAddress = addressField.text.toString().trim()
            val newPhone = phoneField.text.toString().trim()

            if (newName.isEmpty() || newAddress.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(this, "Заповніть усі поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!PHONE_PATTERN.matcher(newPhone).matches()) {
                Toast.makeText(this, "Номер телефону має бути у форматі +380xxxxxxxxx", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId != null) {
                val updatedUser = hashMapOf(
                    "name" to newName,
                    "address" to newAddress,
                    "phoneNumber" to newPhone,
                    "email" to auth.currentUser?.email
                )
                db.collection("users").document(userId)
                    .set(updatedUser)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Дані успішно оновлено", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Помилка при оновленні даних: ${e.message}", e)
                        Toast.makeText(this, "Помилка оновлення даних: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Помилка: користувач не автентифікований", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        val exitAccButton = findViewById<ImageButton>(R.id.exitAcc_b)
        exitAccButton.setOnClickListener {
            auth.signOut()
            if (auth.currentUser == null) {
                Toast.makeText(this, "Ви успішно вийшли з акаунта", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Помилка при виході", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val deleteAccButton = findViewById<ImageButton>(R.id.deleteAcc_b)
        deleteAccButton.setOnClickListener {
            Toast.makeText(this, "Видаляємо ваш акаунт...", Toast.LENGTH_SHORT).show()
            val user = auth.currentUser
            val userId = user?.uid

            if (userId != null) {
                db.collection("users").document(userId)
                    .delete()
                    .addOnSuccessListener {
                        Log.d("Firestore", "Документ користувача $userId видалено з Firestore")
                        user.delete()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Акаунт успішно видалено", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Помилка видалення акаунта: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Помилка при видаленні даних: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Помилка: користувач не автентифікований", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}