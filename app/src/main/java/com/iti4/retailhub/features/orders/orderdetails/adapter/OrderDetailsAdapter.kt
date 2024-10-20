package com.iti4.retailhub.features.orders.orderdetails.adapter

import android.view.View
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.iti4.retailhub.databinding.RvOrderDetailsItemBinding
import com.iti4.retailhub.logic.toTwoDecimalPlaces
import com.iti4.retailhub.models.CountryCodes
import com.iti4.retailhub.models.OrderDetailsItem


class OrderDetailsAdapter(private val conversionRate:Double , private val currencyCode: CountryCodes)  : ListAdapter<OrderDetailsItem, OrderDetailsAdapter.OrderDetailsViewHolder>(OrderDetailsItemUtils()) {
    class OrderDetailsViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val binding = RvOrderDetailsItemBinding.bind(itemView)
    }
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): OrderDetailsViewHolder {
        return OrderDetailsViewHolder(
            android.view.LayoutInflater.from(parent.context).inflate(
                com.iti4.retailhub.R.layout.rv_order_details_item,
                parent,
                false
            )
        )
    }
    override fun onBindViewHolder(holder: OrderDetailsViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            Glide.with(holder.itemView.context)
                .load(item.image).diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions().override(150, 150))
                .into(orderImg)
            orderName.text = item.title.split("|").last()
            orderBrand.text = item.brand
            orderColor.text = item.color
            orderSize.text = item.size
            orderQuantity.text = item.quantity
            orderPrice.text = buildString {
                append((item.price.toDouble()*conversionRate).toTwoDecimalPlaces())
                append(" ")
                append(currencyCode.name)
            }
        }
    }
}