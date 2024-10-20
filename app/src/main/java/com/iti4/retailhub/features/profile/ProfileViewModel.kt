package com.iti4.retailhub.features.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti4.retailhub.datastorage.IRepository
import com.iti4.retailhub.models.CountryCodes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: IRepository) : ViewModel() {
    private val _user = MutableSharedFlow<Map<String,String?>>()
    val user = _user.onStart { getCustomer() }
    fun setCurrencyCode(currencyCode: CountryCodes) {
        repository.setCurrencyCode(currencyCode)
    }

    fun getCurrencyCode(): CountryCodes {
        return repository.getCurrencyCode()
    }
    fun signOut() {
        repository.loginOut()
        repository.deleteUserData()
    }

    private fun getCustomer() {
            viewModelScope.launch(Dispatchers.IO) {
                repository.getCustomerInfoById(repository.getUserShopLocalId()!!).catch { _user.emit(mapOf("error" to it.message)) }.collect {
                    val map = mapOf("fName" to it.firstName, "lName" to it.lastName, "email" to it.email)
                    Log.d("Filo", "getCustomer: $map")
                    _user.emit(map)
                }
            }
    }
}