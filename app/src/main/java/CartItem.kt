package com.example.flowersshop.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class CartItem(
    val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val productName: String = "",
    val productType: String = "",
    val productPrice: Double = 0.0,
    val productPhotoUrl: String = "",
    val quantity: Int = 1
) : Parcelable {
    fun getTotalCost(): Double = productPrice * quantity
    fun decreaseQuantity(): CartItem {
        return copy(quantity = quantity - 1)
    }
    fun isEmpty(): Boolean = quantity <= 0
}
