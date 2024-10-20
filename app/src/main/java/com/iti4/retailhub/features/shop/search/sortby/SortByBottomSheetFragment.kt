package com.iti4.retailhub.features.shop.search.sortby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iti4.retailhub.constants.SortBy
import com.iti4.retailhub.databinding.FragmentSortByBottomSheetBinding

class SortByBottomSheetFragment(private val listener: SortByListener) :  BottomSheetDialogFragment() {
    lateinit var binding: FragmentSortByBottomSheetBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSortByBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            sortByTitle.setOnClickListener { listener.onSortBySelected(SortBy.RELEVANCE); dismiss() }
            highPrice.setOnClickListener { listener.onSortBySelected(SortBy.PRICE_DESC); dismiss() }
            lowPrice.setOnClickListener { listener.onSortBySelected(SortBy.PRICE_ASC); dismiss() }
        }
    }

    override fun onStart() {
        super.onStart()
        val bottomSheet =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.isFitToContents = true
            behavior.isDraggable = false
        }
    }

}