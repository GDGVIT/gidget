package com.rishav.gidget.Models.SearchPage

import com.google.gson.annotations.SerializedName

data class SearchPageUserModel(
    @SerializedName("total_count") val total_count : Int,
    @SerializedName("incomplete_results") val incomplete_results : Boolean,
    @SerializedName("items") val items : List<Items>
)

data class Items (
    @SerializedName("login") val login : String,
    @SerializedName("id") val id : String,
    @SerializedName("node_id") val node_id : String,
    @SerializedName("avatar_url") val avatar_url : String,
    @SerializedName("url") val url : String,
    @SerializedName("html_url") val html_url : String,
    @SerializedName("followers_url") val followers_url : String,
    @SerializedName("following_url") val following_url : String,
    @SerializedName("gists_url") val gists_url : String,
    @SerializedName("starred_url") val starred_url : String,
    @SerializedName("subscriptions_url") val subscriptions_url : String,
    @SerializedName("organizations_url") val organizations_url : String,
    @SerializedName("repos_url") val repos_url : String,
    @SerializedName("events_url") val events_url : String,
    @SerializedName("received_events_url") val received_events_url : String,
    @SerializedName("type") val type : String,
    @SerializedName("site_admin") val site_admin : Boolean,
    @SerializedName("score") val score : Int
)