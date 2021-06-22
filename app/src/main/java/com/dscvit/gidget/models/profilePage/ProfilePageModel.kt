package com.dscvit.gidget.models.profilePage

import com.google.gson.annotations.SerializedName

data class ProfilePageModel(
    @SerializedName("login") val login: String? = null,
    @SerializedName("avatar_url") val avatar_url: String? = null,
    @SerializedName("gravatar_id") val gravatar_id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("followers") val followers: Int? = null,
    @SerializedName("following") val following: Int? = null,
    @SerializedName("created_at") val created_at: String? = null,
    @SerializedName("updated_at") val updated_at: String? = null
)
