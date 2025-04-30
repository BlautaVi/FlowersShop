package com.example.flowersshop

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

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
            val intent = Intent(this, manager_start_page::class.java)
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

class CustomBarChartView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val barPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }
    private var data: List<Map.Entry<String, Float>> = emptyList()
    private val colors = listOf(
       Color.BLUE
    )

    fun setData(newData: List<Map.Entry<String, Float>>) {
        data = newData
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) return

        val barWidth = width / (data.size * 2f)
        val maxValue = data.maxOf { it.value }
        val scale = (height - 200f) / maxValue

        data.forEachIndexed { index, entry ->
            barPaint.color = colors[index % colors.size]

            val left = index * 2 * barWidth + barWidth / 2
            val barHeight = entry.value * scale
            val top = height - barHeight - 100f
            val right = left + barWidth
            val bottom = height - 100f

            canvas.drawRect(left, top, right, bottom, barPaint)

            canvas.save()
            canvas.rotate(-45f, left + barWidth / 2, height - 50f)
            canvas.drawText(entry.key.take(10), left + barWidth / 2, height - 50f, textPaint)
            canvas.restore()

            canvas.drawText(entry.value.toInt().toString(), left + barWidth / 2, top - 20f, textPaint)
        }
    }
}