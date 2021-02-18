package com.rishav.gidget.UI.Activities

import android.annotation.SuppressLint
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rishav.gidget.Adapters.FeedPageUserActivityAdapter
import com.rishav.gidget.Common.Common
import com.rishav.gidget.Interface.RetroFitService
import com.rishav.gidget.Models.FeedPage.FeedPageModel
import com.rishav.gidget.R
import com.rishav.gidget.Realm.SignUp
import com.squareup.picasso.Picasso
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import kotlin.concurrent.thread

class FeedActivity : AppCompatActivity() {
    lateinit var mService: RetroFitService
    lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: FeedPageUserActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        mService = Common.retroFitService

        val recyclerView: RecyclerView = findViewById(R.id.feedPageRecyclerView)
        val profilePhoto: ImageView = findViewById(R.id.feedPageProfilePhoto)
        val progressBar: RelativeLayout = findViewById(R.id.loadingPanel)
        val infoButton: ImageView = findViewById(R.id.feedPageInfoButton)


        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager


        getFeedList(recyclerView, progressBar)
        getProfilePhoto(profilePhoto)
        showGitHubIconInfo(infoButton)
    }

    private fun getFeedList(recyclerView: RecyclerView, progressBar: RelativeLayout) {
        progressBar.visibility = View.VISIBLE

        mService.getActivityList()
            .enqueue(object : Callback<MutableList<FeedPageModel>> {
                override fun onResponse(
                    call: Call<MutableList<FeedPageModel>>,
                    response: Response<MutableList<FeedPageModel>>
                ) {
                    adapter = FeedPageUserActivityAdapter(
                        baseContext,
                        response.body() as MutableList<FeedPageModel>
                    )

                    adapter.notifyDataSetChanged()
                    recyclerView.adapter = adapter

                    progressBar.visibility = View.GONE
                }

                override fun onFailure(call: Call<MutableList<FeedPageModel>>, t: Throwable) {
                    println("Error occurred - $t")
                    progressBar.visibility = View.GONE
                }

            })
    }

    private fun getProfilePhoto(profilePhoto: ImageView) {
        Realm.init(applicationContext)
        val results = Realm.getDefaultInstance().where(SignUp::class.java).findAll().first()
        val photoUrl = results!!.photoUrl
        Picasso.get().load(photoUrl).into(profilePhoto)
    }

    @SuppressLint("InflateParams")
    private fun showGitHubIconInfo(infoButton: ImageView) {
        infoButton.setOnClickListener {
            val messageBoxView =
                LayoutInflater.from(this).inflate(R.layout.github_icons_alertbox, null)
            val messageBoxBuilder = AlertDialog.Builder(this).setView(messageBoxView)
            val messageBoxInstance = messageBoxBuilder.show()
            messageBoxView.setOnClickListener {
                messageBoxInstance.dismiss()
            }
        }
    }
}
