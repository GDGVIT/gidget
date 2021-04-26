@file:Suppress("DEPRECATION")

package com.rishav.gidget.Widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.rishav.gidget.Adapters.WidgetRepoRemoteService
import com.rishav.gidget.R
import com.rishav.gidget.UI.MainActivity

class GidgetWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(
                context,
                appWidgetManager,
                appWidgetId
            )
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {}

    override fun onDisabled(context: Context) {}

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {}
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.gidget_widget)

    val buttonIntent = Intent(context, MainActivity::class.java)
    val buttonPendingIntent = PendingIntent.getActivity(context, 0, buttonIntent, 0)
    views.setOnClickPendingIntent(R.id.appwidgetRefreshButton, buttonPendingIntent)

    val clickIntent = Intent(context, GidgetWidget::class.java)
    val clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, 0)

    val serviceIntent = Intent(context, WidgetRepoRemoteService::class.java)
    serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    serviceIntent.data = Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME))
    views.setRemoteAdapter(appWidgetId, R.id.appwidgetListView, serviceIntent)

    views.setPendingIntentTemplate(R.id.appwidgetListView, clickPendingIntent)
    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.appwidgetListView)
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
