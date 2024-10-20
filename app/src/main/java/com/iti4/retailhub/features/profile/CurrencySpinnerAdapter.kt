package com.iti4.retailhub.features.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.iti4.retailhub.databinding.CurrencySpinnerLayoutBinding
import com.iti4.retailhub.models.CurrencySpinnerItem
import com.iti4.retailhub.models.toDrawable


class CurrencySpinnerAdapter(
    context: Context,
    private val items: List<CurrencySpinnerItem>
) : ArrayAdapter<CurrencySpinnerItem>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent)
    }

    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: CurrencySpinnerLayoutBinding = if (convertView == null) {
            CurrencySpinnerLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        } else {
            CurrencySpinnerLayoutBinding.bind(convertView)
        }

        val item = getItem(position)

        item?.let {
            binding.ivCountry.setImageResource(it.country.toDrawable())
            binding.tvCountryText.text = it.country.toString()
        }

        return binding.root
    }
}