package com.example.flowersshop.models

import android.os.Parcelable

abstract class Products(
    open val name: String, // Змінюємо var на val
    open val type: String,
    open val price: Double?,
    open val description: String,
    open val imageUrl: String,
    open val seller: String
) : Parcelable {
    fun addProduct() {}
    fun editProduct() {}
    fun deleteProduct() {}
    fun viewProductDetails() {}
    abstract fun isAvailable(): Boolean
}