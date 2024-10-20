package com.iti4.retailhub.datastorage.userlocalprofiledata

import com.iti4.retailhub.models.CountryCodes

interface UserLocalProfileDataInterface {
    fun addUserName(name: String): Int
    fun addUserData(userID: String): Int
    fun getUserProfileData(): String
    fun deleteUserData()
    fun addUserShopLocalId(id: String?)
    fun getUserShopLocalId(): String?
    fun saveConversionRates(conversion_rates: Map<String, Double>)
    fun getConversionRates(): Map<String, Double>?
    fun setCurrencyCode(currencyCode: CountryCodes)
    fun getCurrencyCode(): CountryCodes
    fun getFirstTime(): Boolean
    fun setFirstTime()
    fun getShouldIRefrechCurrency(): Boolean
    fun setRefrechCurrency()
    fun setLoginStatus(loginStatus: String)
    fun getLoginStatus(): String?

}