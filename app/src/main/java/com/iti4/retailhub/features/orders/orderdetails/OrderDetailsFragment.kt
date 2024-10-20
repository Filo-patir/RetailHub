package com.iti4.retailhub.features.orders.orderdetails

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.FragmentOrderDetailsBinding
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.orders.orderdetails.adapter.OrderDetailsAdapter
import com.iti4.retailhub.logic.ToolbarSetup
import com.iti4.retailhub.logic.toTwoDecimalPlaces
import com.iti4.retailhub.models.CountryCodes
import com.iti4.retailhub.models.OrderDetails
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderDetailsFragment : Fragment() {
    private lateinit var binding: FragmentOrderDetailsBinding
    private val viewModel: OrderDetailsViewModel by viewModels()
    private lateinit var currencyCode: CountryCodes
    private var conversionRate: Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currencyCode = viewModel.getCurrencyCode()
        conversionRate = viewModel.getConversionRates(currencyCode)
        val orderID = arguments?.getString("orderID")
        Log.d("Filo", "onViewCreated: $orderID")
        if (orderID != null){
            viewModel.getOrderDetails(orderID)
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.orderDetails.collect{
                    when (it){
                        is ApiState.Success<*> -> {
                            setupData(it.data as OrderDetails)
                        }
                        is ApiState.Error -> {
                            Toast.makeText(requireContext(), it.exception.message, Toast.LENGTH_SHORT)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun setupData(orderDetails: OrderDetails){
        ToolbarSetup.setupToolbar(binding.orderDetailsAppbar, "Order Details", resources, {activity?.onBackPressed()})
        val adapter = OrderDetailsAdapter(conversionRate,currencyCode)
        binding.apply {
            orderDetailsAppbar.collapsedPageName.visibility = View.GONE
            orderDetailsRecyclerView.adapter = adapter
            adapter.submitList(orderDetails.items)
            orderDetailsName.text = orderDetails.name
            orderDetailsAppbar.imageButton.setOnClickListener {
                requireActivity().findNavController(R.id.fragmentContainerView2).navigate(R.id.producSearchFragment)
            }
            orderDetailsDate.text = orderDetails.date
            orderDetailsStatus.text = orderDetails.status
            orderDetailsNumber.text = orderDetails.number
            orderDetailsQuantity.text = orderDetails.quantity
            orderDetailsShippingAddress.text = orderDetails.shippingAddress
            orderDetailsPaymentStatus.text = orderDetails.financialStatus
            orderDetailsDiscount.text = (orderDetails.discount.toDouble()*conversionRate).toTwoDecimalPlaces()+" "+currencyCode
            orderDetailsTotalAmount.text = buildString {
                append((orderDetails.total.toDouble()*conversionRate).toTwoDecimalPlaces())
                append(" ")
                append(currencyCode.name)
            }
        }
    }
}