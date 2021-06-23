package com.dscvit.gidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import com.dscvit.gidget.R
import com.dscvit.gidget.activities.MainActivity
import com.dscvit.gidget.adapters.WidgetRepoRemoteService
import com.dscvit.gidget.common.AppWidgetAlarm
import com.dscvit.gidget.common.Common
import com.dscvit.gidget.common.Security
import com.dscvit.gidget.common.Utils
import com.dscvit.gidget.models.widget.AddToWidget
import com.dscvit.gidget.models.widget.WidgetRepoModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GidgetWidget : AppWidgetProvider() {
    private var dataSource: ArrayList<AddToWidget> = arrayListOf()
    private val utils = Utils()
    private val mService = Common.retroFitService

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds)
            updateAppWidget(context, appWidgetManager, appWidgetId, utils)
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && context != null && intent.action == Utils.getUpdateWidgetAction())
            widgetActionUpdate(context)

        if (intent != null && context != null && intent.extras != null && intent.action == Utils.getOnWidgetItemClickedAction())
            onItemClicked(intent = intent, context = context)

        if (intent != null && context != null && intent.action == Utils.getOnRefreshButtonClicked())
            onWidgetRefresh(context)

        if (intent != null && context != null && intent.action == Utils.getDeleteWidgetAction())
            deleteWidgetData(context)

        super.onReceive(context, intent)
    }

    override fun onEnabled(context: Context) {
        val appwidgetAlarm = AppWidgetAlarm(context.applicationContext)
        if (!utils.isEmpty(context))
            appwidgetAlarm.startGidgetRefresh()
    }

    override fun onDisabled(context: Context) {}

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {}

    private fun onItemClicked(intent: Intent, context: Context) {
        if (intent.extras!!.containsKey("dataSource") || intent.hasExtra("dataSource")) {
            val clickedItem: AddToWidget = intent.getParcelableExtra("dataSource")!!
            val uri: Uri = Uri.parse(clickedItem.htmlUrl)
            val clickIntent =
                Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Toast.makeText(context, clickedItem.name, Toast.LENGTH_LONG).show()
            context.startActivity(clickIntent)
        }
    }

    private fun widgetActionUpdate(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, GidgetWidget::class.java))
        updateAppWidget(context, appWidgetManager, appWidgetIds.first(), utils)
    }

    private fun onWidgetRefresh(context: Context) {
        val userMap: MutableMap<String, String> = utils.getUserDetails(context)
        val screenAwake = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (userMap.isNotEmpty() && screenAwake.isInteractive && !screenAwake.isDeviceIdleMode)
            addToWidget(context, userMap)
        else
            Toast.makeText(context, "Cannot refresh empty widget", Toast.LENGTH_LONG).show()
    }

    private fun addToWidget(context: Context, userMap: MutableMap<String, String>) {
        val views = RemoteViews(context.packageName, R.layout.gidget_widget)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, GidgetWidget::class.java))

        views.setViewVisibility(R.id.appwidgetProgressBar, View.VISIBLE)
        appWidgetManager.updateAppWidget(appWidgetIds, views)

        views.setViewVisibility(R.id.appwidgetProgressBar, View.GONE)
        views.setTextViewText(R.id.appwidgetDate, utils.getTime())

        if (userMap["isUser"]!!.toBoolean()) {
            mService.widgetUserEvents(
                userMap["username"]!!,
                "token ${Security.getToken()}"
            )
                .enqueue(object : Callback<MutableList<WidgetRepoModel>> {
                    override fun onResponse(
                        call: Call<MutableList<WidgetRepoModel>>,
                        response: Response<MutableList<WidgetRepoModel>>
                    ) {
                        val dataSource: ArrayList<AddToWidget> = arrayListOf()
                        if (response.body() != null) {
                            for (res in response.body()!!) {
                                val addToWidget = AddToWidget()
                                val eventsList: List<String> = utils.getEventData(res)

                                addToWidget.username = res.actor.login
                                addToWidget.name = res.repo.name
                                addToWidget.avatarUrl = res.actor.avatar_url
                                addToWidget.icon = eventsList[1].toInt()
                                addToWidget.message = eventsList[0]
                                addToWidget.date = utils.getDate(res)
                                addToWidget.htmlUrl = utils.getHtmlUrl(res)

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
                            appWidgetManager.updateAppWidget(appWidgetIds, views)
                        }
                    }

                    override fun onFailure(
                        call: Call<MutableList<WidgetRepoModel>>,
                        t: Throwable
                    ) {
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
                    override fun onResponse(
                        call: Call<MutableList<WidgetRepoModel>>,
                        response: Response<MutableList<WidgetRepoModel>>
                    ) {
                        val dataSource: ArrayList<AddToWidget> = arrayListOf()
                        if (response.body() != null) {
                            for (res in response.body()!!) {
                                val addToWidget = AddToWidget()
                                val eventsList: List<String> = utils.getEventData(res)

                                addToWidget.username = res.actor.login
                                addToWidget.name = res.repo.name
                                addToWidget.avatarUrl = res.actor.avatar_url
                                addToWidget.icon = eventsList[1].toInt()
                                addToWidget.message = eventsList[0]
                                addToWidget.date = utils.getDate(res)
                                addToWidget.htmlUrl = utils.getHtmlUrl(res)

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
                            appWidgetManager.updateAppWidget(appWidgetIds, views)
                        }
                    }

                    override fun onFailure(
                        call: Call<MutableList<WidgetRepoModel>>,
                        t: Throwable
                    ) {
                        println("ERROR - ${t.message}")
                        appWidgetManager.updateAppWidget(appWidgetIds, views)
                    }
                })
        }
    }

    private fun deleteWidgetData(context: Context) {
        val appwidgetAlarm = AppWidgetAlarm(context.applicationContext)
        appwidgetAlarm.stopGidgetRefresh()
        utils.deleteAllData(context)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, GidgetWidget::class.java))
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.appwidgetListView)
        widgetActionUpdate(context)
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    utils: Utils
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

    if (utils.isEmpty(context)) {
        views.setEmptyView(R.id.appwidgetListView, R.id.appwidgetEmptyViewText)
        views.setOnClickPendingIntent(R.id.appwidgetEmptyViewText, buttonPendingIntent)
    } else {
        // Date Widget
        views.setTextViewText(R.id.appwidgetDate, utils.getTime())
        // Widget Service Intent
        val serviceIntent = Intent(context, WidgetRepoRemoteService::class.java)
        views.setRemoteAdapter(R.id.appwidgetListView, serviceIntent)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.appwidgetListView)
    }
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
