package com.rishav.gidget.UI

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rishav.gidget.Adapters.FeedPageAdapter
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

class FeedActivity : AppCompatActivity() {
    lateinit var mService: RetroFitService
    lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: FeedPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        Realm.init(applicationContext)
        val realm: Realm = Realm.getDefaultInstance()
        val results = realm.where(SignUp::class.java).findAll().first()

        mService = Common.retroFitService

        val recyclerView: RecyclerView = findViewById(R.id.feedPageRecyclerView)
        val profilePhoto: ImageView = findViewById(R.id.feedPageProfilePhoto)
        val progressBar: RelativeLayout = findViewById(R.id.loadingPanel)
        val infoButton: ImageView = findViewById(R.id.feedPageInfoButton)
        val searchButton: ImageView = findViewById(R.id.feedPageSearchButton)


        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager


        getFeedList(recyclerView, progressBar, results!!)
        getProfilePhoto(profilePhoto, results)
        showGitHubIconInfo(infoButton)
        navigateToSearchPage(searchButton)
    }

    private fun getFeedList(
        recyclerView: RecyclerView,
        progressBar: RelativeLayout,
        results: SignUp
    ) {
        progressBar.visibility = View.VISIBLE

        mService.getActivityList(results.username)
            .enqueue(object : Callback<MutableList<FeedPageModel>> {
                override fun onResponse(
                    call: Call<MutableList<FeedPageModel>>,
                    response: Response<MutableList<FeedPageModel>>
                ) {
                    adapter = FeedPageAdapter(
                        this@FeedActivity,
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

    private fun getProfilePhoto(profilePhoto: ImageView, results: SignUp) {
        val photoUrl = results.photoUrl
        Picasso.get().load(photoUrl).into(profilePhoto)

        profilePhoto.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
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

    private fun navigateToSearchPage(searchButton: ImageView) {
        searchButton.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }
}
