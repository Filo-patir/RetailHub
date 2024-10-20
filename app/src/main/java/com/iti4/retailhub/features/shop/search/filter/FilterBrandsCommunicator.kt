package com.iti4.retailhub.features.shop.search.filter

import com.iti4.retailhub.models.Brands

interface FilterBrandsCommunicator {
    fun setBrands(brands: List<Brands>)
}
