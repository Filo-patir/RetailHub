package com.iti4.retailhub.features.address

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.iti4.retailhub.R
import com.iti4.retailhub.databinding.FragmentAddressDetailsBinding
import com.iti4.retailhub.models.CustomerAddressV2
import com.iti4.retailhub.modelsdata.countries
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddressDetailsFragment : Fragment() {
    private val viewModel: AddressViewModel by activityViewModels()
    private lateinit var binding: FragmentAddressDetailsBinding
    private lateinit var mapAddress: PlaceLocation

    // if remains null i am adding a new item
    // if not null i am editing an old item
    private lateinit var details: CustomerAddressV2
    private lateinit var reason: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddressDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reason = arguments?.getString("reason")!!

        binding.btnSaveAddress.setOnClickListener {
            if (checkIfValidAddress()) {
                saveAddress()
                if (reason == "map") {
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.addressDetailsFragment, true)
                        .build()
                    findNavController().navigate(R.id.addressFragment, null, navOptions)
                }
                if (reason == "new") {
                    requireActivity().findNavController(R.id.fragmentContainerView2).navigateUp()
                } else {
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.addressDetailsFragment, true)
                        .build()
                    findNavController().navigate(R.id.addressFragment, null, navOptions)
                }
            }
        }
        when (reason) {
            "edit" -> {
                details = viewModel.editCustomerAddress!!
                fillDataFromEdit()
            }

            "map" -> {
                mapAddress = viewModel.selectedMapAddress!!
                fillDataFromMap()
            }

            else -> {}
        }
    }

    private fun saveAddress() {
        if (reason == "new") {
            details = CustomerAddressV2(
                binding.etAddress.text.toString(),
                binding.etAppartment.text.toString(),
                binding.etCity.text.toString(),
                binding.etCountry.text.toString(),
                binding.etPhone.text.toString(),
                binding.etFullName.text.toString(),
                true,
            )
            details.id = details.hashCode().toString()
        } else if (reason == "map") {
            details = CustomerAddressV2(
                binding.etAddress.text.toString(),
                binding.etAppartment.text.toString(),
                binding.etCity.text.toString(),
                binding.etCountry.text.toString(),
                binding.etPhone.text.toString(),
                binding.etFullName.text.toString(),
                true,
            )
            details.id = details.hashCode().toString()
        } else {
            details = viewModel.editCustomerAddress!!
            details.name = binding.etFullName.text.toString()
            details.address1 = binding.etAddress.text.toString()
            details.address2 = binding.etAppartment.text.toString()
            details.city = binding.etCity.text.toString()
            details.country = binding.etCountry.text.toString()
            details.phone = binding.etPhone.text.toString()
            details.isNew = false
        }
        viewModel.addAddress(details!!)
    }

    private fun checkIfValidAddress(): Boolean {
        var validAddress = true
        binding.apply {
            if (etFullName.text.isEmpty()) {
                validAddress = false
                etFullName.error = resources.getString(R.string.ErrorName)
            }
            if (etAddress.text.isEmpty()) {
                validAddress = false
                etAddress.error = resources.getString(R.string.ErrorAddress)
            }
            if (etAppartment.text.isEmpty()) {
                validAddress = false
                etAppartment.error = resources.getString(R.string.ErrorApartment)
            }
            if (etCity.text.isEmpty()) {
                validAddress = false
                etCity.error = resources.getString(R.string.ErrorCity)
            }
            if (etCountry.text.isEmpty()) {
                validAddress = false
                etCountry.error = resources.getString(R.string.ErrorCountry)
            } else if (countries.none { it.equals(etCountry.text.toString(), true) }) {
                validAddress = false
                etCountry.error = resources.getString(R.string.ErrorCountry)
            }
            if (etPhone.text.isEmpty() || etPhone.text.length < 5) {
                validAddress = false
                etPhone.error = resources.getString(R.string.ErrorPhone)
            } else if (!etPhone.text.toString().all { it.isDigit() }) {
                validAddress = false
                etPhone.error = resources.getString(R.string.ErrorPhone)
            }
        }
        return validAddress
    }

    private fun fillDataFromEdit() {
        binding.apply {
            etFullName.setText(details.name)
            etPhone.setText(details.phone)
            etAddress.setText(details.address1)
            etAppartment.setText(details.address2)
            etCity.setText(details.city)
            etCountry.setText(details.country)

        }
    }

    private fun fillDataFromMap() {
        binding.apply {
            val address = mapAddress.address
            Log.i("ahmedelsayed", "fillDataFromMap: "+address.house_number)
            val address1 =
                (if (address.house_number.isNullOrEmpty()) "" else address.house_number) + " " + (if (address.road != "null") address.road else "")
            etAddress.setText(address1)
            etCity.setText(address.city)
            etCountry.setText(address.country)
        }
    }

}
