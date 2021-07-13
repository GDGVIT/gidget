package com.dscvit.gidget.models.activity.commonDataClasses

import com.google.gson.annotations.SerializedName

data class Payload(
    @SerializedName("action") val action: String? = null,
    @SerializedName("commits") val commits: Array<Commits>? = null,
    @SerializedName("forkee") val forkee: Forkee? = null,
    @SerializedName("issue") val issue: Issue? = null,
    @SerializedName("pull_request") val pull_request: PullRequest? = null,
    @SerializedName("review") val review: Review? = null,
    @SerializedName("comment") val comment: Comment? = null,
    @SerializedName("release") val release: Release? = null,
    @SerializedName("member") val member: Member? = null
)
