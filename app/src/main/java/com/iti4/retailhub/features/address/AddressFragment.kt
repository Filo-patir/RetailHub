package com.iti4.retailhub.features.address

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iti4.retailhub.GetAddressesDefaultIdQuery
import com.iti4.retailhub.MainActivity
import com.iti4.retailhub.MainActivityViewModel
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.FragmentAddressBinding
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.logic.ToolbarSetup
import com.iti4.retailhub.models.CustomerAddressV2
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddressFragment : Fragment(), OnClickAddress {
    private val TAG: String = "AddressFragment"
    private var navigatedWithIntentionOf: String? = null
    private var reason: String? = null

    //animation
    private var animationClicked = false
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            this.requireContext(),
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            this.requireContext(),
            R.anim.rotate_close_anim
        )
    }
    private val fromButton: Animation by lazy {
        AnimationUtils.loadAnimation(
            this.requireContext(),
            R.anim.from_button_anim
        )
    }
    private val toButton: Animation by lazy {
        AnimationUtils.loadAnimation(
            this.requireContext(),
            R.anim.to_button_anim
        )
    }

    private lateinit var binding: FragmentAddressBinding

    // Shared viewModels
    private val viewModel: AddressViewModel by activityViewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private val adapter by lazy {
        // addressList is An Empty List initially
        AddressRecyclerViewAdapter(this, viewModel.addressesList)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(TAG, "onCreateView: ")
        binding = FragmentAddressBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButtonsClickListeners()

        // get list of saved addresses from graph

        Log.i(TAG, "onViewCreated: ")
        // then calls getDefault address state
        listenToDefaultAddress()


    }


    private fun listenToDefaultAddress() {
        lifecycleScope.launch {
            viewModel.defaultAddressState.collect { item ->
                when (item) {
                    is ApiState.Success<*> -> {
                        val customer = item.data as GetAddressesDefaultIdQuery.Customer
                        val defaultAddress = customer.defaultAddress
                        if (defaultAddress != null) {
                            viewModel.addressesList.forEach {
                                Log.i(
                                    TAG,
                                    "listenToDefaultAddress: actually default address is $defaultAddress"
                                )
                                if (it.id == defaultAddress.id)
                                    it.isDefault = true
                            }
                            if (mainActivityViewModel.indexOfLastDefaultAddress != 99) {
                                viewModel.addressesList.forEach { it.isDefault = false }
                                viewModel.addressesList[mainActivityViewModel.indexOfLastDefaultAddress].isDefault =
                                    true
                            }
                            adapter.listData = (viewModel.addressesList)
                            adapter.notifyDataSetChanged()
//
                            if (viewModel.addressesList.size > 0)
                                binding.addressNotFoundGroup.visibility = View.GONE
                            else
                                binding.addressNotFoundGroup.visibility = View.VISIBLE
                        }
                        Log.i(TAG, "listenToDefaultAddress: " + viewModel.addressesList)
                    }

                    is ApiState.Error -> {
                        Log.i("here", "listenToDefaultAddress On error")
                    }

                    is ApiState.Loading -> {
                        Log.i("here", "listenToDefaultAddress On loading")
                    }
                }
            }

        }
    }


    override fun onStart() {
        super.onStart()
        if (viewModel.addressesList != null) {

        }
//        viewModel.getAddressesById()
        reason = arguments?.getString("reason")
        if (reason == "changeShipping") {
            navigatedWithIntentionOf = reason
        } else if (reason == "addNew") {
            Log.i("here", "onStart:  add " + "here")
        }
        (activity as MainActivity).hideBottomNavBar()
        ToolbarSetup.setupToolbarMini(
            binding.checkoutAppbar,
            "Addresses",
            resources,
            { requireActivity().onBackPressed() }
        )

    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop: Address Fragment")
        viewModel.updateMyAddresses(viewModel.addressesList)
        //Log.i("here", "onStop: " + viewModel.addressesList.size)
        // viewModel.updateMyAddresses(viewModel.addressesList)

        // viewModel.updateCustomerDefaultAddress()
        (activity as MainActivity).showBottomNavBar()
    }

    override fun onDestroy() {

        super.onDestroy()

        //  viewModel.updateCustomerDefaultAddress()

    }

    override fun onResume() {
        super.onResume()
        adapter.listData = (viewModel.addressesList)
        adapter.notifyDataSetChanged()
        if (viewModel.addressesList.size == 0)
            binding.addressNotFoundGroup.visibility = View.VISIBLE
        else
            binding.addressNotFoundGroup.visibility = View.GONE
    }

    // call back when i click on edit
    override fun editDetails(address: CustomerAddressV2) {
        val bundle = Bundle().apply {
            putString("reason", "edit")
        }
        address.isNew = false
        viewModel.editCustomerAddress = address
        findNavController().navigate(R.id.addressDetailsFragment, bundle)
    }

    //callback when click on the delete icon
    override fun deleteAddress(id: String) {
        val index = viewModel.addressesList.indexOfFirst {
            it.id == id
        }
        viewModel.addressesList.remove(viewModel.addressesList[index])
        adapter.listData = (viewModel.addressesList)
        adapter.notifyDataSetChanged()
        if (viewModel.addressesList.size == 0)
            binding.addressNotFoundGroup.visibility = View.VISIBLE
    }

    // callback when click on the checkbox
    override fun setDefaultAddress(position: Int) {
//        viewModel.updateCustomerDefaultAddress(address.id!!)
        var index = 0
        viewModel.addressesList.forEach { _ ->
            viewModel.addressesList[index].isDefault = false
            index += 1
        }
        viewModel.addressesList[position].isDefault = true
        adapter.updateData(viewModel.addressesList)
        Log.i(TAG, "setDefaultAddress: i clicked on default ${viewModel.addressesList}")
        // viewModel.getDefaultAddress()
        // findNavController().navigateUp()
        if (navigatedWithIntentionOf == "changeShipping") {
            mainActivityViewModel.indexOfLastDefaultAddress = position
            viewModel.updateMyAddresses(viewModel.addressesList)
        }
    }

    //callback when click on the cardView
    override fun checkoutClickedAnAddress(address: CustomerAddressV2) {
        mainActivityViewModel.customerChoseAnAddressNotDefault = true
        mainActivityViewModel.customerChosenAddress = address
        requireActivity().findNavController(R.id.fragmentContainerView2).navigateUp()

    }


    private fun startAnimation() {
        if (!animationClicked) {
            binding.btnAddAddressMap.visibility = View.GONE
            binding.btnAddAddressText.visibility = View.GONE
            binding.btnAddAddress.startAnimation(rotateOpen)
            binding.btnAddAddressMap.startAnimation(fromButton)
            binding.btnAddAddressText.startAnimation(fromButton)
        } else {
            binding.btnAddAddressMap.visibility = View.VISIBLE
            binding.btnAddAddressText.visibility = View.VISIBLE
            binding.btnAddAddress.startAnimation(rotateClose)
            binding.btnAddAddressMap.startAnimation(toButton)
            binding.btnAddAddressText.startAnimation(toButton)
        }
        animationClicked = !animationClicked
    }

    private fun initButtonsClickListeners() {

        val manager = LinearLayoutManager(requireContext())
        manager.setOrientation(RecyclerView.VERTICAL)
        binding.rvAddress.layoutManager = manager
        binding.rvAddress.adapter = adapter


        binding.btnAddAddress.setOnClickListener {
            startAnimation()
        }
        binding.btnAddAddressMap.setOnClickListener {
            findNavController().navigate(R.id.addressMapFragment)
        }
        binding.btnAddAddressText.setOnClickListener {
            val bundle = Bundle().apply {
                putString("reason", "new")
            }
            findNavController().navigate(R.id.addressDetailsFragment, bundle)
        }
    }
}