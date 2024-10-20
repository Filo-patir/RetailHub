package com.iti4.retailhub.features.favorits.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.models.CountryCodes
import com.iti4.retailhub.type.MetafieldDeleteInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(private val repository: IRepository): ViewModel() {
    private val _savedFavortes = MutableStateFlow<ApiState>(ApiState.Loading)
    val savedFavortes = _savedFavortes
    private val _deletedFavortes = MutableStateFlow<ApiState>(ApiState.Loading)
    val deletedFavortes = _deletedFavortes
    val customerId by lazy {repository.getUserShopLocalId()}

    fun  getFavorites(){
        viewModelScope.launch(Dispatchers.IO){

            //handle this customer id
            repository.getCustomerFavoritesoById(customerId!!,
               "")
                .catch {
                        e -> _savedFavortes.emit(ApiState.Error(e))
                }
                .collect{
                    _savedFavortes.emit(ApiState.Success(it))
                }
        }
    }
    fun  deleteFavorites(id:String){
        viewModelScope.launch(Dispatchers.IO){
            repository.deleteCustomerFavoritItem(MetafieldDeleteInput(
                id = id
            ))
                .catch {
                        e -> _deletedFavortes.emit(ApiState.Error(e))
                }
                .collect{
                    _deletedFavortes.emit(ApiState.Success(it))
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