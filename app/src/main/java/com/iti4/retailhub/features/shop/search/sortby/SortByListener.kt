package com.iti4.retailhub.features.shop.search.sortby

import com.iti4.retailhub.constants.SortBy

interface SortByListener {
    fun onSortBySelected(sortBy: SortBy)
}
