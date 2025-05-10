package Activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import Adapters.ProductAdapter
import com.example.flowersshop.ProductManager
import com.example.flowersshop.ProductUpdate
import com.example.flowersshop.models.ProductItem
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.example.flowersshop.R
import com.google.firebase.firestore.FirebaseFirestore

class MainPageActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<ProductItem>()
    private val allProducts = mutableListOf<ProductItem>()
    private var isManager = false
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private val categories = mutableListOf<String>()
    private var isListView = false
    private val productManager = ProductManager(FirebaseFirestore.getInstance())

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

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        productAdapter = ProductAdapter(
            productList,
            onItemClick = { product ->
                if (isManager) {
                    val intent = Intent(this, ManagerEditItemActivity::class.java).apply {
                        putExtra("product", product)
                    }
                    startActivity(intent)
                } else if (product.userId == currentUserId) {
                    val intent = Intent(this, CustomerEditItemActivity::class.java).apply {
                        putExtra("product", product)
                    }
                    startActivity(intent)
                } else {
                    val intent = Intent(this, ItemPageActivity::class.java).apply {
                        putExtra("productId", product.id)
                    }
                    startActivity(intent)
                }
            },
            onAddToCartClick = { product ->
                if (!isManager && product.userId != currentUserId) {
                    addToCart(product)
                }
            }
        )
        recyclerView.adapter = productAdapter

        findViewById<ImageButton>(R.id.categoryButton).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        findViewById<ImageButton>(R.id.toggleViewButton).setOnClickListener {
            isListView = !isListView
            productAdapter.toggleViewType()
        }

        val accBtn = findViewById<ImageButton>(R.id.buttonA)
        val orderBtn = findViewById<ImageButton>(R.id.button_Add)

        if (isManager) {
            accBtn.visibility = View.GONE
            orderBtn.visibility = View.GONE
        } else {
            accBtn.visibility = View.VISIBLE
            orderBtn.visibility = View.VISIBLE
            accBtn.setOnClickListener { startActivity(Intent(this, CustomerAccActivity::class.java)) }
            orderBtn.setOnClickListener { startActivity(Intent(this, OrderingItemCActivity::class.java)) }
        }

        loadCategoriesAndProducts()
    }

    private fun loadCategoriesAndProducts() {
        productManager.loadAllProducts { products ->
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

    private fun addToCart(product: ProductItem) {
        val userId = currentUserId ?: return
        val db = FirebaseFirestore.getInstance()
        ProductUpdate(
            product.name,
            product.type,
            product.price,
            product.description,
            product.availableQuantity
        ).updateInFirestore(db, product.id) { success ->
            if (success) {
                db.collection("cart")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("productId", product.id)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            val cartItem = hashMapOf(
                                "userId" to userId,
                                "productId" to product.id,
                                "productName" to product.name,
                                "productType" to product.type,
                                "productPrice" to product.price,
                                "productPhotoUrl" to product.photoUrl,
                                "quantity" to 1
                            )
                            db.collection("cart").add(cartItem).addOnSuccessListener {
                                Toast.makeText(this, "${product.name} додано до кошика", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                Toast.makeText(this, "Помилка додавання до кошика", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val doc = documents.documents.first()
                            val currentQuantity = doc.getLong("quantity")?.toInt() ?: 1
                            db.collection("cart").document(doc.id).update("quantity", currentQuantity + 1)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "${product.name} додано до кошика", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Помилка оновлення кількості", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
            }
        }
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