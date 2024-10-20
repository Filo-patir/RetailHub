package com.iti4.retailhub.features.checkout

import com.iti4.retailhub.models.Discount

interface OnClickApply {
    fun onClickApply(discount: Discount)

}