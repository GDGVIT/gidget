package com.dscvit.gidget.common

import com.dscvit.gidget.BuildConfig

class Security {
    companion object {
        fun getToken(): String = BuildConfig.TOKEN
    }
}
