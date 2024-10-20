package com.iti4.retailhub.features.shop.search.filter.bottomsheet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.iti4.retailhub.R
import com.iti4.retailhub.models.Brands

class BrandsBottomSheetAdapter : ListAdapter<Brands, BrandsBottomSheetAdapter.BrandsBottomSheetViewHolder>(BrandsUtils()) {
    class BrandsBottomSheetViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val binding = com.iti4.retailhub.databinding.RvFilterItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandsBottomSheetViewHolder {
        return BrandsBottomSheetViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_filter_item, parent, false))
    }

    override fun onBindViewHolder(holder: BrandsBottomSheetViewHolder, position: Int) {
        val brand = getItem(position)
        holder.binding.apply {
            brandName.text = brand.title
            checkBox.setOnCheckedChangeListener{
                _, isChecked ->
                brand.isChecked = isChecked
                submitList(currentList)
            }
            if (brand.isChecked){
                brandName.setTextColor(holder.itemView.context.resources.getColor(R.color.red_color, null))
            }
            else{
                brandName.setTextColor(holder.itemView.context.resources.getColor(R.color.black_variant, null))
            }
                checkBox.isChecked = brand.isChecked
        }
    }

    fun getSelectedBrands(): List<Brands>{
        return currentList.filter { it.isChecked }
    }
}