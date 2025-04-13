package com.example.flowersshop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class manager_start_page : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manager_start_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val add_item = findViewById<Button>(R.id.man_add_items_b)
        val see_items = findViewById<Button>(R.id.man_see_all_b)
        val see_orders = findViewById<Button>(R.id. man_all_orders_b)
        add_item.setOnClickListener {
            startActivity(Intent(this, manager_add_item::class.java))
        }
        see_items.setOnClickListener {
            startActivity(Intent(this, main_page::class.java))
        }
       // see_orders.setOnClickListener {
         //   startActivity(Intent(this, main_page::class.java))
           // finish()
        //}
    }
}