package com.example.flowersshop

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flowersshop.models.ProductAdapter
import com.example.flowersshop.models.ProductItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class main_page : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<ProductItem>()
    private var isManager = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        isManager = currentUser?.email == "manager@gmail.com"

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        productAdapter = ProductAdapter(productList, isManager)
        recyclerView.adapter = productAdapter

        loadProductsFromFirebase()

        val accBtn = findViewById<Button>(R.id.buttonA)
        val orderBtn = findViewById<Button>(R.id.buttonK)

        if (isManager) {
            accBtn.visibility = View.GONE
            orderBtn.visibility = View.GONE
        } else {
            accBtn.visibility = View.VISIBLE
            orderBtn.visibility = View.VISIBLE
            accBtn.setOnClickListener {
                val intent = Intent(this, Customers_acc::class.java)
                startActivity(intent)
            }
            orderBtn.setOnClickListener {
                val intent = Intent(this, Ordering_item_c::class.java)
                startActivity(intent)
            }
        }
    }

    private fun loadProductsFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("items")
            .get()
            .addOnSuccessListener { result ->
                productList.clear()
                for (document in result) {
                    try {
                        val product = document.toObject(ProductItem::class.java).copy(id = document.id)
                        productList.add(product)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Помилка десеріалізації документа: ${document.id}", Toast.LENGTH_SHORT).show()
                    }
                }
                productAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}