package com.dscvit.gidget.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.dscvit.gidget.R
import com.dscvit.gidget.common.RoundedTransformation
import com.dscvit.gidget.common.Utils
import com.dscvit.gidget.models.widget.AddToWidget
import com.squareup.picasso.Picasso

class WidgetRepoRemoteService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return WidgetRepoRemoteViewsFactory(applicationContext)
    }
}

class WidgetRepoRemoteViewsFactory(
    private val context: Context,
) :
    RemoteViewsService.RemoteViewsFactory {
    private var dataSource: ArrayList<AddToWidget> = arrayListOf()
    private val utils = Utils()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        try {
            dataSource = if (!utils.isEmpty(context))
                utils.getArrayList(context)
            else
                arrayListOf()
        } catch (error: Exception) {
            println(error)
        }
    }

    override fun onDestroy() = dataSource.clear()

    override fun getCount(): Int = dataSource.size

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.appwidget_recycler_item)
        val currentItem = dataSource[position]

        // setting the texts
        views.setTextViewText(R.id.appwidgetRecyclerViewItemUsername, currentItem.username)
        views.setTextViewText(R.id.appwidgetRecyclerViewItemRepoName, currentItem.name)
        views.setTextViewText(R.id.appwidgetRecyclerViewItemMessage, currentItem.message)
        views.setTextViewText(R.id.appwidgetRecyclerViewItemDate, currentItem.date)

        // setting profilePhoto
        val profilePhotoBitmap: Bitmap =
            Picasso.get().load(currentItem.avatarUrl).error(R.drawable.github_logo)
                .transform(RoundedTransformation(300, 0)).get()
        views.setImageViewBitmap(R.id.appwidgetRecyclerViewItemProfilePhoto, profilePhotoBitmap)

        // setting eventIcon
        views.setImageViewResource(R.id.appwidgetEventTypeIcon, currentItem.icon!!)

        // setting clickIntent
        val clickIntent = Intent(context, WidgetRepoRemoteService::class.java)
        clickIntent.action = Utils.getOnWidgetItemClickedAction()
        clickIntent.putExtra("dataSource", dataSource[position])
        views.setOnClickFillInIntent(R.id.appWidgetRecyclerItem, clickIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}
