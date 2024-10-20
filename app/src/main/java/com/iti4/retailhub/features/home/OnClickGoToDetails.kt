package com.iti4.retailhub.features.home

import com.iti4.retailhub.models.Discount

interface OnClickGoToDetails {
    fun goToDetails(productId: String)
    fun saveToFavorites(
        productId:String,
        productTitle: String,
        selectedImage: String,
        price: String
    )

     fun deleteFromCustomerFavorites(pinFavorite: String)
}