package com.rishav.gidget.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rishav.gidget.R
import com.rishav.gidget.adapters.FeedPageAdapter
import com.rishav.gidget.common.Common
import com.rishav.gidget.common.Security
import com.rishav.gidget.interfaces.RetroFitService
import com.rishav.gidget.models.feedPage.FeedPageModel
import com.rishav.gidget.realm.SignUp
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
        val progressBar: ProgressBar = findViewById(R.id.feedpageProgressBar)
        val searchButton: CardView = findViewById(R.id.feedPageSearchButton)
        val emptyTextView: TextView = findViewById(R.id.feedPageEmptyTextView)
        recyclerView.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        getFeedList(recyclerView, progressBar, emptyTextView, results!!)
        getProfilePhoto(profilePhoto, results)
        navigateToSearchPage(searchButton)
    }

    private fun getFeedList(
        recyclerView: RecyclerView,
        progressBar: ProgressBar,
        emptyTextView: TextView,
        results: SignUp
    ) {
        progressBar.visibility = View.VISIBLE
        mService.getActivityList(
            results.username,
            "token ${Security.getToken()}"
        )
            .enqueue(object : Callback<MutableList<FeedPageModel>> {
                override fun onResponse(
                    call: Call<MutableList<FeedPageModel>>,
                    response: Response<MutableList<FeedPageModel>>
                ) {
                    emptyTextView.visibility = View.GONE
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
                    emptyTextView.visibility = View.VISIBLE
                }
            })
    }

    private fun getProfilePhoto(profilePhoto: ImageView, results: SignUp) {
        val photoUrl = results.photoUrl
        Picasso.get().load(photoUrl).into(profilePhoto)
        profilePhoto.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("username", results.username)
            intent.putExtra("owner", true)
            startActivity(intent)
        }
    }

    private fun navigateToSearchPage(searchButton: CardView) {
        searchButton.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }
}