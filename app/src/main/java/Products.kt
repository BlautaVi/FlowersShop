package com.example.flowersshop.models

abstract class Products(
    open val name: String,
    open val type: String,
    open val price: Double,
    open val description: String,
    open val photoUrl: String,
    open val userId: String
) {
    constructor() : this("", "", 0.0, "", "", "")
    abstract fun isAvailable(): Boolean
    open fun getProductInfo(): String {
        return "Name: $name, Type: $type, Price: $price, Description: $description"
    }
    fun hasValidPrice(): Boolean = price > 0.0
}
