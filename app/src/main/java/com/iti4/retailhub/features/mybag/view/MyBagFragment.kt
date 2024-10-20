package com.iti4.retailhub.features.mybag.view

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.iti4.retailhub.MainActivity
import com.iti4.retailhub.MainActivityViewModel
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.FragmentMyBagBinding
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.login_and_signup.view.LoginAuthinticationActivity
import com.iti4.retailhub.features.login_and_signup.viewmodel.UserAuthunticationViewModelViewModel
import com.iti4.retailhub.features.mybag.OnClickMyBag
import com.iti4.retailhub.features.mybag.viewmodel.MyBagViewModel
import com.iti4.retailhub.logic.toTwoDecimalPlaces
import com.iti4.retailhub.models.CartProduct
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyBagFragment : Fragment(), OnClickMyBag {
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    val userAuthViewModel: UserAuthunticationViewModelViewModel by viewModels<UserAuthunticationViewModelViewModel>()
    private var conversionRate: Double? = null
    private lateinit var binding: FragmentMyBagBinding
    private val viewModel by viewModels<MyBagViewModel>()
    private lateinit var adapter: MyBagProductRecyclerViewAdapter
    private var totalPrice: Double? = null
    private var cartProductList: MutableList<CartProduct>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyBagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (userAuthViewModel.isguestMode()) {
            binding.guestb.visibility=View.VISIBLE
           binding.tvMessage.text=getString(R.string.please_login_to_use_this_feature)
            binding.btnOkayd.setOnClickListener {
                val intent = Intent(requireContext(), LoginAuthinticationActivity::class.java)
                intent.putExtra("guest","guest")
                startActivity(intent)
                requireActivity().finish()
            }
        } else {
            conversionRate = viewModel.getConversionRates(viewModel.getCurrencyCode())
            adapter =
                MyBagProductRecyclerViewAdapter(this, viewModel.getCurrencyCode(), conversionRate!!)
            lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                    viewModel.myBagProductsState.collect { item ->
                        when (item) {
                            is ApiState.Success<*> -> {
                                val data = item.data as List<CartProduct>
                                if (!data.isNullOrEmpty()) {
                                    cartProductList = data.toMutableList()
                                    updateTotalPrice()
                                    adapter.submitList(data)
                                    binding.lottibagAnimation.visibility = View.GONE
                                } else
                                    binding.lottibagAnimation.visibility = View.VISIBLE
                            }

                            is ApiState.Error -> {
                                Log.d("Filo", "onViewCreated: ${item.exception}")
                            }

                            is ApiState.Loading -> {
                                Log.d("Filo", "onViewCreated: Loading")
                            }
                        }
                    }
                }
            }
            binding.btnCheckout.setOnClickListener {
                if (!cartProductList.isNullOrEmpty()) {
                    val bundle = Bundle().apply {
                        putParcelableArrayList("data", cartProductList as ArrayList<CartProduct>)
                        putDouble("totalprice", totalPrice ?: 0.0)
                    }
                    requireActivity().findNavController(R.id.fragmentContainerView2)
                        .navigate(R.id.checkoutFragment, bundle)
                }
            }

            val manager = LinearLayoutManager(requireContext())
            manager.setOrientation(RecyclerView.VERTICAL)
            binding.rvMyBag.layoutManager = manager
            binding.rvMyBag.adapter = adapter
            adapter.submitList(
                listOf(
                )
            )
        }
    }

    override fun deleteMyBagItem(cartProduct: CartProduct) {
        viewModel.deleteMyBagItem(cartProduct.draftOrderId)
        cartProductList?.remove(cartProduct)
        if (cartProductList.isNullOrEmpty())
            binding.lottibagAnimation.visibility = View.VISIBLE

        updateTotalPrice()
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.myBagProductsRemove.collect { item ->
                    when (item) {
                        is ApiState.Success<*> -> {
                            Toast.makeText(
                                this@MyBagFragment.requireContext(),
                                "Item Deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is ApiState.Error -> {
                            Log.d("Filo", "onViewCreated: ${item.exception}")
                        }

                        is ApiState.Loading -> {
                            Log.d("Filo", "onViewCreated: Loading")
                        }
                    }
                }
            }
        }
    }


    override fun updateTotalPrice() {
        totalPrice = 0.0
        totalPrice = cartProductList?.sumOf {
            val price = it.itemPrice?.toDoubleOrNull() ?: 0.0
            price * it.itemQuantity
        }!!
        totalPrice = totalPrice!! * conversionRate!!
        binding.tvMyBagProductPrice.text =
            "${totalPrice?.toTwoDecimalPlaces()} ${viewModel.getCurrencyCode()}"
    }

    private fun updateQuantity() {
        cartProductList?.forEach {
            if (it.didQuantityChanged) {
                viewModel.updateMyBagItem(it)
            }


        }
    }

    override fun onStart() {
        super.onStart()
        if (!userAuthViewModel.isguestMode()) {
            viewModel.getMyBagProducts()
        }
        binding.mybagAppbar.apply {
            appBar.setExpanded(false)
            collapsedPageName.visibility = View.GONE
            pageName.text = requireContext().getString(R.string.my_bag)
            backButton.setOnClickListener {
                activity?.onBackPressed()
            }
            imageButton.setOnClickListener { findNavController().navigate(R.id.producSearchFragment) }
        }
    }


    override fun onStop() {
        if (!userAuthViewModel.isguestMode()) {
            updateQuantity()
        }
        super.onStop()
    }
}


