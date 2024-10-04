package com.iti4.retailhub.features.mybag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.network.ApiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyBagViewModel @Inject constructor(private val repository: IRepository) : ViewModel() {
    private val dispatcher = Dispatchers.IO
    private val _myBagProducts = MutableStateFlow<ApiState>(ApiState.Loading)
    val products = _myBagProducts.onStart { getMyBagProducts() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiState.Loading)

    private val _myBagProductsRemove = MutableStateFlow<ApiState>(ApiState.Loading)
    val myBagProductsRemove = _myBagProductsRemove.asStateFlow()

    private fun getMyBagProducts() {
        viewModelScope.launch(dispatcher) {
            repository.getMyBagProducts("customer_id:6945540964394")
                .catch { e -> _myBagProducts.emit(ApiState.Error(e)) }.collect {
                    _myBagProducts.emit(ApiState.Success(it))
                }
        }
    }

    fun deleteMyBagItem(itemId: String) {
        viewModelScope.launch(dispatcher) {
            repository.deleteMyBagItem(itemId)
                .catch { e -> _myBagProductsRemove.emit(ApiState.Error(e)) }.collect {
                    _myBagProductsRemove.emit(ApiState.Success(it))
                }
        }
    }


}