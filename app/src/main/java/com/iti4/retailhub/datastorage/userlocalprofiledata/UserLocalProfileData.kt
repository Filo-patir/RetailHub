package com.iti4.retailhub.datastorage.userlocalprofiledata

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iti4.retailhub.models.CountryCodes
import java.time.LocalDateTime

class UserLocalProfileData(context: Context) : UserLocalProfileDataInterface {

    private val profileData: SharedPreferences =
        context.getSharedPreferences("ProfileData", Context.MODE_PRIVATE)
    private val profileDataEditor: SharedPreferences.Editor = profileData.edit()
    private val userShopID: SharedPreferences =
        context.getSharedPreferences("UserShopID", Context.MODE_PRIVATE)
    private val userShopIDEditor: SharedPreferences.Editor = userShopID.edit()


    private val currencyData: SharedPreferences =
        context.getSharedPreferences("currencyData", Context.MODE_PRIVATE)
    private val currencyDataEditor: SharedPreferences.Editor = currencyData.edit()

    override fun getFirstTime(): Boolean {
        return currencyData.getBoolean("firstTime", true)
    }

    private val loginStatus: SharedPreferences =
        context.getSharedPreferences("loginStatus", Context.MODE_PRIVATE)
    private val loginStatusEditor: SharedPreferences.Editor = loginStatus.edit()

    override fun addUserName(name: String): Int {
        if (name.isEmpty()) {
            return 0
        } else {
            profileDataEditor.putString("Name", name)
            profileDataEditor.apply()
            return 1
        }
    }

    override fun setFirstTime() {
        currencyDataEditor.putBoolean("firstTime", false)
        currencyDataEditor.apply()
    }

    override fun getShouldIRefrechCurrency(): Boolean {
        val storedDay = currencyData.getInt("dayOfTheWeek", 0)
        val today = LocalDateTime.now().dayOfWeek.value
        if (storedDay != today)
            return true
        else
            return false
    }

    override fun setRefrechCurrency() {
        currencyDataEditor.putInt("dayOfTheWeek", LocalDateTime.now().dayOfWeek.value)
        currencyDataEditor.apply()
    }


    override fun saveConversionRates(conversion_rates: Map<String, Double>) {
        val gson = Gson()
        val json = gson.toJson(conversion_rates)
        currencyDataEditor.putString("conversionRates", json)
        currencyDataEditor.apply()
    }

    override fun getConversionRates(): Map<String, Double>? {
        val json = currencyData.getString("conversionRates", null)
        return if (json != null) {
            val gson = Gson()
            val type = object : TypeToken<Map<String, Double>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    override fun setCurrencyCode(currencyCode: CountryCodes) {
        currencyDataEditor.putString("currencyCode", currencyCode.name)
        currencyDataEditor.commit()
    }

    override fun getCurrencyCode(): CountryCodes {
        val stringCode = currencyData.getString("currencyCode", CountryCodes.EGP.name)
        return getCountryCode(stringCode!!)
    }

    private fun getCountryCode(code: String): CountryCodes {
        return try {
            enumValueOf<CountryCodes>(code)
        } catch (e: IllegalArgumentException) {
            return CountryCodes.EGP
        }
    }

    override fun addUserShopLocalId(id: String?) {
        profileDataEditor.putString("ID", id)
        profileDataEditor.apply()
    }

    override fun getUserShopLocalId(): String? {
        return profileData.getString("ID", null)
    }
    override fun setLoginStatus(loginStatus: String) {
        loginStatusEditor.putString("LoginStatus", loginStatus)
        loginStatusEditor.apply()
    }
    override fun getLoginStatus(): String? {
        return loginStatus.getString("LoginStatus", null)
    }

    override fun addUserData(userID: String): Int {
        profileDataEditor.putString("UserID", userID)
        profileDataEditor.apply()
        return 1
    }
    override fun getUserProfileData(): String {
        val name = profileData.getString("Name", null)
        val userID = profileData.getString("UserID", null)
        val email = profileData.getString("Email", null)
        val profilePictureURl = profileData.getString("ProfilePictureURL", null)
        return "$name,$userID,$email,$profilePictureURl"
    }
    override fun deleteUserData() {
        profileDataEditor.clear()
        loginStatusEditor.clear()
        profileDataEditor.apply()
        loginStatusEditor.apply()
    }
}