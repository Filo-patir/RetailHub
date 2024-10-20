package com.iti4.retailhub.features.favorits.view.adapter

interface OnFavoritItemClocked {
    abstract fun onShowFavoritItemDetails(variantId: String)
    abstract fun showDeleteAlert(id: String)

}
