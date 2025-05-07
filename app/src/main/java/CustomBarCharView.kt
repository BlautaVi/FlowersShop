package com.example.flowersshop
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

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
    private val colors = listOf(Color.BLUE)
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
