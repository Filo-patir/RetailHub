package com.iti4.retailhub.features.shop.search.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.FragmentFilterBinding
import com.iti4.retailhub.features.shop.search.filter.bottomsheet.BrandsBottomSheetFragment
import com.iti4.retailhub.features.shop.search.viewmodels.FilterViewModel
import com.iti4.retailhub.logic.ToolbarSetup
import com.iti4.retailhub.models.Brands
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilterFragment : Fragment(), FilterBrandsCommunicator {
    private val viewModel by viewModels<FilterViewModel>()
    private lateinit var binding: FragmentFilterBinding
    private var query : String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        binding.apply {
            filterBrandsBtn.setOnClickListener {
                BrandsBottomSheetFragment(this@FilterFragment).show(childFragmentManager, null)
            }
        }
        binding.applyBtn.setOnClickListener {
            binding.apply {
                query+= " price:>=${rangeSlider.values[0].toInt()} price:<=${rangeSlider.values[1].toInt()} "
                query += if (chipGroup3.checkedChipId == View.NO_ID) "" else view.findViewById<Chip>(chipGroup3.checkedChipId).text.toString()
            }
            findNavController().navigate(R.id.action_filterFragment_to_searchFragment, bundleOf("query" to query))
        }
        binding.discardBtn.setOnClickListener { findNavController().navigateUp() }
    }

    private fun setupToolbar() {
        ToolbarSetup.setupToolbar(binding.filterAppbar, "Filters", resources, {activity?.onBackPressed()})
        binding.filterAppbar.apply {
            imageButton.visibility = View.GONE
            appBar.setExpanded(false)
        }
    }

    override fun setBrands(brands: List<Brands>) {
        val allBrands = brands as ArrayList
        query += " AND ( ${allBrands[0].title}"
        allBrands.removeFirst()
        allBrands.forEach {
            query += " OR ${it.title}"
        }
        query += " )"
    }
}