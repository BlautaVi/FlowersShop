package com.example.flowersshop

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.graphics.toColorInt

class CustomBarChartView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val colors = listOf(
        "#1E88E5".toColorInt(),
        "#43A047".toColorInt(),
        "#FB8C00".toColorInt(),
        "#8E24AA".toColorInt(),
        "#F4511E".toColorInt()
    )

    private val barPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        setShadowLayer(8f, 0f, 4f, Color.GRAY)
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.DKGRAY
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics
        )
        textAlign = Paint.Align.CENTER
    }

    private var data: List<Map.Entry<String, Float>> = emptyList()

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

            val wrappedText = wrapText(entry.key, barWidth * 1.5f, textPaint)
            val textHeight = wrappedText.size * textPaint.textSize * 1.2f
            val labelY = height - textHeight / 2

            wrappedText.forEachIndexed { lineIndex, line ->
                canvas.drawText(
                    line,
                    left + barWidth / 2,
                    labelY + (lineIndex * textPaint.textSize * 1.2f),
                    textPaint
                )
            }

            canvas.drawText(entry.value.toInt().toString(), left + barWidth / 2, top - 20f, textPaint)
        }
    }
    private fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (char in text) {
            val testLine = currentLine + char
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = char.toString()
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }

}
