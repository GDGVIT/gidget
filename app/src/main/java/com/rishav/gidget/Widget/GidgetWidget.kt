package com.rishav.gidget.Widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.Toast
import com.rishav.gidget.Adapters.WidgetRepoRemoteService
import com.rishav.gidget.R
import com.rishav.gidget.UI.MainActivity

/**
 * Implementation of App Widget functionality.
 */
class GidgetWidget : AppWidgetProvider() {
    private val actionToast = "actionToast"
    private val extraItemPosition = "extraItemPosition"

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {

            updateAppWidget(
                context,
                appWidgetManager,
                appWidgetId,
                actionToast
            )
        }
    }

    override fun onEnabled(context: Context) {
        Toast.makeText(context, "onEnabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context) {
        Toast.makeText(context, "onDisabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        Toast.makeText(context, "onDeleted", Toast.LENGTH_SHORT).show()
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        val views = RemoteViews(context.packageName, R.layout.gidget_widget)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (actionToast == intent.action) {
            val clickedPosition = intent.getIntExtra(extraItemPosition, 0)
            Toast.makeText(context, "Clicked position: $clickedPosition", Toast.LENGTH_SHORT).show()
        }
        super.onReceive(context, intent)
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    ACTION_TOAST: String
) {
    val buttonIntent = Intent(context, MainActivity::class.java)
    val buttonPendingIntent = PendingIntent.getActivity(context, 0, buttonIntent, 0)

    val serviceIntent = Intent(context, WidgetRepoRemoteService::class.java)
    serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))

    val clickIntent = Intent(context, GidgetWidget::class.java)
    clickIntent.action = ACTION_TOAST
    val clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, 0)

    val views = RemoteViews(context.packageName, R.layout.gidget_widget)
    views.setRemoteAdapter(appWidgetId, R.id.appwidgetListView, serviceIntent)
    views.setOnClickPendingIntent(R.id.appwidgetRefreshButton, buttonPendingIntent)
    views.setPendingIntentTemplate(R.id.appwidgetListView, clickPendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}
