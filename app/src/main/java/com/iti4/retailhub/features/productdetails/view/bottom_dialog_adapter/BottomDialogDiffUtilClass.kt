package com.iti4.retailhub.features.productdetails.view.bottom_dialog_adapter

import androidx.recyclerview.widget.DiffUtil
import com.iti4.retailhub.ProductDetailsQuery

class BottomDialogDiffUtilClass : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

}
