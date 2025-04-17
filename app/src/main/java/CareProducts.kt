package com.example.flowersshop.models
import android.media.Image



abstract class CareProducts(
    name: String,
    type: String,
    price: Double,
    description: String,
    imageUrl: String,
    seller: String
) : Products(name, type, price, description, imageUrl, seller) {
}
