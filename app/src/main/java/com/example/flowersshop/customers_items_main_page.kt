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


class customers_items_main_page : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<ProductItem>()
    private var isManager = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customers_items_main_page)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        productAdapter = ProductAdapter(productList, isManager = false)
        recyclerView.adapter = productAdapter

        loadUserProducts()
        val toAcc = findViewById<Button>(R.id.buttonA)
        toAcc.setOnClickListener(){
            val intent = Intent(this, Customers_acc::class.java)
            startActivity(intent)
        }
        val Add_item = findViewById<Button>(R.id.button_Add)
        Add_item.setOnClickListener(){
            val intent = Intent(this,manager_add_item::class.java)
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
