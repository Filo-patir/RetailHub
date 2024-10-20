package com.iti4.retailhub.features.orders.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.RvOrderItemBinding
import com.iti4.retailhub.logic.toTwoDecimalPlaces
import com.iti4.retailhub.models.CountryCodes
import com.iti4.retailhub.models.Order
import com.stripe.android.core.model.Country

class OrdersAdapter(private val conversionRate:Double , private val currencyCode:CountryCodes) : ListAdapter<Order, OrdersAdapter.OrderViewHolder>(OrderDiffUtils()) {
    lateinit var context : Context
    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding = RvOrderItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        context = parent.context
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_order_item, parent, false))
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val item = getItem(position)
        Log.d("Filo", "onBindViewHolder: ${item.id}")
        holder.binding.apply {
            OrderName.text = buildString {
                append("Order ")
                append(item.name)
            }
            TrackingNumber.text = item.number
            date.text = item.dateTime
            when(item.fulfillmentStatus){
                "FULFILLED" -> {
                    deliveryStatus.text = context.getString(R.string.delivered)
                }
                else -> {
                    deliveryStatus.text = context.getString(R.string.processing)
                }
            }
            when(item.financialStatus){
                "PAID" -> {
                    payStatus.text = context.getString(R.string.paid)
                }
                else -> {
                    payStatus.visibility = View.GONE
                }
            }
            quantity.text = item.quantity.toString()

            total.text = buildString {
                append((item.price.toDouble()*conversionRate).toTwoDecimalPlaces())
                append(" ")
                append(currencyCode.name)
            }
            details.setOnClickListener { Navigation.findNavController(it).navigate(R.id.orderDetailsFragment,
                bundleOf("orderID" to item.id)
            ) }
        }
    }
}