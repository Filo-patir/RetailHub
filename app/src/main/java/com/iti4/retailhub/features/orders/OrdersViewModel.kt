package com.iti4.retailhub.features.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.logic.extractNumbersFromString
import com.iti4.retailhub.models.CountryCodes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(private val repository: IRepository) : ViewModel() {
    private val dispatcher by lazy { Dispatchers.IO }
    private val _orders = MutableStateFlow<ApiState>(ApiState.Loading)
    val orders = _orders.onStart { getOrders() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiState.Loading)
    val userId by lazy {repository.getUserShopLocalId()}

    fun getOrders(){
        viewModelScope.launch(dispatcher){
            if (userId != null) {
                repository.getOrders("customer_id:${extractNumbersFromString(userId!!)}").catch { e -> _orders.emit(ApiState.Error(e)) }.collect{
                    _orders.value = ApiState.Success(it)
                }
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