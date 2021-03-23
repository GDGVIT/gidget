package com.rishav.gidget.Common

import com.rishav.gidget.Interface.RetroFitService
import com.rishav.gidget.Retrofit.RetroFitClient

object Common {
    private const val baseUrl = "https://api.github.com/"
    val retroFitService: RetroFitService
        get() = RetroFitClient.getClient(baseUrl).create(RetroFitService::class.java)
}
