package com.iti4.retailhub.features.shop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.FragmentShopBinding
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.shop.adapter.OnClickNavigate
import com.iti4.retailhub.features.shop.adapter.ShopAdapter
import com.iti4.retailhub.logic.ToolbarSetup
import com.iti4.retailhub.models.Category
import com.iti4.retailhub.models.CountryCodes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShopFragment : Fragment(), OnClickNavigate {
    lateinit var binding: FragmentShopBinding
    private val viewModel by viewModels<ShopViewModel>()
    private val tabTitles = arrayOf("Men", "Women", "Kids")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ShopAdapter(this)
        binding.viewPager.adapter = adapter
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categoriesList.collect {
                    when (it) {
                        is ApiState.Success<*> -> {
                            it.data as List<Category>
                            Log.d("Filo", "onViewCreated: ${it.data}")
                            adapter.submitList(it.data)
                        }

                        is ApiState.Error -> {
                            adapter.submitList(emptyList())
                            Toast.makeText(
                                requireContext(),
                                it.exception.message,
                                Toast.LENGTH_SHORT
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
        TabLayoutMediator(binding.tablayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onStart() {
        super.onStart()
        ToolbarSetup.setupToolbar(binding.appBarLayout, "Categories", resources, {activity?.onBackPressed()})
    }

    override fun navigate(category: String, productType:String) {
        findNavController().navigate(R.id.searchFragment, bundleOf("query" to category, "type" to  productType))
    }
}