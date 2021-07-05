package com.dscvit.gidget.common

import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.view.LayoutInflater
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.dscvit.gidget.R
import com.dscvit.gidget.interfaces.RetroFitService
import com.dscvit.gidget.models.widget.AddToWidget
import com.dscvit.gidget.models.widget.WidgetRepoModel
import com.dscvit.gidget.widget.GidgetWidget
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Transformation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.Exception

class Utils {
    companion object {
        fun getOnWidgetItemClickedAction(): String = "onWidgetItemClicked"
        fun getUpdateWidgetAction(): String = "updateWidgetWithDatasource"
        fun getOnRefreshButtonClicked(): String = "onRefreshButtonClicked"
        fun getDeleteWidgetAction(): String = "deleteWidgetWithDatasource"
        fun getClearWidgetItems(): String = "clearWidgetItems"
        fun automaticUpdateWidget(): String = "android.appwidget.action.APPWIDGET_UPDATE"
    }

    fun addToWidget(
        mService: RetroFitService,
        isUser: Boolean,
        username: String,
        name: String,
        ownerAvatarUrl: String,
        context: Context,
    ) {
        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
        val appwidgetIDs: IntArray = appWidgetManager
            .getAppWidgetIds(ComponentName(context, GidgetWidget::class.java))
        val alertDialog: AlertDialog = alertDialog(context)
        if (appwidgetIDs.isNotEmpty()) {
            if (isUser)
                mService.widgetUserEvents(
                    username.substring(0, username.indexOf(",")),
                    "token ${Security.getToken()}"
                )
                    .enqueue(object : Callback<MutableList<WidgetRepoModel>> {
                        override fun onResponse(
                            call: Call<MutableList<WidgetRepoModel>>,
                            response: Response<MutableList<WidgetRepoModel>>
                        ) {
                            if (response.body() != null) {
                                val dataSource: ArrayList<AddToWidget> = arrayListOf()
                                for (res in response.body()!!) {
                                    val addToWidget = AddToWidget()
                                    val eventsList: List<String> = getEventMessageAndIcon(res)

                                    addToWidget.username = res.actor.login
                                    addToWidget.name = res.repo.name
                                    addToWidget.avatarUrl = res.actor.avatar_url
                                    addToWidget.icon = eventsList[1].toInt()
                                    addToWidget.message = eventsList[0]
                                    addToWidget.details = getEventDetails(res)
                                    addToWidget.date = getDate(res)
                                    addToWidget.dateISO = res.created_at
                                    addToWidget.htmlUrl = getHtmlUrl(res)

                                    dataSource.add(addToWidget)
                                }
                                if (dataSource.isNullOrEmpty())
                                    Toast.makeText(
                                        context,
                                        "No activity found for this user",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                else {
                                    saveArrayList(
                                        dataSource = dataSource,
                                        context = context,
                                        username = username,
                                        name = name,
                                        photoUrl = ownerAvatarUrl,
                                        isUser = isUser
                                    )
                                    val widgetIntent = Intent(context, GidgetWidget::class.java)
                                    widgetIntent.action = getUpdateWidgetAction()
                                    context.sendBroadcast(widgetIntent)
                                }

                                if (alertDialog.isShowing)
                                    alertDialog.dismiss()
                                Toast.makeText(context, "Added to Gidget", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                        override fun onFailure(
                            call: Call<MutableList<WidgetRepoModel>>,
                            t: Throwable
                        ) {
                            if (alertDialog.isShowing) {
                                Toast.makeText(context, "Could not add Gidget", Toast.LENGTH_LONG)
                                    .show()
                                alertDialog.dismiss()
                            }
                            println("ERROR - ${t.message}")
                        }
                    })
            else
                mService.widgetRepoEvents(
                    username.substring(0, username.indexOf(",")),
                    name,
                    "token ${Security.getToken()}"
                )
                    .enqueue(object : Callback<MutableList<WidgetRepoModel>> {
                        override fun onResponse(
                            call: Call<MutableList<WidgetRepoModel>>,
                            response: Response<MutableList<WidgetRepoModel>>
                        ) {
                            if (response.body() != null) {
                                val dataSource: ArrayList<AddToWidget> = arrayListOf()
                                for (res in response.body()!!) {
                                    val addToWidget = AddToWidget()
                                    val eventsList: List<String> = getEventMessageAndIcon(res)

                                    addToWidget.username = res.actor.login
                                    addToWidget.name = res.repo.name
                                    addToWidget.avatarUrl = res.actor.avatar_url
                                    addToWidget.icon = eventsList[1].toInt()
                                    addToWidget.message = eventsList[0]
                                    addToWidget.details = getEventDetails(res)
                                    addToWidget.date = getDate(res)
                                    addToWidget.dateISO = res.created_at
                                    addToWidget.htmlUrl = getHtmlUrl(res)

                                    dataSource.add(addToWidget)
                                }

                                if (dataSource.isNullOrEmpty())
                                    Toast.makeText(
                                        context,
                                        "No activity found for this repo",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                else {
                                    saveArrayList(
                                        dataSource = dataSource,
                                        context = context,
                                        username = username,
                                        name = name,
                                        photoUrl = ownerAvatarUrl,
                                        isUser = isUser
                                    )
                                    val widgetIntent = Intent(context, GidgetWidget::class.java)
                                    widgetIntent.action = getUpdateWidgetAction()
                                    context.sendBroadcast(widgetIntent)
                                }

                                if (alertDialog.isShowing)
                                    alertDialog.dismiss()
                                Toast.makeText(context, "Added to Gidget", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                        override fun onFailure(
                            call: Call<MutableList<WidgetRepoModel>>,
                            t: Throwable
                        ) {
                            if (alertDialog.isShowing) {
                                Toast.makeText(context, "Could not add Gidget", Toast.LENGTH_LONG)
                                    .show()
                                alertDialog.dismiss()
                            }
                            println("ERROR - ${t.message}")
                        }
                    })
        } else {
            if (alertDialog.isShowing)
                alertDialog.dismiss()
            Toast.makeText(context, "Please add Gidget to home screen", Toast.LENGTH_LONG).show()
        }
    }

    fun saveArrayList(
        dataSource: ArrayList<AddToWidget>,
        context: Context,
        username: String,
        name: String,
        photoUrl: String,
        isUser: Boolean
    ) {
        try {
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor: SharedPreferences.Editor = prefs.edit()
            val gson = Gson()
            if (prefs.contains("dataSource")) {
                val userDetailsMap: MutableMap<String, MutableMap<String, String>>? =
                    getUserDetails(context)
                if (!userDetailsMap.isNullOrEmpty()) {
                    var userDataSource: ArrayList<AddToWidget>? = getArrayList(context)

                    if (!userDataSource.isNullOrEmpty()) {
                        if (userDetailsMap.containsKey(username)) {
                            userDetailsMap.remove(username)
                            userDataSource =
                                userDataSource.filter { it.username!! == username } as ArrayList<AddToWidget>
                        }

                        userDetailsMap[username] = mutableMapOf(
                            "name" to name,
                            "photoUrl" to photoUrl,
                            "isUser" to isUser.toString()
                        )
                        userDataSource.addAll(dataSource)
                        userDataSource.sortWith(SortByDate())
                        if (userDataSource.size > 50) userDataSource.subList(
                            51,
                            userDataSource.size
                        )
                            .clear()
                        editor.putString("dataSource", gson.toJson(userDataSource))
                        editor.putString("userDetails", gson.toJson(userDetailsMap))
                        editor.apply()
                    }
                }
            } else {
                val userDetails: MutableMap<String, MutableMap<String, String>> = mutableMapOf(
                    username to mutableMapOf(
                        "name" to name,
                        "photoUrl" to photoUrl,
                        "isUser" to isUser.toString()
                    )
                )
                editor.putString("dataSource", gson.toJson(dataSource))
                editor.putString("userDetails", gson.toJson(userDetails))
                editor.apply()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to save details", Toast.LENGTH_LONG).show()
            println(e.message)
        }
    }

    fun getArrayList(context: Context): ArrayList<AddToWidget>? {
        return try {
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val gson = Gson()
            val json: String = prefs.getString("dataSource", null).toString()
            val type: Type = object : TypeToken<ArrayList<AddToWidget?>?>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    fun deleteAllData(context: Context) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().clear().apply()
    }

    fun deleteArrayList(context: Context) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (prefs.contains("dataSource")) {
            val editor: SharedPreferences.Editor = prefs.edit()
            editor.remove("dataSource")
            editor.remove("userDetails")
            editor.apply()
        }
    }

    fun getUserDetails(context: Context): MutableMap<String, MutableMap<String, String>>? {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return if (prefs.contains("userDetails")) {
            val gson = Gson()
            val jsonUserDetails: String = prefs.getString("userDetails", null).toString()
            val jsonUserDetailsType: Type =
                object : TypeToken<MutableMap<String?, MutableMap<String?, String?>>?>() {}.type
            gson.fromJson(jsonUserDetails, jsonUserDetailsType)
        } else
            null
    }

    fun getEventDetails(currentItem: WidgetRepoModel): String? {
        try {
            return when (currentItem.type) {
                "CommitCommentEvent" -> {
                    if (currentItem.payload?.comment?.body.isNullOrEmpty()) null
                    else "\"${currentItem.payload?.comment?.body}\""
                }
                "CreateEvent" -> null
                "ForkEvent" -> null
                "DeleteEvent" -> null
                "GollumEvent" -> null
                "IssueCommentEvent" -> {
                    if (currentItem.payload?.action.isNullOrEmpty()) null
                    else when (currentItem.payload?.action) {
                        "created" -> "\"${currentItem.payload.comment?.body}\""
                        else -> null
                    }
                }
                "IssuesEvent" -> {
                    if (currentItem.payload?.action.isNullOrEmpty()) null
                    else "\"${currentItem.payload?.issue?.title}\""
                }
                "MemberEvent" -> null
                "PublicEvent" -> null
                "PullRequestEvent" -> {
                    if (currentItem.payload?.action.isNullOrEmpty() || currentItem.payload?.pull_request?.title.isNullOrEmpty()) null
                    else "\"${currentItem.payload?.pull_request?.title}\""
                }
                "PullRequestReviewEvent" -> {
                    if (currentItem.payload?.pull_request?.title.isNullOrEmpty()) null
                    else "\"${currentItem.payload?.pull_request?.title}\""
                }
                "PullRequestReviewCommentEvent" -> {
                    if (currentItem.payload?.comment?.body.isNullOrEmpty()) null
                    else "\"${currentItem.payload?.comment?.body}\""
                }
                "PushEvent" -> {
                    var message = ""
                    currentItem.payload?.commits?.forEach {
                        if (!it.message.isNullOrEmpty()) {
                            message += if (currentItem.payload.commits.last() != it)
                                "${it.message}, "
                            else it.message
                        }
                    }
                    if (currentItem.payload?.commits.isNullOrEmpty()) null
                    else "\"$message\""
                }
                "ReleaseEvent" -> {
                    if (currentItem.payload?.release?.name.isNullOrEmpty()) null
                    else "Release - ${currentItem.payload?.release?.name}"
                }
                "SponsorshipEvent" -> null
                "WatchEvent" -> null
                else -> null
            }
        } catch (e: Throwable) {
            return null
        }
    }

    fun getEventMessageAndIcon(currentItem: WidgetRepoModel): List<String> {
        try {
            return when (currentItem.type) {
                "CommitCommentEvent" -> listOf(
                    if (currentItem.payload?.comment?.body.isNullOrEmpty())
                        "User commented on a commit"
                    else "User commented on a commit\n\"${currentItem.payload?.comment?.body}\"",
                    R.drawable.ic_baseline_comment_24.toString()
                )
                "CreateEvent" -> listOf(
                    "User created a branch / tag",
                    R.drawable.ic_git_branch.toString()
                )
                "ForkEvent" -> listOf(
                    "User forked this repository",
                    R.drawable.ic_github_fork.toString()
                )
                "DeleteEvent" -> listOf(
                    "User deleted a branch / tag",
                    R.drawable.ic_baseline_delete_24.toString()
                )
                "GollumEvent" -> listOf(
                    "User created / updated a wiki page",
                    R.drawable.github_gollum.toString()
                )
                "IssueCommentEvent" -> listOf(
                    if (currentItem.payload?.action.isNullOrEmpty()) "User commented on an issue"
                    else when (currentItem.payload?.action) {
                        "edited" -> "User edited a comment"
                        "deleted" -> "User deleted a comment"
                        "created" -> "User commented on an issue\n\"${currentItem.payload.comment?.body}\""
                        else -> "User commented on an issue"
                    },
                    R.drawable.ic_baseline_comment_24.toString()
                )
                "IssuesEvent" -> listOf(
                    if (currentItem.payload?.action.isNullOrEmpty()) "Activity related to an issue"
                    else "User ${currentItem.payload?.action} a issue\n\"${currentItem.payload?.issue?.title}\"",
                    R.drawable.ic_github_issue.toString()
                )
                "MemberEvent" -> listOf(
                    "A collaborator was added or removed",
                    R.drawable.ic_baseline_group_24.toString()
                )
                "PublicEvent" -> listOf(
                    "Repository was made public",
                    R.drawable.ic_baseline_public_24.toString()
                )
                "PullRequestEvent" -> listOf(
                    if (currentItem.payload?.action.isNullOrEmpty() || currentItem.payload?.pull_request?.title.isNullOrEmpty())
                        "User made a pull request"
                    else "User ${currentItem.payload?.action} a pull request\n\"${currentItem.payload?.pull_request?.title}\"",
                    R.drawable.ic_github_pull_request.toString()
                )
                "PullRequestReviewEvent" -> listOf(
                    if (currentItem.payload?.pull_request?.title.isNullOrEmpty()) "User reviewed a pull request"
                    else "User reviewed a pull request\n\"${currentItem.payload?.pull_request?.title}\"",
                    R.drawable.pull_request_review_event.toString()
                )
                "PullRequestReviewCommentEvent" -> listOf(
                    if (currentItem.payload?.comment?.body.isNullOrEmpty()) "User commented on a pull request review"
                    else "User commented on a pull request review\n\"${currentItem.payload?.comment?.body}\"",
                    R.drawable.ic_baseline_comment_24.toString()
                )
                "PushEvent" -> {
                    var message = ""
                    currentItem.payload?.commits?.forEach {
                        if (!it.message.isNullOrEmpty()) {
                            message += if (currentItem.payload.commits.last() != it)
                                "${it.message}, "
                            else it.message
                        }
                    }
                    listOf(
                        if (currentItem.payload?.commits.isNullOrEmpty()) "User made a push request"
                        else "User made a push event\n\"$message\"",
                        R.drawable.ic_baseline_cloud_upload_24.toString()
                    )
                }
                "ReleaseEvent" -> listOf(
                    "User made a new release",
                    R.drawable.ic_baseline_new_releases_24.toString()
                )
                "SponsorshipEvent" -> listOf(
                    "User started sponsoring",
                    R.drawable.ic_baseline_monetization_on_24.toString()
                )
                "WatchEvent" -> listOf(
                    "User starred this repo",
                    R.drawable.github_star.toString()
                )
                else -> listOf("Unidentified event", R.drawable.github_logo.toString())
            }
        } catch (e: Throwable) {
            return listOf("Unidentified event", R.drawable.github_logo.toString())
        }
    }

    fun getDate(currentItem: WidgetRepoModel): String {
        val dateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val createDate = LocalDateTime.parse(currentItem.created_at, dateTimePattern)
        val currentDate = LocalDateTime.now(ZoneId.of("Etc/UTC"))
        val differenceTime = Duration.between(currentDate, createDate).abs()
        return when {
            differenceTime.seconds < 60 -> "${differenceTime.seconds} secs ago"
            differenceTime.toMinutes().toInt() == 1 -> "${differenceTime.toMinutes()} min ago"
            differenceTime.toMinutes() < 60 -> "${differenceTime.toMinutes()} mins ago"
            differenceTime.toHours().toInt() == 1 -> "${differenceTime.toHours()} hr ago"
            differenceTime.toHours() < 24 -> "${differenceTime.toHours()} hrs ago"
            differenceTime.toDays().toInt() == 1 -> "${differenceTime.toDays()} day ago"
            else -> "${differenceTime.toDays()} days ago"
        }
    }

    fun getTime(): String {
        val localTime: LocalTime = LocalTime.now()
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        return localTime.format(dateTimeFormatter)
    }

    fun getHtmlUrl(currentItem: WidgetRepoModel): String {
        return try {
            when (currentItem.type) {
                "CommitCommentEvent" -> "https://github.com/${currentItem.repo.name}"
                "CreateEvent" -> "https://github.com/${currentItem.repo.name}"
                "ForkEvent" -> currentItem.payload!!.forkee!!.html_url!!
                "DeleteEvent" -> "https://github.com/${currentItem.repo.name}"
                "GollumEvent" -> "https://github.com/${currentItem.repo.name}"
                "IssueCommentEvent" -> currentItem.payload!!.issue!!.html_url!!
                "IssuesEvent" -> currentItem.payload!!.issue!!.html_url!!
                "MemberEvent" -> "https://github.com/${currentItem.repo.name}"
                "PublicEvent" -> "https://github.com/${currentItem.repo.name}"
                "PullRequestEvent" -> currentItem.payload!!.pull_request!!.html_url!!
                "PullRequestReviewEvent" -> currentItem.payload!!.review!!.html_url!!
                "PullRequestReviewCommentEvent" -> currentItem.payload!!.comment!!.html_url!!
                "PushEvent" -> try {
                    "https://github.com/${currentItem.repo.name}/commit/${
                    currentItem.payload?.commits?.get(
                        0
                    )?.sha
                    }"
                } catch (e: Exception) {
                    "https://github.com/${currentItem.repo.name}/commit"
                }
                "ReleaseEvent" -> currentItem.payload!!.release!!.html_url!!
                "SponsorshipEvent" -> "https://github.com/${currentItem.repo.name}"
                "WatchEvent" -> "https://github.com/${currentItem.repo.name}"
                else -> "https://github.com/${currentItem.repo.name}"
            }
        } catch (e: Exception) {
            "https://github.com/${currentItem.repo.name}"
        }
    }

    fun isEmpty(context: Context): Boolean {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return !prefs.contains("userDetails")
    }

    private fun alertDialog(context: Context): AlertDialog {
        val alertDialogView =
            LayoutInflater.from(context).inflate(R.layout.loading_alertdialog, null)
        val alertDialogBuilder =
            AlertDialog.Builder(context).setView(alertDialogView).setCancelable(false)
        return alertDialogBuilder.show()
    }
}

class SortByDate : Comparator<AddToWidget> {
    override fun compare(first: AddToWidget?, second: AddToWidget?): Int {
        val firstDate = first!!.dateISO!!
        val secondDate = second!!.dateISO!!

        val dateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val createFirstDate = LocalDateTime.parse(firstDate, dateTimePattern)
        val createSecondDate = LocalDateTime.parse(secondDate, dateTimePattern)
        val result = createFirstDate.compareTo(createSecondDate)
        return when {
            result < 0 -> 1
            result > 0 -> -1
            else -> 0
        }
    }
}

class RoundedTransformation(
    private val radius: Int, // dp
    private var margin: Int
) : Transformation {
    override fun transform(source: Bitmap): Bitmap {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawRoundRect(
            RectF(
                margin.toFloat(),
                margin.toFloat(),
                (source.width - margin).toFloat(),
                (source.height - margin).toFloat()
            ),
            radius.toFloat(), radius.toFloat(), paint
        )
        if (source != output) {
            source.recycle()
        }
        return output
    }

    override fun key(): String {
        return "rounded"
    }
}
