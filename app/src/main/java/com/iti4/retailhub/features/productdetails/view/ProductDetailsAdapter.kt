package com.iti4.retailhub.features.productdetails.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iti4.retailhub.ProductDetailsQuery
import com.iti4.retailhub.R

class ProductDetailsAdapter(
    private val images: List<ProductDetailsQuery.Edge2>
) : RecyclerView.Adapter<ProductDetailsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = images[position]
        // Assuming you're using a library like Glide or Picasso to load the image
        Glide.with(holder.itemView.context)
            .load(imageUrl.node.url.toString().split("?")[0])
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = images.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.productImageView)
    }
}
