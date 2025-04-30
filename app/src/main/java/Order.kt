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
){
    constructor() : this("", null, 0L, 0.0, emptyList(), "unconfirmed")
    fun getFormattedOrderDate(): String {
        val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date(orderDateMillis))
    }
}