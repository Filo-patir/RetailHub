package com.iti4.retailhub.models

data class HomeProducts (
    val id: String?,
    val title: String?,
    val stock:Int,
    val image: String,
    val brand: String,
    val maxPrice: String,
    val minPrice: String,
    val currencyCode: String,
    var isFav: Boolean = false,
    var favID: String? = null
)