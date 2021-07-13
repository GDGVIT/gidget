package com.dscvit.gidget.models.activity.commonDataClasses

import com.google.gson.annotations.SerializedName

data class Commits(
    @SerializedName("sha") val sha: String? = null,
    @SerializedName("message") val message: String? = null
)
