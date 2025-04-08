package com.example.flowersshop

import android.content.Intent
import android.os.Bundle
import android.view.View
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
        val passwordInput = findViewById<EditText>(R.id.enter_passwd)
        val loginBtn = findViewById<Button>(R.id.auth_b)
        val guestBtn = findViewById<Button>(R.id.guest_b)

        auth = FirebaseAuth.getInstance()

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val signInMethods = task.result?.signInMethods
                            if (signInMethods.isNullOrEmpty()) {
                                Toast.makeText(this, "Користувача не знайдено, перехід до реєстрації...", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, Registration::class.java)
                                intent.putExtra("email", email)
                                startActivity(intent)
                            } else {
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(this) { loginTask ->
                                        if (loginTask.isSuccessful) {
                                            Toast.makeText(this, "Вхід успішний!", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, main_page::class.java))
                                            finish()
                                        } else {
                                            Toast.makeText(this, "Помилка входу: ${loginTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Помилка перевірки: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Заповніть всі поля", Toast.LENGTH_SHORT).show()
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
