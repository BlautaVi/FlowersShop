package com.example.flowersshop

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Order(
    val id: String = "",
    val userId: String? = null,
    val orderDateMillis: Long = 0L,
    val totalPrice: Double = 0.0,
    val items: List<Map<String, Any>> = emptyList(),
val status: String = "unconfirmed"
) {
    constructor() : this("", null, 0L, 0.0, emptyList(), "unconfirmed")
    fun getFormattedOrderDate(): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(orderDateMillis))
    }

    fun isUnconfirmed(): Boolean = status == "unconfirmed"

    fun getItemsSummary(): String {
        val builder = StringBuilder()
        builder.append("Товари:\n")
        for (item in items) {
            val productName = item["productName"] as? String ?: "Невідомий товар"
            val productType = item["productType"] as? String ?: "Невідомий тип"
            val productPrice = item["productPrice"] as? Double ?: 0.0
            val quantity = (item["quantity"] as? Long)?.toInt() ?: 1
            builder.append("- $productName ($productType): $productPrice грн x $quantity\n")
        }
        return builder.toString()
    }
}
