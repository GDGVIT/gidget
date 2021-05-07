package com.rishav.gidget.Widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.rishav.gidget.Adapters.WidgetRepoRemoteService
import com.rishav.gidget.R
import com.rishav.gidget.Realm.AddToWidget
import com.rishav.gidget.UI.MainActivity

class GidgetWidget : AppWidgetProvider() {
    private var dataSource: ArrayList<AddToWidget> = arrayListOf()
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

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && context != null && intent.extras != null && intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            if (intent.extras!!.containsKey("dataSource") || intent.hasExtra("dataSource")) {
                dataSource = intent.getParcelableArrayListExtra("dataSource")!!

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
        }
        super.onReceive(context, intent)
    }

    override fun onEnabled(context: Context) {}

    override fun onDisabled(context: Context) {}

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {}
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    dataSource: ArrayList<AddToWidget>
) {
    val views = RemoteViews(context.packageName, R.layout.gidget_widget)

    if (dataSource.isNullOrEmpty()) {
        views.setEmptyView(R.id.appwidgetListView, R.id.appwidgetEmptyViewText)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    } else {
        // Widget Service Intent
        val serviceIntent = Intent(context, WidgetRepoRemoteService::class.java)
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        val bundle = Bundle()
        bundle.putParcelableArrayList("dataSourceBundle", dataSource)
        serviceIntent.putExtra("dataSource", bundle)
        views.setRemoteAdapter(R.id.appwidgetListView, serviceIntent)

        // Button Intent
        val buttonIntent = Intent(context, MainActivity::class.java)
        val buttonPendingIntent = PendingIntent.getActivity(context, 0, buttonIntent, 0)
        views.setOnClickPendingIntent(R.id.appWidgetRecyclerItem, buttonPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
