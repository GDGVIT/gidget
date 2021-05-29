package com.rishav.gidget.Widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.rishav.gidget.Adapters.WidgetRepoRemoteService
import com.rishav.gidget.Common.Common
import com.rishav.gidget.Common.Utils
import com.rishav.gidget.R
import com.rishav.gidget.Realm.AddToWidget
import com.rishav.gidget.UI.MainActivity

class GidgetWidget : AppWidgetProvider() {
    private var dataSource: ArrayList<AddToWidget> = arrayListOf()

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

    override fun onDisabled(context: Context) {}

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        dataSource.clear()
        Utils.deleteArrayList(context!!)
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
            appWidgetManager.getAppWidgetIds(
                ComponentName(
                    context,
                    GidgetWidget::class.java
                )
            )
        onUpdate(context, appWidgetManager, appWidgetIds)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun onWidgetRefresh(context: Context) {
        val userMap: MutableMap<String, String> = Utils.getUserDetails(context)
        Utils().addToWidget(
            mService = Common.retroFitService,
            isUser = userMap["isUser"]!!.toBoolean(),
            isWidget = true,
            username = userMap["username"]!!,
            name = userMap["name"]!!,
            context = context
        )
        widgetActionUpdate(context)
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

//    val searchIntent = Intent(context, SearchActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//    val searchPendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, searchIntent, 0)
//    views.setOnClickPendingIntent(R.id.appwidgetRefreshButton, searchPendingIntent)
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
