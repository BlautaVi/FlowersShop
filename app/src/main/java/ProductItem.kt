package com.example.flowersshop.models

import android.os.Parcel
import android.os.Parcelable

data class ProductItem(
    val id: String = "",
    override val name: String = "",
    override val type: String = "",
    override val price: Double? = 0.0,
    override val description: String = "",
    val photoUrl: String = "",
    val userId: String = ""
) : Products(
    name = name,
    type = type,
    price = price,
    description = description,
    imageUrl = photoUrl,
    seller = userId
), Parcelable {

    override fun isAvailable(): Boolean = true

    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        name = parcel.readString() ?: "",
        type = parcel.readString() ?: "",
        price = if (parcel.readByte() == 0.toByte()) null else parcel.readDouble(),
        description = parcel.readString() ?: "",
        photoUrl = parcel.readString() ?: "",
        userId = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(type)
        if (price == null) {
            parcel.writeByte(0.toByte())
        } else {
            parcel.writeByte(1.toByte())
            parcel.writeDouble(price)
        }
        parcel.writeString(description)
        parcel.writeString(photoUrl)
        parcel.writeString(userId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ProductItem> {
        override fun createFromParcel(parcel: Parcel): ProductItem {
            return ProductItem(parcel)
        }

        override fun newArray(size: Int): Array<ProductItem?> {
            return arrayOfNulls(size)
        }
    }
}