package com.iti4.retailhub.features.shop.search.filter.bottomsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.network.ApiState
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
class BrandsBottomSheetViewModel @Inject constructor(private val repository: IRepository) : ViewModel() {
    private val _brands = MutableStateFlow<ApiState>(ApiState.Loading)
    val brands = _brands.onStart { getBrands() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),ApiState.Loading)

    private fun getBrands() {
        viewModelScope.launch(Dispatchers.IO){
            repository.getBrands().catch { _brands.emit(ApiState.Error(it)) }.collect{
                _brands.emit(ApiState.Success(it))
            }
        }
    }
}