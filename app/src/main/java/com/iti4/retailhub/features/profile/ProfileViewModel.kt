package com.iti4.retailhub.features.profile

import androidx.lifecycle.ViewModel
import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.models.CountryCodes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: IRepository) : ViewModel() {

    fun setCurrencyCode(currencyCode: CountryCodes) {
        repository.setCurrencyCode(currencyCode)
    }


}