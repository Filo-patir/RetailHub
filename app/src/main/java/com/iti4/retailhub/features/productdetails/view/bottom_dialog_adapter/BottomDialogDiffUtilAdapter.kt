package com.iti4.retailhub.features.productdetails.view.bottom_dialog_adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iti4.retailhub.ProductDetailsQuery
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.BottomDialogDetailsRvBinding
import com.iti4.retailhub.databinding.ProductDetailsBottomSheetBinding
import com.iti4.retailhub.databinding.ProductDetailsRecycleviewItemBinding


class BottomDialogDiffUtilAdapter(
    private val type: String,
    private val onClick: ButtomDialogOnClickListn,
    private val selectedItem:String) : ListAdapter<String, BottomDialogDiffUtilAdapter.ViewHolder>(BottomDialogDiffUtilClass()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BottomDialogDetailsRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        Log.d("onBindViewHolder", "onBindViewHolder:$selectedItem ")


            if (type=="size") {
                holder.binding.textView31.text = item
                if(selectedItem==item) {
                    holder.binding.textView31.setTextColor(holder.itemView.context.getColor(R.color.white))
                    holder.binding.cardView3.setCardBackgroundColor(
                        holder.binding.root.context.getColor(
                           R.color.red_color
                        )
                    )
                }
            }else {
                setColor(holder, item)
            }
        if(selectedItem!=item) {
            holder.binding.cardView3.setOnClickListener {
                onClick.choosedItem(position, item, type)
            }
        }

    }
    /*override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        Log.d("onBindViewHolder", "onBindViewHolder:$selectedItem ")
        holder.binding.textView31.text = item
        if(selectedItem!=item) {
            holder.binding.cardView3.setOnClickListener {
                onClick.choosedItem(position, item, type)
            }
        }else{
            holder.binding.textView31.setTextColor(holder.binding.root.context.getColor(com.iti4.retailhub.R.color.white))
            holder.binding.cardView3.setCardBackgroundColor(holder.binding.root.context.getColor(com.iti4.retailhub.R.color.red_color))
        }


    }*/
    private fun setColor(holder: ViewHolder, color: String) {
        when (color.toLowerCase()) {
            "burgandy" -> holder.binding.cardView3.setCardBackgroundColor(holder.binding.root.context.getColor(com.iti4.retailhub.R.color.burgandy))
            "red" -> holder.binding.cardView3.setCardBackgroundColor(holder.binding.root.context.getColor(com.iti4.retailhub.R.color.red_color))
            "white" -> holder.binding.cardView3.setCardBackgroundColor(holder.binding.root.context.getColor(com.iti4.retailhub.R.color.white))
            "blue" -> holder.binding.cardView3.setCardBackgroundColor(holder.binding.root.context.getColor(com.iti4.retailhub.R.color.blue))
            "black" -> holder.binding.cardView3.setCardBackgroundColor(holder.binding.root.context.getColor(com.iti4.retailhub.R.color.black))
            "gray" -> holder.binding.cardView3.setCardBackgroundColor(holder.binding.root.context.getColor(com.iti4.retailhub.R.color.gray))
            "light_brown" -> holder.binding.cardView3.setCardBackgroundColor(holder.binding.root.context.getColor(com.iti4.retailhub.R.color.light_brown))
            "beige" -> holder.binding.cardView3.setCardBackgroundColor(holder.binding.root.context.getColor(com.iti4.retailhub.R.color.beige))
            "yellow" -> holder.binding.cardView3.setCardBackgroundColor(holder.binding.root.context.getColor(com.iti4.retailhub.R.color.yellow))
        }
    }

    class ViewHolder(val binding: BottomDialogDetailsRvBinding) : RecyclerView.ViewHolder(binding.root)
}