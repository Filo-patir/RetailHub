package com.iti4.retailhub.features.checkout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iti4.retailhub.databinding.RvMybagCoupounItemBinding
import com.iti4.retailhub.models.Discount
import com.iti4.retailhub.models.toDrawable

class DiffUtilPromo : DiffUtil.ItemCallback<Discount>() {
    override fun areItemsTheSame(oldItem: Discount, newItem: Discount): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: Discount, newItem: Discount): Boolean {
        return oldItem == newItem
    }
}

class MyBagCouponRecyclerViewAdapter(private val onClickApply: OnClickApply) :
    ListAdapter<Discount, MyBagCouponRecyclerViewAdapter.ViewHolder>(DiffUtilPromo()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RvMybagCoupounItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvMyBagPromoTitle.text = item.value
            tvMyBagPromoCode.text = item.title
            ivMyBagPromoImage.setImageResource(item.toDrawable())
            btnMyBagPromoApply.setOnClickListener {
                onClickApply.onClickApply(item)
            }
        }

    }


    inner class ViewHolder(val binding: RvMybagCoupounItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}