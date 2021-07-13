package com.dscvit.gidget.models.activity.commonDataClasses

import com.google.gson.annotations.SerializedName

data class Forkee(
    @SerializedName("html_url") val html_url: String? = null,
)
