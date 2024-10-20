package com.iti4.retailhub.features.address

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.logic.toCustomerAddressList
import com.iti4.retailhub.models.CustomerAddressV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AddressViewModel @Inject constructor(private val repository: IRepository) : ViewModel() {
    private val TAG: String = "AddressViewModel"
    private val dispatcher = Dispatchers.IO
    val customerId by lazy { repository.getUserShopLocalId() }

    init {
        getAddressesById()
    }

    // list to hold customer's addresses locally
    var addressesList: MutableList<CustomerAddressV2> = mutableListOf()

    // send a CustomerAddressV2 for editing either map or edit
    var editCustomerAddress: CustomerAddressV2? = null
    var selectedMapAddress: PlaceLocation? = null

    private val _defaultAddressState = MutableStateFlow<ApiState>(ApiState.Loading)
    val defaultAddressState = _defaultAddressState.asStateFlow()


    private val _addressLookUp = MutableStateFlow<ApiState>(ApiState.Loading)
    val addressLookUp = _addressLookUp.asStateFlow()

    private val _addressGeocoding = MutableStateFlow<ApiState>(ApiState.Loading)
    val addressGeocoding = _addressGeocoding.asStateFlow()


     fun setState() {
        viewModelScope.launch(dispatcher) {
            _addressGeocoding.emit(ApiState.Loading)
        }
    }

    private fun getAddressesById() {
        viewModelScope.launch(dispatcher) {
            repository.getAddressesById(customerId!!)
                .catch { e ->
                    Log.i(
                        TAG,
                        "AddressViewModel : getAddressesById: on error ${e.message}"
                    )
                }.collect {
                    addressesList =
                        it.toCustomerAddressList()
                    getDefaultAddress()
                }
        }
    }

    fun getDefaultAddress() {
        viewModelScope.launch(dispatcher) {
            repository.getDefaultAddress(customerId!!)
                .catch { e -> _defaultAddressState.emit(ApiState.Error(e)) }.collect {
                    _defaultAddressState.emit(ApiState.Success(it))
                }
        }
    }

    fun addAddress(address: CustomerAddressV2) {
        if (address.isNew) {
            addressesList.add(address)
        } else {
            val index = addressesList.indexOfFirst { it.id == address.id }
            addressesList[index] = address
        }
    }


    fun getLocationSuggestions(query: String) {
        viewModelScope.launch(dispatcher) {
            repository.getLocationSuggestions(query)
                .catch { e ->
                    _addressLookUp.emit(ApiState.Error(e))
                }
                .collect {
                    if (it.isSuccessful && it.body() != null) {
                        _addressLookUp.emit(ApiState.Success(it.body()))
                    } else {
                        _addressLookUp.emit(ApiState.Error(Exception("error")))
                    }
                }
        }
    }

    fun getLocationGeocoding(lat: String, lon: String) {
        viewModelScope.launch(dispatcher) {
            repository.getLocationGeocoding(lat, lon)
                .catch { e ->
                    _addressGeocoding.emit(ApiState.Error(e))
                }
                .collect {
                    if (it.isSuccessful && it.body() != null) {
                        _addressGeocoding.emit(ApiState.Success(it.body()))
                    } else {
                        _addressGeocoding.emit(ApiState.Error(Exception("error")))
                    }
                }
        }
    }

    fun updateMyAddresses(address: List<CustomerAddressV2>) {
        Log.i(TAG, "updateMyAddresses: Step1  $address")
        GlobalScope.launch(dispatcher) {
            repository.updateCustomerAddress(customerId!!, address)
                .catch { Log.i(TAG, "updateMyAddresses: on error ${it.message}") }
                .collect { data ->
                    Log.i(TAG, "updateMyAddresses: Step2 collect  $address")

                    address.forEach {
                        if (it.isDefault) {
                            val addresss =
                                data.customer!!.addresses.filter { insideData -> it.address1 == insideData.address1 }
                            updateCustomerDefaultAddress(addresss[0].id)
                        }
                    }
                }
        }
    }

    private fun updateCustomerDefaultAddress(addressId: String) {
        GlobalScope.launch(dispatcher) {
            repository.updateCustomerDefaultAddress(customerId!!, addressId)
                .catch { Log.i(TAG, "updateCustomerDefaultAddress: on error ${it.message}") }
                .collect {
                    Log.i(
                        TAG,
                        "updateCustomerDefaultAddress: step3 update defaul ${it.defaultAddress}"
                    )
                }
        }
    }


}