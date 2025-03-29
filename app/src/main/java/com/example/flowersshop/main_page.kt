package com.example.flowersshop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class main_page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val acc_b = findViewById<Button>(R.id.buttonA)
        val order_b = findViewById<Button>(R.id.buttonK)
        acc_b.setOnClickListener(){
            val intent = Intent(this, Customers_acc::class.java)
            startActivity(intent)
        }
        order_b.setOnClickListener(){
            val intent = Intent(this, Ordering_item_c::class.java)
            startActivity(intent)
        }
    }
}

