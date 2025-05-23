package Activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import Adapters.ProductAdapter
import com.example.flowersshop.ProductManager
import com.example.flowersshop.models.ProductItem
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.example.flowersshop.R
import com.google.firebase.firestore.FirebaseFirestore

class CustomersItemsMainPageActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<ProductItem>()
    private val allProducts = mutableListOf<ProductItem>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private val categories = mutableListOf<String>()
    private var isListView = false
    private val productManager = ProductManager(FirebaseFirestore.getInstance())

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
                val intent = Intent(this, CustomerEditItemActivity::class.java).apply {
                    putExtra("product", product)
                }
                startActivity(intent)
            },
            onAddToCartClick = { }
        )
        recyclerView.adapter = productAdapter

        findViewById<ImageButton>(R.id.categoryButton).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        findViewById<ImageButton>(R.id.toggleViewButton).setOnClickListener {
            isListView = !isListView
            productAdapter.toggleViewType()
        }

        val ordersButton = findViewById<ImageButton>(R.id.orders_btn)
        ordersButton.setOnClickListener {
            startActivity(Intent(this, CustomersOrdersForSalesActivity::class.java))
        }

        val toAcc = findViewById<ImageButton>(R.id.buttonA)
        toAcc.setOnClickListener {
            startActivity(Intent(this, CustomerAccActivity::class.java))
        }

        val addItem = findViewById<ImageButton>(R.id.button_Add)
        addItem.setOnClickListener {
            startActivity(Intent(this, CustomersItemAddPageActivity::class.java))
        }

        loadCategoriesAndProducts()
    }

    private fun loadCategoriesAndProducts() {
        val userId = currentUserId ?: return

        productManager.loadUserProducts(userId) { products ->
            allProducts.clear()
            productList.clear()
            val categorySet = mutableSetOf<String>()

            allProducts.addAll(products)
            productList.addAll(products)
            products.forEach { categorySet.add(it.type) }

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