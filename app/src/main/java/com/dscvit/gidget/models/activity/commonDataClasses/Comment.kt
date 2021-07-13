package com.dscvit.gidget.models.activity.commonDataClasses

import com.google.gson.annotations.SerializedName

data class Comment(
    @SerializedName("html_url") val html_url: String? = null,
    @SerializedName("body") val body: String? = null
)
