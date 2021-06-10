package com.rishav.gidget.common

import com.rishav.gidget.BuildConfig

class Security {
    companion object {
        fun getToken(): String = BuildConfig.TOKEN
    }
}
