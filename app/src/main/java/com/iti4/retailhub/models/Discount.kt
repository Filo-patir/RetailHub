package com.iti4.retailhub.models

import com.iti4.retailhub.R

data class Discount(val id: String, val value: String, val title: String) {
    fun getDiscountAsDouble(): Double {
        return if (value.endsWith("%")) {
            value.removeSuffix("%").toDouble()
        } else {
            0.0
        }
    }

}

fun Discount.toDrawable(): Int {
    return when (this.value) {
        "5%" -> R.drawable.ads5
        "10%" -> R.drawable.ads10
        "15%" -> R.drawable.ads15
        "20%" -> R.drawable.ads20
        "25%" -> R.drawable.ads25
        else -> {
            1
        }
    }
}
