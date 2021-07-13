package com.dscvit.gidget.models.activity.commonDataClasses

import com.google.gson.annotations.SerializedName

data class Issue(
    @SerializedName("html_url") val html_url: String? = null,
    @SerializedName("title") val title: String? = null
)
