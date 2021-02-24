package com.rishav.gidget.Interface

import com.rishav.gidget.Models.FeedPage.FeedPageModel
import com.rishav.gidget.Models.ProfilePage.ProfilePageModel
import retrofit2.Call
import retrofit2.http.GET

interface RetroFitService {
    @GET("rishavnaskar/received_events")
    fun getActivityList(): Call<MutableList<FeedPageModel>>

    @GET("rishavnaskar")
    fun getProfileInfo() : Call<ProfilePageModel>
}