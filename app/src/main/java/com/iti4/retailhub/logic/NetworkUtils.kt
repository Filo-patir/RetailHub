package com.iti4.retailhub.logic
import android.content.Context
import android.net.ConnectivityManager

class NetworkUtils (private val context: Context) : INetworkUtils {
    override fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities != null
    }
}

interface INetworkUtils {
    fun isNetworkAvailable(): Boolean
}
