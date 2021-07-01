package com.dscvit.gidget.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.PowerManager

class PhoneState {
    fun isPhoneActive(context: Context): Boolean {
        return try {
            val screenAwake = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            screenAwake.isInteractive
        } catch (e: Exception) {
            println(e.message)
            false
        }
    }

    fun isInternetConnected(context: Context): Boolean {
        try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } catch (e: Exception) {
            println(e.message)
            return false
        }
    }
}
