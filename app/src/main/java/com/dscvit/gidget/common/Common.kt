package com.dscvit.gidget.common

import com.dscvit.gidget.interfaces.RetroFitService
import com.dscvit.gidget.retrofit.RetroFitClient

object Common {
    private const val baseUrl = "https://api.github.com/"
    val retroFitService: RetroFitService
        get() = RetroFitClient.getClient(baseUrl).create(RetroFitService::class.java)
}
