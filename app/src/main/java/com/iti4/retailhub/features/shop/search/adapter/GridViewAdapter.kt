package com.iti4.retailhub.features.shop.search.adapter

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.RvShopGridItemBinding
import com.iti4.retailhub.features.home.OnClickGoToDetails
import com.iti4.retailhub.features.home.adapter.HomeProductsDiffUtils
import com.iti4.retailhub.logic.toTwoDecimalPlaces
import com.iti4.retailhub.models.CountryCodes
import com.iti4.retailhub.models.HomeProducts

class GridViewAdapter(
    private val handleAction: OnClickGoToDetails,
    val currencyCodes: CountryCodes,
    val conversionRate: Double,
    private val isGuest: Boolean
) : ListAdapter<HomeProducts, GridViewAdapter.ListViewHolder>(
    HomeProductsDiffUtils()
) {
    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RvShopGridItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.rv_shop_grid_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val item = getItem(position)
        val convertedPrice = (item.maxPrice.toDouble() * conversionRate).toTwoDecimalPlaces()

        holder.binding.apply {
            Glide.with(holder.itemView).load(item.image)
                .into(imageView)
            textView7.text = item.brand
            textView8.text = item.title?.split("|")?.get(1)
            newItemPrice.text = "$convertedPrice $currencyCodes"
            root.setOnClickListener {
                handleAction.goToDetails(item.id!!)
            }
            if (item.isFav) {
                favBtn.setImageResource(R.drawable.fav_filled)

            }
            else {
                favBtn.setImageResource(R.drawable.baseline_favorite_border_24)
            }
            favBtn.setOnClickListener {
                if (!item.isFav && !isGuest) {
                    item.isFav = true
                    handleAction.saveToFavorites(
                        item.id!!,
                        item.title!!, item.image,
                        buildString {
                            append(item.maxPrice)
                            append(" ")
                            append(item.currencyCode)
                        }
                    )
                } else {
                    item.isFav = false
                    handleAction.deleteFromCustomerFavorites(item.favID.toString())
                }
                notifyItemChanged(position)
            }
        }
    }
}