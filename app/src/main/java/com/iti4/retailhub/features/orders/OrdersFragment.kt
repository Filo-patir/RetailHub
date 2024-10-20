package com.iti4.retailhub.features.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.iti4.retailhub.databinding.FragmentOrdersBinding
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.orders.adapter.OrdersAdapter
import com.iti4.retailhub.logic.ToolbarSetup
import com.iti4.retailhub.models.CountryCodes
import com.iti4.retailhub.models.Order
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrdersFragment : Fragment() {
    private lateinit var currencyCode: CountryCodes
    private var conversionRate: Double = 0.0
    lateinit var binding: FragmentOrdersBinding
    private val viewModel: OrdersViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currencyCode = viewModel.getCurrencyCode()
        conversionRate = viewModel.getConversionRates(currencyCode)

        ToolbarSetup.setupToolbar(binding.ordersAppbar, "Orders", resources, {activity?.onBackPressed()})
        val adapter = OrdersAdapter(conversionRate,currencyCode)
        binding.ordersRV.adapter = adapter
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED){
                viewModel.orders.collect{
                    when(it){
                        is ApiState.Success<*> -> {
                            adapter.submitList(it.data as List<Order>)
                        }
                        is ApiState.Error -> {
                            Toast.makeText(requireContext(), it.exception.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}