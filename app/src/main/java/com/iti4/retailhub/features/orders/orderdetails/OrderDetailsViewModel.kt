package com.iti4.retailhub.features.orders.orderdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.models.CountryCodes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(private val repository: IRepository) : ViewModel() {
    private val _orderDetails = MutableStateFlow<ApiState>(ApiState.Loading)
    val orderDetails = _orderDetails.stateIn(viewModelScope, SharingStarted.Eagerly, ApiState.Loading)
    fun getOrderDetails(orderId: String) {
        _orderDetails.value = ApiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            repository.getOrderDetails(orderId).catch { e -> _orderDetails.emit(ApiState.Error(e)) }.collect{
                _orderDetails.emit(ApiState.Success(it))
            }
        }
    }

    fun getConversionRates(currencyCode: CountryCodes): Double {
        return repository.getConversionRates(currencyCode)
    }

    fun getCurrencyCode(): CountryCodes {
        return repository.getCurrencyCode()
    }
}