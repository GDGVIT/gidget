package com.rishav.gidget.common

import com.rishav.gidget.interfaces.RetroFitService
import com.rishav.gidget.retrofit.RetroFitClient

object Common {
    private const val baseUrl = "https://api.github.com/"
    val retroFitService: RetroFitService
        get() = RetroFitClient.getClient(baseUrl).create(RetroFitService::class.java)
}
