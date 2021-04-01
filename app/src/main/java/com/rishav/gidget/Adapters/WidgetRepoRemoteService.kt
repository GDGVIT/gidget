package com.rishav.gidget.Adapters

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.annotation.RequiresApi
import com.rishav.gidget.Common.Common
import com.rishav.gidget.Interface.RetroFitService
import com.rishav.gidget.Models.Widget.WidgetRepoModel
import com.rishav.gidget.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WidgetRepoRemoteService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return WidgetRepoRemoteViewsFactory(applicationContext, intent!!)
    }
}

class WidgetRepoRemoteViewsFactory(private val context: Context, private val intent: Intent) :
    RemoteViewsService.RemoteViewsFactory {
    private lateinit var mService: RetroFitService
    private var dataSource: MutableList<WidgetRepoModel>? = null
    private var appWidgetId = 0
    private val actionToast = "actionToast"
    private val extraItemPosition = "extraItemPosition"

    init {
        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
    }

    override fun onCreate() {
        mService = Common.retroFitService
    }

    override fun onDataSetChanged() {
        mService.widgetRepoEvents("PowerShellOrg", "DSC")
            .enqueue(object : Callback<MutableList<WidgetRepoModel>> {
                override fun onResponse(
                    call: Call<MutableList<WidgetRepoModel>>,
                    response: Response<MutableList<WidgetRepoModel>>
                ) {
                    println(response)
                    println(response.body())

                    dataSource = response.body()
                }

                override fun onFailure(call: Call<MutableList<WidgetRepoModel>>, t: Throwable) {
                    println("ERROR - ${t.message}")
                }
            })
    }

    override fun onDestroy() {
        TODO("Not yet implemented")
    }

    override fun getCount(): Int {
        return if (dataSource.isNullOrEmpty())
            0
        else
            dataSource!!.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.appwidget_recycler_item)
        val currentItem = dataSource!![position]

        views.setTextViewText(R.id.appwidgetRecyclerViewItemUsername, currentItem.actor.login)
//        views.setTextViewText(R.id.appwidgetRecyclerViewItemRepoName, currentItem.repo.name)
//        views.setTextViewText(R.id.appwidgetRecyclerViewItemMessage, getMessage(currentItem))
//        views.setTextViewText(R.id.appwidgetRecyclerViewItemDate, getDate(currentItem))

        val fillIntent = Intent().putExtra(extraItemPosition, position)
        views.setOnClickFillInIntent(R.id.appwidgetRecyclerViewItemUsername, fillIntent)

        SystemClock.sleep(5000)
        return views
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true

    private fun getMessage(currentItem: WidgetRepoModel): String {
        return when (currentItem.type) {
            "CommitCommentEvent" -> "User commented on a commit"
            "CreateEvent" -> "User created a branch / tag"
            "ForkEvent" -> "User forked this repository"
            "DeleteEvent" -> "User deleted a branch / tag"
            "GollumEvent" -> "User created / updated a wiki page"
            "IssueCommentEvent" -> "User commented on an issue"
            "IssuesEvent" -> "Activity related to an issue"
            "MemberEvent" -> "A collaborator was added or removed"
            "PublicEvent" -> "Repository was made public"
            "PullRequestEvent" -> "User made a pull request"
            "PullRequestReviewCommentEvent" -> "User commented on a pull request review"
            "PushEvent" -> "User made a push request"
            "ReleaseEvent" -> "User made a new release"
            "SponsorshipEvent" -> "User started sponsoring"
            "WatchEvent" -> "User was watching"
            else -> "Unidentified event"
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
