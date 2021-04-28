package com.rishav.gidget.Adapters

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.annotation.RequiresApi
import com.rishav.gidget.R
import com.rishav.gidget.Realm.AddToWidget
import com.squareup.picasso.Picasso

class WidgetRepoRemoteService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return WidgetRepoRemoteViewsFactory(applicationContext, intent!!)
    }
}

class WidgetRepoRemoteViewsFactory(
    private val context: Context,
    intent: Intent,
) :
    RemoteViewsService.RemoteViewsFactory {
    private lateinit var dataSource: ArrayList<AddToWidget>
    private var appWidgetId = 0

    init {
        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        if (intent.extras != null) {
            if (intent.extras!!.containsKey("dataSource")) {
                val bundle = intent.extras!!.getBundle("dataSource")
                dataSource = bundle!!.getParcelableArrayList("dataSourceBundle")!!
            } else
                println("Doesnt contain bundle")
        } else
            println("no extras")
    }

    override fun onCreate() {}

    override fun onDataSetChanged() {}

    override fun onDestroy() = dataSource.clear()

    override fun getCount(): Int = dataSource.size // if (dataSource.size <= 10) dataSource.size else 10

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.appwidget_recycler_item)
        val currentItem = dataSource[position]

        views.setTextViewText(R.id.appwidgetRecyclerViewItemUsername, currentItem.username)
        views.setTextViewText(R.id.appwidgetRecyclerViewItemRepoName, currentItem.name)
        views.setTextViewText(R.id.appwidgetRecyclerViewItemMessage, currentItem.message)
        views.setTextViewText(R.id.appwidgetRecyclerViewItemDate, currentItem.date)

        val profilePhotoBitmap: Bitmap = Picasso.get().load(currentItem.avatarUrl).get()
        views.setImageViewBitmap(R.id.appwidgetRecyclerViewItemProfilePhoto, profilePhotoBitmap)

        views.setImageViewResource(R.id.appwidgetEventTypeIcon, currentItem.icon!!)

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

//        temp.username = "vigneshshettyin"
//        temp.name = "gdg-x/aura"
//        temp.avatarUrl = "https://avatars.githubusercontent.com/u/77713888?"
//        temp.message = "User was watching"
//        temp.date = "today"
//        temp.icon = R.drawable.ic_github_issue
//        dataSource.add(temp)
//
//        temp.username = "dependabot[bot]"
//        temp.name = "gdg-x/aura"
//        temp.avatarUrl = "https://avatars.githubusercontent.com/u/49699333?"
//        temp.message = "User created a branch / tag"
//        temp.date = "today"
//        temp.icon = R.drawable.ic_github_issue
//        dataSource.add(temp)
//
//        temp.username = "dependabot[bot]"
//        temp.name = "gdg-x/aura"
//        temp.avatarUrl = "https://avatars.githubusercontent.com/u/15610556?"
//        temp.message = "User created a branch / tag"
//        temp.date = "today"
//        temp.icon = R.drawable.ic_github_issue
//        dataSource.add(temp)
//
//        temp.username = "asdoasd"
//        temp.name = "asdoaksd"
//        temp.avatarUrl = "https://avatars.githubusercontent.com/u/15610556?"
//        temp.message = "random message"
//        temp.date = "today"
//        temp.icon = R.drawable.ic_github_issue
//        dataSource.add(temp)
//
//        temp.username = "asdoasd"
//        temp.name = "asdoaksd"
//        temp.avatarUrl = "https://avatars.githubusercontent.com/u/15610556?"
//        temp.message = "random message"
//        temp.date = "today"
//        temp.icon = R.drawable.ic_github_issue
//        dataSource.add(temp)
