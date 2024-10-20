package com.iti4.retailhub.features.checkout

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti4.retailhub.GetAddressesDefaultIdQuery
import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.datastorage.network.ApiState
import com.iti4.retailhub.features.summary.PaymentIntentResponse
import com.iti4.retailhub.features.summary.PaymentRequest
import com.iti4.retailhub.models.AddressInputModel
import com.iti4.retailhub.models.CartProduct
import com.iti4.retailhub.models.CustomerAddressV2
import com.iti4.retailhub.models.CustomerInputModel
import com.iti4.retailhub.models.Discount
import com.iti4.retailhub.models.DiscountInput
import com.iti4.retailhub.models.DraftOrderInputModel
import com.iti4.retailhub.models.toLineItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(private val repository: IRepository) : ViewModel() {
    private val TAG = "CheckoutViewModel"
    private val dispatcher = Dispatchers.IO
    lateinit var customerEmail: String
    private lateinit var customerName: String
    var selectedDiscount: Discount? = null

    private val _customerDataResponse = MutableStateFlow<ApiState>(ApiState.Loading)
    val customerResponse = _customerDataResponse.onStart { }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiState.Loading)

    private val _checkoutDraftOrderCreated = MutableStateFlow<ApiState>(ApiState.Loading)
    val checkoutDraftOrderCreated = _checkoutDraftOrderCreated.onStart { }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiState.Loading)

    private val _paymentIntentResponse = MutableStateFlow<ApiState>(ApiState.Loading)
    val paymentIntentResponse = _paymentIntentResponse.onStart { }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiState.Loading)

    private val _addressesState = MutableStateFlow<ApiState>(ApiState.Loading)
    val addressesState = _addressesState.onStart {}
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiState.Loading)

    val customerId by lazy { (repository.getUserShopLocalId()!!) }


    fun getCustomerData() {
        viewModelScope.launch(dispatcher) {
            repository.getCustomerInfoById(customerId)
                .catch { e ->
                    _customerDataResponse.emit(ApiState.Error(e))
                }
                .collect {
                    if (it.email.isNullOrEmpty())
                        _customerDataResponse.emit(ApiState.Error(Exception("invalid data")))
                    else {
                        customerName = it.firstName + it.lastName
                        customerEmail = it.email!!
                        _customerDataResponse.emit(ApiState.Success(it))
                    }

                }
        }
    }

    fun getDefaultAddress() {
        viewModelScope.launch(dispatcher) {
            repository.getDefaultAddress(customerId!!)
                .catch { e -> _addressesState.emit(ApiState.Error(e)) }.collect {
                    _addressesState.emit(ApiState.Success(it))
                }
        }
    }

    // part 1
    suspend fun deleteCartItems(listOfCartProduct: List<CartProduct>) {
        listOfCartProduct.forEach {
            repository.deleteMyBagItem(it.draftOrderId)
                .catch { e -> Log.i(TAG, "deleteCartItems: +${e.message}") }.collect {}
        }
    }

    // initlises a discount
    fun getDiscountInput(): DiscountInput? {
        return if (selectedDiscount != null) {
            DiscountInput(
                selectedDiscount!!.getDiscountAsDouble(), selectedDiscount!!.title
            )
        } else null
    }

        fun createCheckoutDraftOrder(
            listOfCartProduct: List<CartProduct>,
            isCard: Boolean,
            checkoutAddress: CustomerAddressV2?,
            checkoutDefaultAddress: GetAddressesDefaultIdQuery.DefaultAddress?
        ) {
            viewModelScope.launch(dispatcher) {
                try {
                    val draftOrderInputModel = createDraftOrderInputModel(
                        listOfCartProduct,
                        checkoutAddress,
                        checkoutDefaultAddress
                    )

                    // Delete items from the bag
                    deleteCartItems(listOfCartProduct)

                    // Create the draft order and handle checkout
                    handleCheckoutProcess(draftOrderInputModel, isCard)

                } catch (e: Exception) {
                    _checkoutDraftOrderCreated.emit(ApiState.Error(e))
                }
            }
        }

    // creates an input for the main method
    fun createDraftOrderInputModel(
        listOfCartProduct: List<CartProduct>,
        checkoutAddress: CustomerAddressV2?,
        checkoutDefaultAddress: GetAddressesDefaultIdQuery.DefaultAddress?
    ): DraftOrderInputModel {
        val lineItems = listOfCartProduct.map { it.toLineItem() }
        val discount = getDiscountInput()
        val customerInputModel = CustomerInputModel(customerId, customerEmail)
        val addressInputModel =
            getAddressInputModel(checkoutAddress, checkoutDefaultAddress, customerInputModel)
        return DraftOrderInputModel(
            lineItems,
            customerInputModel,
            addressInputModel,
            customerEmail,
            discount,
            false
        )
    }

    fun getAddressInputModel(
        checkoutAddress: CustomerAddressV2?,
        checkoutDefaultAddress: GetAddressesDefaultIdQuery.DefaultAddress?,
        customerInputModel: CustomerInputModel
    ): AddressInputModel {
        return if (checkoutAddress != null) {
            customerInputModel.firstName = checkoutAddress.name
            customerInputModel.phone = checkoutAddress.phone
            AddressInputModel(
                checkoutAddress.address1!!,
                checkoutAddress.address2!!,
                checkoutAddress.city!!,
                checkoutAddress.country!!,
                ""
            )
        } else {
            customerInputModel.firstName = checkoutDefaultAddress!!.name
            customerInputModel.phone = checkoutDefaultAddress!!.phone
            AddressInputModel(
                checkoutDefaultAddress!!.address1!!,
                checkoutDefaultAddress!!.address2!!,
                checkoutDefaultAddress!!.city!!,
                checkoutDefaultAddress!!.country!!,
                ""
            )
        }
    }


    suspend fun handleCheckoutProcess(
        draftOrderInputModel: DraftOrderInputModel,
        isCard: Boolean
    ) {
        repository.createCheckoutDraftOrder(draftOrderInputModel)
            .catch { e ->
                _checkoutDraftOrderCreated.emit(ApiState.Error(e))
            }
            .collect { draftId ->
                completeCheckout(draftId.draftOrder!!.id, isCard)
            }
    }

    suspend fun completeCheckout(
        draftOrderId: String,
        isCard: Boolean
    ) {
        repository.emailCheckoutDraftOrder(draftOrderId)
            .catch { }
            .collect {
                repository.completeCheckoutDraftOrder(draftOrderId)
                    .catch { }
                    .collect { response ->
                        setCustomerUsedDiscount()
                        markAsPaidIfCard(response.order!!.id, isCard)
                        finalizeOrder(response.id)
                    }
            }
    }

    suspend fun setCustomerUsedDiscount() {
        selectedDiscount?.let {
            repository.setCustomerUsedDiscounts(customerId, selectedDiscount!!.title)
                .catch { }
                .collect { }
        }
    }

    // 8. Mark order as paid if card payment is selected
    suspend fun markAsPaidIfCard(orderId: String, isCard: Boolean) {
        if (isCard) {
            repository.markOrderAsPaid(orderId)
                .catch { }
                .collect { }
        }
    }

    // 9. Finalize the order and emit success
    suspend fun finalizeOrder(orderId: String) {
        repository.deleteMyBagItem(orderId)
            .catch { e -> _checkoutDraftOrderCreated.emit(ApiState.Error(e)) }.collect {
                _checkoutDraftOrderCreated.emit(ApiState.Success(it))
            }
    }


    fun createPaymentIntent(orderPrice: Int) {
        viewModelScope.launch(dispatcher) {
            repository.createStripePaymentIntent(
                PaymentRequest(
                    customerEmail, customerName, orderPrice, repository.getCurrencyCode().toString()
                )
            ).collect {
                val response = it
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    if (responseBody != null) {
                        val jsonObject = JSONObject(responseBody)
                        if (jsonObject.has("clientSecret")) {
                            _paymentIntentResponse.value = ApiState.Success(
                                PaymentIntentResponse(
                                    jsonObject.getString("clientSecret"),
                                    jsonObject.getString("dpmCheckerLink"),
                                    jsonObject.getString("ephemeralKey"),
                                    jsonObject.getString("customer")
                                )
                            )
                        } else {
                            ApiState.Error(throw Exception(jsonObject.getString("error")))
                        }
                    }
                } else {
                    ApiState.Error(throw Exception())
                }
            }
        }
    }


}