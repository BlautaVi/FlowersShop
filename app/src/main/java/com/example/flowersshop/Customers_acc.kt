package com.example.flowersshop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Customers_acc : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customers_acc)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val deleteAcc_b = findViewById<Button>(R.id.deleteAcc_b)
        deleteAcc_b.setOnClickListener(){
            Toast.makeText(this,"Видаляємо ваш акаунт...", Toast.LENGTH_SHORT ).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

}