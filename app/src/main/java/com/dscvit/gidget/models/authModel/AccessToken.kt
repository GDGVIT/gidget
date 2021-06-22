package com.dscvit.gidget.models.authModel

import com.google.gson.annotations.SerializedName

data class AccessToken(
    @SerializedName("access_token") val access_token: String? = null,
    @SerializedName("token_type") val token_type: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("error_description") val error_description: String? = null,
    @SerializedName("error_uri") val error_uri: String? = null,
)
