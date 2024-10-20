package com.iti4.retailhub.models

import com.iti4.retailhub.R


data class CurrencyResponse(
    val result: String,
    val base_code: String,
    val conversion_rates: Map<String, Double>
)

enum class CountryCodes {
    EGP, USD, EUR, AED
}
fun CountryCodes.toDrawable(): Int {
    return when (this) {
        CountryCodes.EGP -> R.drawable.ic_egp
        CountryCodes.USD -> R.drawable.ic_usa
        CountryCodes.EUR -> R.drawable.ic_eur
        CountryCodes.AED -> R.drawable.ic_aed
    }
}


data class CurrencySpinnerItem( val country: CountryCodes)