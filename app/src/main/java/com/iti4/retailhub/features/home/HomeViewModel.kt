package com.iti4.retailhub.features.home

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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: IRepository) : ViewModel() {
    private val dispatcher = Dispatchers.IO
    val customerId by lazy { repository.getUserShopLocalId() }
    private val _products = MutableStateFlow<ApiState>(ApiState.Loading)
    val products = _products.onStart { getProducts() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiState.Loading)

    private val _brands = MutableStateFlow<ApiState>(ApiState.Loading)
    val brands = _brands.onStart { getBrands() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiState.Loading)

    private val _couponsState = MutableStateFlow<ApiState>(ApiState.Loading)
    val couponsState = _couponsState.onStart { getDiscount() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiState.Loading)

    private fun getBrands() {
        viewModelScope.launch(dispatcher) {
            repository.getBrands().catch { e -> _brands.emit(ApiState.Error(e)) }.collect {
                _brands.emit(ApiState.Success(it))
            }
        }
    }

    private fun getProducts() {
        viewModelScope.launch(dispatcher) {
            repository.getProducts("").catch { e -> _products.emit(ApiState.Error(e)) }.collect {
                _products.emit(ApiState.Success(it.subList(0,9)))
            }
        }
    }

    fun getConversionRates(currencyCode: CountryCodes): Double {
        return repository.getConversionRates(currencyCode)
    }

    fun getCurrencyCode(): CountryCodes {
        return repository.getCurrencyCode()
    }


    fun getDiscount() {
        viewModelScope.launch(dispatcher) {
            repository.getDiscounts().catch { e ->
                _couponsState.emit(ApiState.Error(e))
            }.collect {
                _couponsState.emit(ApiState.Success(it))
            }
        }
    }


}