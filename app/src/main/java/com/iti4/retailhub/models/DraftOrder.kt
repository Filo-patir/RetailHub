package com.iti4.retailhub.models

// Customer input (either by id or basic info)
data class CustomerInputModel(
    val id: String? = null,
    var firstName: String? = null,
    var phone: String? = null,
    val email: String? = null
)

// Shipping / Billing address input
data class AddressInputModel(
    val address1: String,
    val address2: String,
    val city: String,
    val country: String,
    val zip: String
)

// Discount input
data class DiscountInput(
    val value: Double,
    val valueType: String // Can be "percentage" or "fixedAmount"
)

data class LineItemInputModel(
    val variantId: String,
    val quantity: Int
)


// Draft order input with all possible fields
data class DraftOrderInputModel(
    val lineItems: List<LineItemInputModel>,
    val customer: CustomerInputModel? = null,
    val shippingAddress: AddressInputModel? = null,
    val email: String? = null,
    val appliedDiscount: DiscountInput? = null,
    val useCustomerDefaultAddress: Boolean = false,
)