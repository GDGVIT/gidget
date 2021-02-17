package com.rishav.gidget.Common

import com.rishav.gidget.Interface.RetroFitService
import com.rishav.gidget.Retrofit.RetroFitClient

object Common {
    private const val BASE_URL = "https://api.github.com/users/"

    val retroFitService: RetroFitService
        get() = RetroFitClient.getClient(BASE_URL).create(RetroFitService::class.java)
}