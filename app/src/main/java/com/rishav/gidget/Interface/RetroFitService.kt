package com.rishav.gidget.Interface

import com.rishav.gidget.Models.FeedPage.FeedPageModel
import retrofit2.Call
import retrofit2.http.GET

interface RetroFitService {
//    @Headers("Authorization: 80edae6d921e4b09f4e4185c344ad108f7d84ffe")
    @GET("rishavnaskar/received_events")
    fun getActivityList(): Call<MutableList<FeedPageModel>>
}