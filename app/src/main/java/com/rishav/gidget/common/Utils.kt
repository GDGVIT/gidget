package com.rishav.gidget.common

import android.annotation.SuppressLint
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
import android.os.Build
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rishav.gidget.R
import com.rishav.gidget.interfaces.RetroFitService
import com.rishav.gidget.models.widget.WidgetRepoModel
import com.rishav.gidget.realm.AddToWidget
import com.rishav.gidget.widget.GidgetWidget
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
        username: String,
        name: String,
        context: Context,
    ) {
        val ids: IntArray = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, GidgetWidget::class.java))
        val alertDialog: AlertDialog = alertDialog(context)
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
                                val widgetIntent = Intent(context, GidgetWidget::class.java)
                                widgetIntent.action = getUpdateWidgetAction()
                                context.sendBroadcast(widgetIntent)
                                val appwidgetAlarm = AppWidgetAlarm(context.applicationContext)
                                appwidgetAlarm.startGidgetRefresh()

                                if (alertDialog.isShowing)
                                    alertDialog.dismiss()
                                Toast.makeText(context, "Added to Gidget", Toast.LENGTH_LONG).show()
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
                                val widgetIntent = Intent(context, GidgetWidget::class.java)
                                widgetIntent.action = getUpdateWidgetAction()
                                context.sendBroadcast(widgetIntent)
                                val appwidgetAlarm = AppWidgetAlarm(context.applicationContext)
                                appwidgetAlarm.startGidgetRefresh()

                                if (alertDialog.isShowing)
                                    alertDialog.dismiss()
                                Toast.makeText(context, "Added to Gidget", Toast.LENGTH_LONG)
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

    fun getEventData(currentItem: WidgetRepoModel): List<String> {
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
                "User starred this repo",
                R.drawable.github_star.toString()
            )
            else -> listOf("Unidentified event", R.drawable.github_logo.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTime(): String {
        val localTime: LocalTime = LocalTime.now()
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        return localTime.format(dateTimeFormatter)
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
