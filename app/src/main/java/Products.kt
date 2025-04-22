package com.example.flowersshop.models

abstract class Products(
    open val name: String,
    open val type: String,
    open val price: Double,
    open val description: String,
    open val photoUrl: String,
    open val userId: String
) {
    abstract fun isAvailable(): Boolean
}