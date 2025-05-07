package Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.flowersshop.CustomBarChartView
import com.google.firebase.firestore.FirebaseFirestore
import com.example.flowersshop.R

class manager_sales_analyst : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var chartView: CustomBarChartView
    private val db = FirebaseFirestore.getInstance()
    private val productSales = mutableMapOf<String, Float>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manager_sales_analyst)

        progressBar = findViewById(R.id.progressBar)
        chartView = findViewById(R.id.custom_bar_chart)
        var back_btn = findViewById<ImageButton>(R.id.back_btn)
        back_btn.setOnClickListener {
            val intent = Intent(this, ManagerStartPageActivity::class.java)
            finish()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadSalesData()
    }

    private fun loadSalesData() {
        progressBar.visibility = View.VISIBLE

        db.collection("orders")
            .get()
            .addOnSuccessListener { documents ->
                productSales.clear()

                for (document in documents) {
                    val items = document.get("items") as? List<Map<String, Any>> ?: continue
                    for (item in items) {
                        val productName = item["productName"] as? String ?: continue
                        val quantity = (item["quantity"] as? Long)?.toFloat() ?: (item["quantity"] as? Double)?.toFloat() ?: 0f
                        productSales[productName] = productSales.getOrDefault(productName, 0f) + quantity
                    }
                }

                if (productSales.isEmpty()) {
                    Toast.makeText(this, "Немає даних про продажі", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    return@addOnSuccessListener
                }

                displayChart()
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Помилка завантаження даних: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }

    private fun displayChart() {
        val topProducts = productSales.entries.sortedByDescending { it.value }.take(5)
        chartView.setData(topProducts)
        chartView.invalidate()
    }
}