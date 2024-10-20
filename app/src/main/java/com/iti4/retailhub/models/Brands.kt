package com.iti4.retailhub.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Brands(
    val id: String,
    val title: String,
    val image: String,
    var isChecked: Boolean = false
) : Parcelable
