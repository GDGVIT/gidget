package com.rishav.gidget.Adapters

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.annotation.RequiresApi
import com.rishav.gidget.Models.Widget.WidgetRepoModel
import com.rishav.gidget.R
import com.rishav.gidget.Realm.AddToWidget
import com.squareup.picasso.Picasso
import io.realm.Realm

class WidgetRepoRemoteService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return WidgetRepoRemoteViewsFactory(applicationContext, intent!!)
    }
}

class WidgetRepoRemoteViewsFactory(private val context: Context, intent: Intent) :
    RemoteViewsService.RemoteViewsFactory {
    private var dataSource: MutableList<AddToWidget> = mutableListOf()
    private lateinit var item: WidgetRepoModel
    private var appWidgetId = 0

    init {
        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
    }

    override fun onCreate() {
        val temp = AddToWidget()
        temp.username = "asdoasd"
        temp.name = "asdoaksd"
        temp.avatarUrl = "https://avatars.githubusercontent.com/u/15610556?"
        temp.message = "random message"
        temp.date = "today"

        dataSource.add(temp)
    }

    override fun onDataSetChanged() {
        val realm: Realm = Realm.getDefaultInstance()
        val result = realm.where(AddToWidget::class.java).findAll()

        dataSource = result
    }

    override fun onDestroy() = dataSource.clear()

    override fun getCount(): Int = dataSource.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.appwidget_recycler_item)
        val currentItem = dataSource[position]
        println(currentItem)

        views.setTextViewText(R.id.appwidgetRecyclerViewItemUsername, currentItem.username)
        views.setTextViewText(R.id.appwidgetRecyclerViewItemRepoName, currentItem.name)
        views.setTextViewText(R.id.appwidgetRecyclerViewItemMessage, currentItem.message)
        views.setTextViewText(R.id.appwidgetRecyclerViewItemDate, currentItem.date)

        val bitmap: Bitmap = Picasso.get().load(currentItem.avatarUrl).get()
        views.setImageViewBitmap(R.id.appwidgetRecyclerViewItemProfilePhoto, bitmap)

        val fillIntent = Intent()
        views.setOnClickFillInIntent(R.id.appwidgetRecyclerViewItemUsername, fillIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}
