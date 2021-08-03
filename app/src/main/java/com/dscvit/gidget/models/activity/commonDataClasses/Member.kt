package com.dscvit.gidget.models.activity.commonDataClasses

import com.google.gson.annotations.SerializedName

data class Member(
    @SerializedName("login") val login: String? = null
)
