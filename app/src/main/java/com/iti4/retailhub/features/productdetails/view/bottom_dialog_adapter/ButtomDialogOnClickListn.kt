package com.iti4.retailhub.features.productdetails.view.bottom_dialog_adapter

interface ButtomDialogOnClickListn {
    abstract fun choosedItem(position: Int, item: String?, type: String)
}