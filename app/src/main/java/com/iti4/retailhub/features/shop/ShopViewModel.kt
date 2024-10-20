package com.iti4.retailhub.features.shop

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
class ShopViewModel @Inject constructor(private val repository: IRepository) : ViewModel() {
    private val dispatcher = Dispatchers.IO
    private val _categoriesList = MutableStateFlow<ApiState>(ApiState.Loading)
    val categoriesList = _categoriesList.onStart { getCategoriesList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiState.Loading)

    fun getCategoriesList() {
        viewModelScope.launch(dispatcher) {
            repository.getProductTypesOfCollection()
                .catch { e -> _categoriesList.emit(ApiState.Error(e)) }.collect {
                _categoriesList.emit(ApiState.Success(it))
            }
        }
    }


}
