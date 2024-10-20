package com.iti4.retailhub.features.home.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.BrandItemBinding
import com.iti4.retailhub.features.shop.adapter.OnClickNavigate
import com.iti4.retailhub.models.Brands

class BrandAdapter(private val navigator: OnClickNavigate) : ListAdapter<Brands, BrandAdapter.ViewHolder>(BrandUtils()) {
    class ViewHolder( itemVew: View) : RecyclerView.ViewHolder(itemVew) {
        val binding = BrandItemBinding.bind(itemVew)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(View.inflate(parent.context, R.layout.brand_item, null).rootView)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            Glide.with(holder.itemView)
                .load(item.image).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter()
                .apply(RequestOptions().override(150, 150))
                .into(imageView4)
            root.setOnClickListener { navigator.navigate(item.title,"") }
        }
    }
}