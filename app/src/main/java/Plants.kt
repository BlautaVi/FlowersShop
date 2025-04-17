package com.example.flowersshop.models

abstract class Plants(
    name: String,
    type: String,
    price: Double,
    description: String,
    imageUrl: String,
    seller: String
) : Products(name, type, price, description, imageUrl, seller) {
}
