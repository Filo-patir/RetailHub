package com.iti4.retailhub.features.address

import com.iti4.retailhub.models.CustomerAddress
import com.iti4.retailhub.models.CustomerAddressV2

interface OnClickAddress {
    fun editDetails(address: CustomerAddressV2)
    fun deleteAddress(id: String)
    fun setDefaultAddress(addressIndex: Int)
    fun checkoutClickedAnAddress(address: CustomerAddressV2)
}