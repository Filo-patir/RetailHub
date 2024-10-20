package com.iti4.retailhub.features.shop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iti4.retailhub.R
import com.iti4.retailhub.constants.CategoriesName
import com.iti4.retailhub.databinding.RvCategoryItemBinding
import com.iti4.retailhub.logic.toImg

class CategoryItemAdapter(private val list :List<String>,private val category: String,private val listener : OnClickNavigate) :
    RecyclerView.Adapter<CategoryItemAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RvCategoryItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_category_item, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            val categoryName = CategoriesName.fromId(category)
            textView9.text = item
            imageView5.setImageResource(item.toImg(categoryName))
            root.setOnClickListener { listener.navigate(categoryName.toString(), item) }
        }
    }
}