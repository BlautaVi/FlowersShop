package com.example.flowersshop

import android.content.Intent import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flowersshop.models.ProductAdapter
import com.example.flowersshop.models.ProductItem
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class customers_items_main_page : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<ProductItem>()
    private val allProducts = mutableListOf<ProductItem>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private val categories = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customers_items_main_page)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

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
            onAddToCartClick = { }
        )
        recyclerView.adapter = productAdapter

        findViewById<Button>(R.id.categoryButton).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val ordersButton = findViewById<Button>(R.id.orders_btn)
        ordersButton.setOnClickListener {
            val intent = Intent(this, CustomersOrdersForSalesActivity::class.java)
            startActivity(intent)
        }

        val toAcc = findViewById<ImageButton>(R.id.buttonA)
        toAcc.setOnClickListener {
            val intent = Intent(this, Customers_acc::class.java)
            startActivity(intent)
        }

        val addItem = findViewById<ImageButton>(R.id.button_Add)
        addItem.setOnClickListener {
            val intent = Intent(this, Customers_item_Add_page::class.java)
            startActivity(intent)
        }
        loadCategoriesAndProducts()
    }

    private fun loadCategoriesAndProducts() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("items")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                allProducts.clear()
                productList.clear()
                val categorySet = mutableSetOf<String>()

                for (document in result) {
                    try {
                        val product = document.toObject(ProductItem::class.java).copy(id = document.id)
                        allProducts.add(product)
                        productList.add(product)
                        categorySet.add(product.type)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Помилка: ${document.id}", Toast.LENGTH_SHORT).show()
                    }
                }
                categories.clear()
                categories.addAll(categorySet.sorted())
                val menu = navigationView.menu
                menu.clear()
                menu.add(0, R.id.nav_all, 0, "Усі категорії").setOnMenuItemClickListener {
                    filterProducts(null)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                categories.forEachIndexed { index, category ->
                    menu.add(0, index + 1, 0, category).setOnMenuItemClickListener {
                        filterProducts(category)
                        drawerLayout.closeDrawer(GravityCompat.START)
                        true
                    }
                }

                productAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterProducts(category: String?) {
        productList.clear()
        if (category == null) {
            productList.addAll(allProducts)
        } else {
            productList.addAll(allProducts.filter { it.type == category })
        }
        productAdapter.notifyDataSetChanged()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

}