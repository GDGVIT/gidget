package com.rishav.gidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.rishav.gidget.R
import com.rishav.gidget.adapters.WidgetRepoRemoteService
import com.rishav.gidget.common.AppWidgetAlarm
import com.rishav.gidget.common.Common
import com.rishav.gidget.common.Security
import com.rishav.gidget.common.Utils
import com.rishav.gidget.models.widget.WidgetRepoModel
import com.rishav.gidget.realm.AddToWidget
import com.rishav.gidget.ui.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GidgetWidget : AppWidgetProvider() {
    private var dataSource: ArrayList<AddToWidget> = arrayListOf()
    private val utils = Utils()
    private val mService = Common.retroFitService

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(
                context,
                appWidgetManager,
                appWidgetId,
                dataSource
            )
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && context != null && intent.action == Utils.getUpdateWidgetAction())
            widgetActionUpdate(context)

        if (intent != null && context != null && intent.extras != null && intent.action == Utils.getOnWidgetItemClickedAction())
            onItemClicked(intent = intent, context = context)

        if (intent != null && context != null && intent.action == Utils.getOnRefreshButtonClicked())
            onWidgetRefresh(context)

        super.onReceive(context, intent)
    }

    override fun onEnabled(context: Context) {}

    override fun onDisabled(context: Context) {
        val appwidgetAlarm = AppWidgetAlarm(context.applicationContext)
        appwidgetAlarm.stopAlarm()
        dataSource.clear()
        Utils.deleteArrayList(context)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        val appwidgetAlarm = AppWidgetAlarm(context!!.applicationContext)
        appwidgetAlarm.stopAlarm()
        dataSource.clear()
        Utils.deleteArrayList(context)
    }

    private fun onItemClicked(intent: Intent, context: Context) {
        if (intent.extras!!.containsKey("dataSource") || intent.hasExtra("dataSource")) {
            val clickedItem: AddToWidget = intent.getParcelableExtra("dataSource")!!
            val uri: Uri = Uri.parse("https://github.com/${clickedItem.name}")
            val clickIntent =
                Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Toast.makeText(context, clickedItem.name, Toast.LENGTH_LONG).show()
            context.startActivity(clickIntent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun widgetActionUpdate(context: Context) {
        dataSource = Utils.getArrayList(context)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds =
            appWidgetManager.getAppWidgetIds(ComponentName(context, GidgetWidget::class.java))
        onUpdate(context, appWidgetManager, appWidgetIds)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun onWidgetRefresh(context: Context) {
        val userMap: MutableMap<String, String> = Utils.getUserDetails(context)
        if (userMap.isNotEmpty())
            addToWidget(context, userMap)
    }

    private fun addToWidget(context: Context, userMap: MutableMap<String, String>) {
        val views = RemoteViews(context.packageName, R.layout.gidget_widget)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds =
            appWidgetManager.getAppWidgetIds(ComponentName(context, GidgetWidget::class.java))

        views.setViewVisibility(R.id.appwidgetProgressBar, View.VISIBLE)
        appWidgetManager.updateAppWidget(appWidgetIds, views)

        views.setViewVisibility(R.id.appwidgetProgressBar, View.GONE)

        if (userMap["isUser"]!!.toBoolean()) {
            mService.widgetUserEvents(
                userMap["username"]!!,
                "token ${Security.getToken()}"
            )
                .enqueue(object : Callback<MutableList<WidgetRepoModel>> {
                    @RequiresApi(Build.VERSION_CODES.Q)
                    override fun onResponse(
                        call: Call<MutableList<WidgetRepoModel>>,
                        response: Response<MutableList<WidgetRepoModel>>
                    ) {
                        if (response.body() != null) {
                            val dataSource: ArrayList<AddToWidget> = arrayListOf()
                            for (res in response.body()!!) {
                                val addToWidget = AddToWidget()
                                val eventsList: List<String> = utils.getEventData(res)

                                addToWidget.username = res.actor.login
                                addToWidget.name = res.repo.name
                                addToWidget.avatarUrl = res.actor.avatar_url
                                addToWidget.icon = eventsList[1].toInt()
                                addToWidget.message = eventsList[0]
                                addToWidget.date = utils.getDate(res)

                                dataSource.add(addToWidget)
                            }

                            utils.saveArrayList(
                                arrayList = dataSource,
                                context = context,
                                username = userMap["username"]!!,
                                name = userMap["name"]!!,
                                isUser = userMap["isUser"]!!.toBoolean()
                            )
                            widgetActionUpdate(context)
                            Toast.makeText(context, "Gidget refreshed", Toast.LENGTH_LONG).show()
                            appWidgetManager.updateAppWidget(appWidgetIds, views)
                        }
                    }

                    override fun onFailure(
                        call: Call<MutableList<WidgetRepoModel>>,
                        t: Throwable
                    ) {

                        Toast.makeText(context, "Gidget refresh unsuccessful", Toast.LENGTH_LONG)
                            .show()
                        println("ERROR - ${t.message}")
                        appWidgetManager.updateAppWidget(appWidgetIds, views)
                    }
                })
        } else {
            mService.widgetRepoEvents(
                userMap["username"]!!,
                userMap["name"]!!,
                "token ${Security.getToken()}"
            )
                .enqueue(object : Callback<MutableList<WidgetRepoModel>> {
                    @RequiresApi(Build.VERSION_CODES.Q)
                    override fun onResponse(
                        call: Call<MutableList<WidgetRepoModel>>,
                        response: Response<MutableList<WidgetRepoModel>>
                    ) {
                        if (response.body() != null) {
                            val dataSource: ArrayList<AddToWidget> = arrayListOf()
                            for (res in response.body()!!) {
                                val addToWidget = AddToWidget()
                                val eventsList: List<String> = utils.getEventData(res)

                                addToWidget.username = res.actor.login
                                addToWidget.name = res.repo.name
                                addToWidget.avatarUrl = res.actor.avatar_url
                                addToWidget.icon = eventsList[1].toInt()
                                addToWidget.message = eventsList[0]
                                addToWidget.date = utils.getDate(res)

                                dataSource.add(addToWidget)
                            }

                            utils.saveArrayList(
                                arrayList = dataSource,
                                context = context,
                                username = userMap["username"]!!,
                                name = userMap["name"]!!,
                                isUser = userMap["isUser"]!!.toBoolean()
                            )

                            widgetActionUpdate(context)
                            Toast.makeText(context, "Gidget refreshed", Toast.LENGTH_LONG).show()
                            appWidgetManager.updateAppWidget(appWidgetIds, views)
                        }
                    }

                    override fun onFailure(
                        call: Call<MutableList<WidgetRepoModel>>,
                        t: Throwable
                    ) {
                        Toast.makeText(context, "Gidget refresh unsuccessful", Toast.LENGTH_LONG)
                            .show()
                        println("ERROR - ${t.message}")
                        appWidgetManager.updateAppWidget(appWidgetIds, views)
                    }
                })
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    dataSource: ArrayList<AddToWidget>
) {
    val views = RemoteViews(context.packageName, R.layout.gidget_widget)

    // Button Intent
    val buttonIntent =
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    val buttonPendingIntent = PendingIntent.getActivity(context, 0, buttonIntent, 0)
    views.setOnClickPendingIntent(R.id.appWidgetLogo, buttonPendingIntent)
    views.setOnClickPendingIntent(R.id.appwidgetTitle, buttonPendingIntent)

    val refreshIntent = Intent(context, GidgetWidget::class.java)
    refreshIntent.action = Utils.getOnRefreshButtonClicked()
    val refreshPendingIntent =
        PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    views.setOnClickPendingIntent(R.id.appwidgetRefreshButton, refreshPendingIntent)

    // Main Widget
    val clickIntent = Intent(context, GidgetWidget::class.java)
    val clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, 0)
    views.setPendingIntentTemplate(R.id.appwidgetListView, clickPendingIntent)

    if (dataSource.isNullOrEmpty()) {
        views.setEmptyView(R.id.appwidgetListView, R.id.appwidgetEmptyViewText)
        views.setOnClickPendingIntent(R.id.appwidgetEmptyViewText, buttonPendingIntent)
    } else {
        // Widget Service Intent
        val serviceIntent = Intent(context, WidgetRepoRemoteService::class.java)
        views.setRemoteAdapter(R.id.appwidgetListView, serviceIntent)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.appwidgetListView)
    }
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
