package com.rishav.gidget.Common

import com.rishav.gidget.BuildConfig

class Security {
    companion object {
        fun getToken(): String = BuildConfig.TOKEN
    }
}
