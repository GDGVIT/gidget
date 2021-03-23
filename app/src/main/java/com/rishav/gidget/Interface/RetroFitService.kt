package com.rishav.gidget.Interface

import com.rishav.gidget.Models.FeedPage.FeedPageModel
import com.rishav.gidget.Models.ProfilePage.ProfilePageModel
import com.rishav.gidget.Models.SearchPage.SearchPageRepoModel
import com.rishav.gidget.Models.SearchPage.SearchPageUserModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface RetroFitService {
    @GET("users/{user}/received_events")
    fun getActivityList(
            @Path("user") user: String,
            @Header("Authorization") token: String
    ): Call<MutableList<FeedPageModel>>

    @GET("users/{user}")
    fun getProfileInfo(
            @Path("user") user: String,
            @Header("Authorization") token: String
    ): Call<ProfilePageModel>

    @GET("search/users")
    fun searchUser(
            @Query("q") q: String,
            @Header("Authorization") token: String
    ): Call<SearchPageUserModel>

    @GET("search/repositories")
    fun searchRepo(
            @Query("q") q: String,
            @Header("Authorization") token: String
    ): Call<SearchPageRepoModel>
}
