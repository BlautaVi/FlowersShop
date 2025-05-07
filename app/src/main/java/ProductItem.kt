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
    override val userId: String = "",
    val availableQuantity: Int = 0,
    val isAvailableFlag: Boolean = true
) : Products(name, type, price, description, photoUrl, userId), Parcelable {
    constructor() : this("", "", "", 0.0, "", "", "")
    override fun isAvailable(): Boolean = isAvailableFlag && availableQuantity > 0
    override fun getProductInfo(): String {
        return "${super.getProductInfo()}, ID: $id, Available: $availableQuantity"
    }
    fun updateQuantity(newQuantity: Int): ProductItem {
        return copy(availableQuantity = newQuantity, isAvailableFlag = newQuantity > 0)
    }
}
