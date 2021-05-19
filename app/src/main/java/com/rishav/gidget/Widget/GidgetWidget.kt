package com.rishav.gidget.Widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.rishav.gidget.Adapters.MyBroadcastReceiver
import com.rishav.gidget.Adapters.WidgetRepoRemoteService
import com.rishav.gidget.Common.Utils
import com.rishav.gidget.R
import com.rishav.gidget.Realm.AddToWidget
import com.rishav.gidget.UI.MainActivity
import com.rishav.gidget.UI.SearchActivity

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

        if (intent != null && context != null && intent.extras != null && intent.action == Utils.getOnWidgetItemClickedAction()) {
            if (intent.extras!!.containsKey("dataSource") || intent.hasExtra("dataSource")) {
                val clickedItem: AddToWidget = intent.getParcelableExtra("dataSource")!!
                val uri: Uri = Uri.parse("https://github.com/${clickedItem.name}")
                val clickIntent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Toast.makeText(context, "Item clicked - ${clickedItem.name}", Toast.LENGTH_LONG).show()
                context.startActivity(clickIntent)
            }
        }
        super.onReceive(context, intent)
    }

    override fun onEnabled(context: Context) {}

    override fun onDisabled(context: Context) {}

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {}
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
    val buttonIntent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    val buttonPendingIntent = PendingIntent.getActivity(context, 0, buttonIntent, 0)
    views.setOnClickPendingIntent(R.id.appWidgetLogo, buttonPendingIntent)
    views.setOnClickPendingIntent(R.id.appwidgetTitle, buttonPendingIntent)

    val searchIntent = Intent(context, SearchActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    val searchPendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, searchIntent, 0)
    views.setOnClickPendingIntent(R.id.appwidgetRefreshButton, searchPendingIntent)

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

        val tempIntent = Intent(context, MyBroadcastReceiver::class.java)
        val bundle = Bundle()
        bundle.putParcelableArrayList("dataSourceBundle", dataSource)
        tempIntent.putExtra("dataSource", bundle)
        context.sendBroadcast(tempIntent)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.appwidgetListView)
    }

    appWidgetManager.updateAppWidget(appWidgetId, views)
}
