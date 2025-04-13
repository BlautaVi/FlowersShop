package com.example.flowersshop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val emailInput = findViewById<EditText>(R.id.enter_login2)
        val passwordInput = findViewById<EditText>(R.id.enter_passwd2)
        val loginBtn = findViewById<Button>(R.id.auth_b)
        val guestBtn = findViewById<Button>(R.id.guest_b)

        auth = FirebaseAuth.getInstance()

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Вхід успішний!", Toast.LENGTH_SHORT).show()
                            if (email == "manager@gmail.com") {
                                startActivity(Intent(this, manager_start_page::class.java))
                            } else {
                                startActivity(Intent(this, main_page::class.java))
                            }
                            finish()
                        } else {
                            val exception = task.exception
                            when {
                                exception?.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> {
                                    Toast.makeText(
                                        this,
                                        "Користувача не знайдено, перехід до реєстрації...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this, Registration::class.java)
                                    intent.putExtra("email", email)
                                    startActivity(intent)
                                }

                                exception?.message?.contains("INVALID_EMAIL") == true -> {
                                    Toast.makeText(
                                        this,
                                        "Некоректний формат email",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                else -> {
                                    Toast.makeText(
                                        this,
                                        "Помилка входу(перехід до реєстрації)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this, Registration::class.java)
                                    intent.putExtra("email", email)
                                    startActivity(intent)
                                }
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