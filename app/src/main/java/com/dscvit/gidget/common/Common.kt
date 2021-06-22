package com.dscvit.gidget.common

import com.dscvit.gidget.interfaces.AuthService
import com.dscvit.gidget.interfaces.RetroFitService
import com.dscvit.gidget.retrofit.AuthRetroFitClient
import com.dscvit.gidget.retrofit.RetroFitClient

object Common {
    private const val baseUrl = "https://api.github.com/"
    val retroFitService: RetroFitService
        get() = RetroFitClient.getClient(baseUrl).create(RetroFitService::class.java)

    private const val authUrl = "https://github.com/"
    val authService: AuthService
        get() = AuthRetroFitClient.getClient(authUrl).create(AuthService::class.java)
}
