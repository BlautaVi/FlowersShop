package com.example.flowersshop.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductItem(
    val id: String = "",
    override val name: String = "",
    override val type: String = "",
    override val price: Double = 0.0,
    override val description: String = "",
    override val photoUrl: String = "",
    override val userId: String = ""
) : Products(
    name = name,
    type = type,
    price = price,
    description = description,
    photoUrl = photoUrl,
    userId = userId
), Parcelable {
    override fun isAvailable(): Boolean = true
}