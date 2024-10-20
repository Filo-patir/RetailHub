package com.iti4.retailhub.features.productdetails.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.logic.extractNumbersFromString
import com.iti4.retailhub.models.CountryCodes
import com.iti4.retailhub.type.CustomerInput
import com.iti4.retailhub.type.MetafieldInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(private val repository: IRepository): ViewModel() {
    private val _productDetails = MutableStateFlow<ApiState>(ApiState.Loading)
    val productDetails = _productDetails
    private val _createDraftOrder = MutableStateFlow<ApiState>(ApiState.Loading)
    val createDraftOrder = _createDraftOrder
    private val _customerDraftOrders = MutableStateFlow<ApiState>(ApiState.Loading)
    val customerDraftOrders = _customerDraftOrders
    private val _saveProductToFavortes = MutableStateFlow<ApiState>(ApiState.Loading)
    val saveProductToFavortes = _saveProductToFavortes
    private val _productInFavorites = MutableStateFlow<ApiState>(ApiState.Loading)
    val productInFavorites = _productInFavorites
    val customerId by lazy {repository.getUserShopLocalId()}

    fun getProductDetails(id:String) {
        viewModelScope.launch(Dispatchers.IO){
            repository.getProductDetails(id)
                .catch {
                    e -> _productDetails.emit(ApiState.Error(e))
                }
                .collect{
                    _productDetails.emit(ApiState.Success(it))
            }
        }
    }

    fun  getDraftOrdersByCustomer(productTitle:String){
    val customerIdNumberOnly = extractNumbersFromString(customerId!!)
    viewModelScope.launch(Dispatchers.IO){
        repository.getDraftOrdersByCustomer("(customer_id:${customerIdNumberOnly}) ${productTitle}")
            .catch {
                    e -> _customerDraftOrders.emit(ApiState.Error(e))
            }
            .collect{
                _customerDraftOrders.emit(ApiState.Success(it))
    }
}
}

    fun addToCart(selectedProductVariantId: String) {

        viewModelScope.launch(Dispatchers.IO){
            /*if (customerId != null) {*/
                repository.insertMyBagItem(selectedProductVariantId,customerId!!)
                    .catch { e ->
                        _createDraftOrder.emit(ApiState.Error(e))
                    }
                    .collect{
                        _createDraftOrder.emit(ApiState.Success(it))
                    }
//            }
        }
    }

    fun saveToFavorites(
        productId: String,variantID:String,
        productTitle: String,
        selectedImage: String,
        price: String
    ) {
        viewModelScope.launch(Dispatchers.IO){

            repository.saveProductToFavotes(CustomerInput(
                id = Optional.present(customerId),
                metafields = Optional.present(
                    listOf(
                        MetafieldInput(
                            namespace= Optional.present(productId),
                            key = Optional.present("favorites"),
                            value = Optional.present(productId),
                            type = Optional.present("string"),
                            description = Optional.present("${productTitle},${selectedImage},${price}")
            )
                    )
                )))
                .catch { e ->
                    _saveProductToFavortes.emit(ApiState.Error(e))
                }
                .collect{
                    _saveProductToFavortes.emit(ApiState.Success(it))
                }
        }
    }

    fun searchProductInCustomerFavorites(selectedProductVariantId: String) {
        /*val regex = """\/([^\/]+)$""".toRegex()
        val matchResult = regex.find(selectedProductVariantId)
        val id = selectedProductVariantId.split("/").last()*/
        viewModelScope.launch(Dispatchers.IO){
            /*if (customerId != null) {*/
            repository.getCustomerFavoritesoById(customerId!!,selectedProductVariantId.toString())
                .catch { e ->
                    _productInFavorites.emit(ApiState.Error(e))
                }
                .collect{
                    _productInFavorites.emit(ApiState.Success(it))
                }
//            }
        }
    }






    fun getConversionRates(currencyCode: CountryCodes): Double {
        return repository.getConversionRates(currencyCode)
    }

    fun getCurrencyCode(): CountryCodes {
        return repository.getCurrencyCode()
    }
}


