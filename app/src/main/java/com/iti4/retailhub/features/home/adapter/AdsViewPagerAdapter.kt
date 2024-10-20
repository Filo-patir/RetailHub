package com.iti4.retailhub.features.home.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iti4.retailhub.databinding.VpHomeAdsLayoutBinding
import com.iti4.retailhub.features.home.OnClickAddCopyCoupon
import com.iti4.retailhub.features.home.OnClickGoToDetails
import com.iti4.retailhub.models.Discount
import com.iti4.retailhub.models.toDrawable


class AdsViewPagerAdapter(
    private var data: List<Discount>,
    private val actionHandler: OnClickAddCopyCoupon
) :
    RecyclerView.Adapter<AdsViewPagerAdapter.ViewHolder>() {
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding =
            VpHomeAdsLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        with(holder.binding) {
            ivDiscountImage.setImageResource(item.toDrawable())
            root.setOnClickListener {
                actionHandler.copyCoupon(item)
            }
        }
    }


    fun setData(data: List<Discount>) {
        this.data = data
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: VpHomeAdsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)


}