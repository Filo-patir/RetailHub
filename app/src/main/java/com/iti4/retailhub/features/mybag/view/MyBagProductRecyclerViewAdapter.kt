package com.iti4.retailhub.features.mybag.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iti4.retailhub.CustomDialog
import com.iti4.retailhub.databinding.RvMybagProductItemBinding
import com.iti4.retailhub.features.mybag.OnClickMyBag
import com.iti4.retailhub.logic.toTwoDecimalPlaces
import com.iti4.retailhub.models.CartProduct
import com.iti4.retailhub.models.CountryCodes


class DiffUtilProduct : DiffUtil.ItemCallback<CartProduct>() {
    override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
        return oldItem.itemId == newItem.itemId
    }

    override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
        return oldItem == newItem
    }
}

class MyBagProductRecyclerViewAdapter(
    val handleActions: OnClickMyBag,
    val currencyCode: CountryCodes,
    val conversionRate: Double
) :
    ListAdapter<CartProduct, MyBagProductRecyclerViewAdapter.ViewHolder>(DiffUtilProduct()) {

    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding =
            RvMybagProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        with(holder.binding) {
            Glide.with(context!!)
                .load(item.itemImage)
                .into(favoritimage)
            val quantity = item.itemQuantity
            val inventoryQuantity = item.inventoryQuantity

            val price = item.itemPrice.toDouble() * conversionRate
            tvMyBagProductName.text = item.itemTitle
            tvMyBagProductPrice.text = price.toTwoDecimalPlaces() + " $currencyCode"
            tvMyBagProductColor.text = item.itemColor
            tvMyBagProductSize.text = item.itemSize

            if (inventoryQuantity!! < 1)
                tvMyBagProductCount.text = "Out of Stock"

            if (quantity > inventoryQuantity)
                tvMyBagProductCount.text = inventoryQuantity.toString()
            else
                tvMyBagProductCount.text = quantity.toString()


            btnMyBagDelete.setOnClickListener {
                val dialog =
                    CustomDialog(it.context, null, handleActions, "mybag")
                dialog.show()
                dialog.getData(null, item, ::deleteMyBagItem)

            }

            btnMyBagAdd.setOnClickListener {
                val currentCountString = tvMyBagProductCount.text.toString()
                val currentCount = Integer.parseInt(currentCountString) + 1
                if (currentCount > inventoryQuantity) {
                    tvMaxReached.visibility = View.VISIBLE
                } else {
                    item.itemQuantity = currentCount
                    item.didQuantityChanged = true
                    handleActions.updateTotalPrice()
                    tvMyBagProductCount.text = (currentCount).toString()
                }
            }

            btnMyBagRemove.setOnClickListener {
                val currentCountString = tvMyBagProductCount.text.toString()
                val currentCount = Integer.parseInt(currentCountString) - 1
                if (currentCount in 1..<inventoryQuantity) {
                    tvMaxReached.visibility = View.GONE
                }
                if (currentCount >= 1) {
                    item.itemQuantity = currentCount
                    item.didQuantityChanged = true
                    handleActions.updateTotalPrice()
                    tvMyBagProductCount.text = (currentCount).toString()
                }
            }

        }
    }

    fun deleteMyBagItem(cartProduct: CartProduct) {
        val currentList = currentList.toMutableList()
        currentList.remove(cartProduct)
        submitList(currentList)
    }

    inner class ViewHolder(val binding: RvMybagProductItemBinding) :
        RecyclerView.ViewHolder(binding.root)


}