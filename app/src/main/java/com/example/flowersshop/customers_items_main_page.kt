package com.example.flowersshop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flowersshop.models.ProductAdapter
import com.example.flowersshop.models.ProductItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class customers_items_main_page : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<ProductItem>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customers_items_main_page)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        productAdapter = ProductAdapter(
            productList,
            onItemClick = { product ->
                val intent = Intent(this, customer_edit_item::class.java).apply {
                    putExtra("product", product)
                }
                startActivity(intent)
            },
            onAddToCartClick = {  }
        )
        recyclerView.adapter = productAdapter

        loadUserProducts()

        val toAcc = findViewById<Button>(R.id.buttonA)
        toAcc.setOnClickListener {
            val intent = Intent(this, Customers_acc::class.java)
            startActivity(intent)
        }

        val addItem = findViewById<Button>(R.id.button_Add)
        addItem.setOnClickListener {
            val intent = Intent(this, Customers_item_Add_page::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserProducts() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("items")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                productList.clear()
                for (document in result) {
                    try {
                        val product = document.toObject(ProductItem::class.java).copy(id = document.id)
                        productList.add(product)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Помилка: ${document.id}", Toast.LENGTH_SHORT).show()
                    }
                }
                productAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}