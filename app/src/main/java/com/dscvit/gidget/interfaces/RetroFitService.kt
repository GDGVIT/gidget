package com.dscvit.gidget.interfaces

import com.dscvit.gidget.models.feedPage.FeedPageModel
import com.dscvit.gidget.models.profilePage.ProfilePageModel
import com.dscvit.gidget.models.searchPage.SearchPageRepoModel
import com.dscvit.gidget.models.searchPage.SearchPageUserModel
import com.dscvit.gidget.models.widget.WidgetRepoModel
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

    @GET("repos/{owner}/{repo}/events")
    fun widgetRepoEvents(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Authorization") token: String
    ): Call<MutableList<WidgetRepoModel>>

    @GET("users/{user}/events")
    fun widgetUserEvents(
        @Path("user") user: String,
        @Header("Authorization") token: String
    ): Call<MutableList<WidgetRepoModel>>
}
