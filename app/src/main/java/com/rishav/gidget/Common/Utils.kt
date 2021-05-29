package com.rishav.gidget.Common

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rishav.gidget.Interface.RetroFitService
import com.rishav.gidget.Models.Widget.WidgetRepoModel
import com.rishav.gidget.R
import com.rishav.gidget.Realm.AddToWidget
import com.rishav.gidget.Widget.GidgetWidget
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Utils {
    companion object {
        fun getOnWidgetItemClickedAction(): String = "onWidgetItemClicked"
        fun getUpdateWidgetAction(): String = "updateWidgetWithDatasource"
        fun getOnRefreshButtonClicked(): String = "onRefreshButtonClicked"

        fun getArrayList(context: Context): ArrayList<AddToWidget> {
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val gson = Gson()
            val json: String = prefs.getString("dataSource", null).toString()
            val type: Type = object : TypeToken<ArrayList<AddToWidget?>?>() {}.type
            return gson.fromJson(json, type)
        }

        fun deleteArrayList(context: Context) {
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().clear().apply()
        }

        fun getUserDetails(context: Context): MutableMap<String, String> {
            val userMap: MutableMap<String, String> = mutableMapOf()
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            if (prefs.all.isNotEmpty()) {
                userMap["username"] = prefs.getString("username", null).toString()
                userMap["name"] = prefs.getString("name", null).toString()
                userMap["isUser"] = prefs.getString("isUser", null).toString()
            }
            return userMap
        }
    }

    fun addToWidget(
        mService: RetroFitService,
        isUser: Boolean,
        isWidget: Boolean,
        username: String,
        name: String,
        context: Context,
    ) {
        val ids: IntArray = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, GidgetWidget::class.java))
        var alertDialog: AlertDialog? = null
        if (!isWidget)
            alertDialog = alertDialog(context)
        if (ids.isNotEmpty()) {
            if (isUser)
                mService.widgetUserEvents(
                    username,
                    "token ${Security.getToken()}"
                )
                    .enqueue(object : Callback<MutableList<WidgetRepoModel>> {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(
                            call: Call<MutableList<WidgetRepoModel>>,
                            response: Response<MutableList<WidgetRepoModel>>
                        ) {
                            if (response.body() != null) {
                                val dataSource: ArrayList<AddToWidget> = arrayListOf()
                                for (res in response.body()!!) {
                                    val addToWidget = AddToWidget()
                                    val eventsList: List<String> = getEventData(res)

                                    addToWidget.username = res.actor.login
                                    addToWidget.name = res.repo.name
                                    addToWidget.avatarUrl = res.actor.avatar_url
                                    addToWidget.icon = eventsList[1].toInt()
                                    addToWidget.message = eventsList[0]
                                    addToWidget.date = getDate(res)

                                    dataSource.add(addToWidget)
                                }

                                saveArrayList(
                                    arrayList = dataSource,
                                    context = context,
                                    username = username,
                                    name = name,
                                    isUser = isUser
                                )
                                if (!isWidget && alertDialog != null) {
                                    val widgetIntent = Intent(context, GidgetWidget::class.java)
                                    widgetIntent.action = getUpdateWidgetAction()
                                    context.sendBroadcast(widgetIntent)
                                    if (alertDialog.isShowing)
                                        alertDialog.dismiss()
                                    Toast.makeText(context, "Added to widget", Toast.LENGTH_LONG).show()
                                } else if (isWidget)
                                    Toast.makeText(context, "Widget refreshed", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(
                            call: Call<MutableList<WidgetRepoModel>>,
                            t: Throwable
                        ) {
                            if (alertDialog != null && alertDialog.isShowing && !isWidget) {
                                Toast.makeText(context, "Could not add widget", Toast.LENGTH_LONG).show()
                                alertDialog.dismiss()
                            } else if (isWidget)
                                Toast.makeText(context, "Widget refresh unsuccessful", Toast.LENGTH_LONG).show()
                            println("ERROR - ${t.message}")
                        }
                    })
            else
                mService.widgetRepoEvents(
                    username,
                    name,
                    "token ${Security.getToken()}"
                )
                    .enqueue(object : Callback<MutableList<WidgetRepoModel>> {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(
                            call: Call<MutableList<WidgetRepoModel>>,
                            response: Response<MutableList<WidgetRepoModel>>
                        ) {
                            if (response.body() != null) {
                                val dataSource: ArrayList<AddToWidget> = arrayListOf()
                                for (res in response.body()!!) {
                                    val addToWidget = AddToWidget()
                                    val eventsList: List<String> = getEventData(res)

                                    addToWidget.username = res.actor.login
                                    addToWidget.name = res.repo.name
                                    addToWidget.avatarUrl = res.actor.avatar_url
                                    addToWidget.icon = eventsList[1].toInt()
                                    addToWidget.message = eventsList[0]
                                    addToWidget.date = getDate(res)

                                    dataSource.add(addToWidget)
                                }

                                saveArrayList(
                                    arrayList = dataSource,
                                    context = context,
                                    username = username,
                                    name = name,
                                    isUser = isUser
                                )
                                if (!isWidget) {
                                    val widgetIntent = Intent(context, GidgetWidget::class.java)
                                    widgetIntent.action = getUpdateWidgetAction()
                                    context.sendBroadcast(widgetIntent)
                                    if (alertDialog != null && alertDialog.isShowing)
                                        alertDialog.dismiss()
                                    Toast.makeText(context, "Added to widget", Toast.LENGTH_LONG)
                                        .show()
                                } else if (isWidget)
                                    Toast.makeText(context, "Widget refreshed", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(
                            call: Call<MutableList<WidgetRepoModel>>,
                            t: Throwable
                        ) {
                            if (alertDialog != null && alertDialog.isShowing && !isWidget) {
                                Toast.makeText(context, "Could not add widget", Toast.LENGTH_LONG).show()
                                alertDialog.dismiss()
                            } else if (isWidget)
                                Toast.makeText(context, "Widget refresh unsuccessful", Toast.LENGTH_LONG).show()
                            println("ERROR - ${t.message}")
                        }
                    })
        } else {
            if (alertDialog != null && alertDialog.isShowing && !isWidget)
                alertDialog.dismiss()
            Toast.makeText(context, "Please add widget to home screen", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveArrayList(
        arrayList: ArrayList<AddToWidget>,
        context: Context,
        username: String,
        name: String,
        isUser: Boolean
    ) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor: SharedPreferences.Editor = prefs.edit()
        val gson = Gson()
        val json: String = gson.toJson(arrayList)
        editor.putString("dataSource", json)
        editor.putString("username", username)
        editor.putString("name", name)
        editor.putString("isUser", isUser.toString())
        editor.apply()
    }

    private fun getEventData(currentItem: WidgetRepoModel): List<String> {
        return when (currentItem.type) {
            "CommitCommentEvent" -> listOf(
                "User commented on a commit",
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
                "User commented on an issue",
                R.drawable.ic_baseline_comment_24.toString()
            )
            "IssuesEvent" -> listOf(
                "Activity related to an issue",
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
                "User made a pull request",
                R.drawable.ic_github_pull_request.toString()
            )
            "PullRequestReviewEvent" -> listOf(
                "User reviewed a pull request",
                R.drawable.pull_request_review_event.toString()
            )
            "PullRequestReviewCommentEvent" -> listOf(
                "User commented on a pull request review",
                R.drawable.ic_baseline_comment_24.toString()
            )
            "PushEvent" -> listOf(
                "User made a push request",
                R.drawable.ic_baseline_cloud_upload_24.toString()
            )
            "ReleaseEvent" -> listOf(
                "User made a new release",
                R.drawable.ic_baseline_new_releases_24.toString()
            )
            "SponsorshipEvent" -> listOf(
                "User started sponsoring",
                R.drawable.ic_baseline_monetization_on_24.toString()
            )
            "WatchEvent" -> listOf(
                "User was watching",
                R.drawable.ic_baseline_remove_red_eye_24.toString()
            )
            else -> listOf("Unidentified event", R.drawable.github_logo.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(currentItem: WidgetRepoModel): String {
        val dateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val createDate = LocalDateTime.parse(currentItem.created_at, dateTimePattern)
        val currentDate = LocalDateTime.now()
        val differenceTime = Duration.between(currentDate, createDate).abs()
        return when {
            differenceTime.toMinutes() < 60 -> "${differenceTime.toMinutes()} minutes ago"
            differenceTime.toHours() < 24 -> "${differenceTime.toHours()} hours ago"
            differenceTime.toDays() <= 1 -> "${differenceTime.toDays()} day ago"
            else -> "${differenceTime.toDays()} days ago"
        }
    }

    @SuppressLint("InflateParams")
    private fun alertDialog(context: Context): AlertDialog {
        val alertDialogView =
            LayoutInflater.from(context).inflate(R.layout.loading_alertdialog, null)
        val alertDialogBuilder =
            AlertDialog.Builder(context).setView(alertDialogView).setCancelable(false)
        val alertDialog = alertDialogBuilder.show()

        alertDialogView.findViewById<Button>(R.id.alertCancel).setOnClickListener {
            alertDialog.dismiss()
            Toast.makeText(context, "User Cancelled", Toast.LENGTH_LONG).show()
        }
        return alertDialog
    }
}
