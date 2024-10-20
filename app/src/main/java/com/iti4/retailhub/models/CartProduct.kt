package com.iti4.retailhub.models

import android.os.Parcel
import android.os.Parcelable


data class CartProduct(
    val draftOrderId: String, val itemId: String,
    var itemQuantity: Int, val inventoryQuantity: Int, val itemTitle: String,
    val itemSize: String, val itemColor: String,
    val itemPrice: String, val itemImage: String,
    var didQuantityChanged: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt(), parcel.readInt(),
        parcel.readString().toString(), parcel.readString().toString(),
        parcel.readString().toString(), parcel.readString().toString(),
        parcel.readString().toString(), parcel.readByte() != 0.toByte(),
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(draftOrderId)
        parcel.writeString(itemId)
        parcel.writeInt(itemQuantity)
        parcel.writeInt(inventoryQuantity)
        parcel.writeString(itemTitle)
        parcel.writeString(itemSize)
        parcel.writeString(itemColor)
        parcel.writeString(itemPrice)
        parcel.writeString(itemImage)
        parcel.writeByte(if (didQuantityChanged) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CartProduct> {
        override fun createFromParcel(parcel: Parcel): CartProduct {
            return CartProduct(parcel)
        }

        override fun newArray(size: Int): Array<CartProduct?> {
            return arrayOfNulls(size)
        }
    }

}

fun CartProduct.toLineItem(): LineItemInputModel {

    return LineItemInputModel(
        variantId = this.itemId,
        quantity = this.itemQuantity
    )
}