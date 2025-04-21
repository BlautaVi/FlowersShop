package com.example.flowersshop

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Order(
    val id: String,
    val userId: String? = null,
    val orderDateMillis: Long,
    val totalPrice: Double,
    val items: List<Map<String, Any>>
)