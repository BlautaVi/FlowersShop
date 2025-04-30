package com.example.flowersshop

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.firestore
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        val db = Firebase.firestore
        val emailInput = findViewById<EditText>(R.id.enter_login2)
        val passwordInput = findViewById<EditText>(R.id.enter_passwd2)
        val loginBtn = findViewById<Button>(R.id.auth_b)
        val guestBtn = findViewById<Button>(R.id.guest_b)

        auth = FirebaseAuth.getInstance()

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заповніть усі поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Вхід успішний!", Toast.LENGTH_SHORT).show()
                        val intent = if (email == "manager@gmail.com") {
                            Intent(this, manager_start_page::class.java)
                        } else {
                            Intent(this, main_page::class.java)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        val exception = task.exception
                        Log.d("Exception", "exception ${exception}")
                        when {
                            email.isBlank() -> {
                                Toast.makeText(
                                    this,
                                    "Поле email не може бути порожнім",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            password.isBlank() -> {
                                Toast.makeText(
                                    this,
                                    "Поле пароля не може бути порожнім",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }exception is FirebaseAuthInvalidCredentialsException -> {
                            Firebase.firestore.collection("users")
                                .whereEqualTo("email", email)
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (documents.isEmpty) {
                                        Toast.makeText(this, "Користувача з таким email не знайдено", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, Registration::class.java)
                                        intent.putExtra("email", email)
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(this, "Невірний пароль", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Помилка при перевірці користувача", Toast.LENGTH_SHORT).show()
                                }
                        }

                            exception?.cause is UnknownHostException || exception?.cause is TimeoutException -> {
                                Toast.makeText(
                                    this,
                                    "Помилка мережі. Перевірте підключення до інтернету",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            else -> {
                                Toast.makeText(
                                    this,
                                    "Помилка входу: ${exception?.localizedMessage ?: "Невідома помилка"}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
        }
        guestBtn.setOnClickListener {
            startActivity(Intent(this, for_guest::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

}