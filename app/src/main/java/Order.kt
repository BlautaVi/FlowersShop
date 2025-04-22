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
)