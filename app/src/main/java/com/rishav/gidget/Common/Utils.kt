package com.rishav.gidget.Common

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.rishav.gidget.Interface.RetroFitService
import com.rishav.gidget.Models.Widget.WidgetRepoModel
import com.rishav.gidget.R
import com.rishav.gidget.Realm.AddToWidget
import com.rishav.gidget.Widget.GidgetWidget
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Utils {
    companion object {
        fun getOnWidgetItemClickedAction(): String {
            return "onWidgetItemClicked"
        }
    }

    fun addToWidget(
        mService: RetroFitService,
        isUser: Boolean,
        username: String,
        name: String,
        context: Context
    ) {
        if (isUser)
            mService.widgetUserEvents(username)
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
                            val ids: IntArray =
                                AppWidgetManager.getInstance(context).getAppWidgetIds(
                                    ComponentName(context, GidgetWidget::class.java)
                                )
                            if (ids.isNotEmpty()) {
                                val widgetIntent = Intent(context, GidgetWidget::class.java)
                                widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                widgetIntent.putParcelableArrayListExtra("dataSource", dataSource)
                                context.sendBroadcast(widgetIntent)
                            }

                            Toast.makeText(context, "Added to widget", Toast.LENGTH_LONG).show()
                        } else
                            Toast.makeText(context, "Could not add", Toast.LENGTH_LONG).show()
                    }

                    override fun onFailure(call: Call<MutableList<WidgetRepoModel>>, t: Throwable) {
                        println("ERROR - ${t.message}")
                    }
                })
        else
            mService.widgetRepoEvents(username, name)
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

                            val ids: IntArray =
                                AppWidgetManager.getInstance(context).getAppWidgetIds(
                                    ComponentName(context, GidgetWidget::class.java)
                                )
                            if (ids.isNotEmpty()) {
                                val widgetIntent = Intent(context, GidgetWidget::class.java)
                                widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                widgetIntent.putParcelableArrayListExtra("dataSource", dataSource)
                                context.sendBroadcast(widgetIntent)
                            }

                            Toast.makeText(context, "Added to widget", Toast.LENGTH_LONG).show()
                        } else
                            Toast.makeText(context, "Could not add", Toast.LENGTH_LONG).show()
                    }

                    override fun onFailure(call: Call<MutableList<WidgetRepoModel>>, t: Throwable) {
                        println("ERROR - ${t.message}")
                    }
                })
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
}
