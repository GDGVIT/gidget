package com.dscvit.gidget.models.widget

import com.google.gson.annotations.SerializedName

data class WidgetRepoModel(
    @SerializedName("id") val id: Long,
    @SerializedName("type") val type: String,
    @SerializedName("actor") val actor: Actor,
    @SerializedName("repo") val repo: Repo,
    @SerializedName("public") val public: Boolean,
    @SerializedName("created_at") val created_at: String,
    @SerializedName("payload") val payload: Payload? = null,
    @SerializedName("org") val org: Org? = null
)

data class Actor(
    @SerializedName("id") val id: Int,
    @SerializedName("login") var login: String,
    @SerializedName("display_login") val display_login: String,
    @SerializedName("url") val url: String,
    @SerializedName("avatar_url") val avatar_url: String
)

data class Repo(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
)

data class Org(
    @SerializedName("avatar_url") val avatar_url: String? = null
)

data class Payload(
    @SerializedName("commits") val commits: Array<Commits>? = null,
    @SerializedName("forkee") val forkee: Forkee? = null,
    @SerializedName("issue") val issue: Issue? = null,
    @SerializedName("pull_request") val pull_request: PullRequest? = null,
    @SerializedName("review") val review: Review? = null,
    @SerializedName("comment") val comment: Comment? = null,

)

// Payload subclasses

data class Commits(
    @SerializedName("sha") val sha: String? = null
)

data class Forkee(
    @SerializedName("html_url") val html_url: String? = null,
)

data class Issue(
    @SerializedName("html_url") val html_url: String? = null
)

data class PullRequest(
    @SerializedName("html_url") val html_url: String? = null
)

data class Review(
    @SerializedName("html_url") val html_url: String? = null
)

data class Comment(
    @SerializedName("html_url") val html_url: String? = null
)
