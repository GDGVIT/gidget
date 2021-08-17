package com.dscvit.gidget.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dscvit.gidget.R
import com.dscvit.gidget.adapters.FeedPageAdapter
import com.dscvit.gidget.animations.BounceEdgeEffectFactory
import com.dscvit.gidget.common.Common
import com.dscvit.gidget.common.FeedType
import com.dscvit.gidget.common.Security
import com.dscvit.gidget.common.SignUp
import com.dscvit.gidget.common.Utils
import com.dscvit.gidget.interfaces.RetroFitService
import com.dscvit.gidget.models.activity.feedPage.FeedPageModel
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedActivity : AppCompatActivity() {
    private lateinit var mService: RetroFitService
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: FeedPageAdapter
    private val signUp = SignUp()
    private val utils = Utils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        mService = Common.retroFitService
        val signedUpUserMap = signUp.getSignedUpUserDetails(this)

        val recyclerView: RecyclerView = findViewById(R.id.feedPageRecyclerView)
        val profilePhoto: ImageView = findViewById(R.id.feedPageProfilePhoto)
        val progressBar: ProgressBar = findViewById(R.id.feedpageProgressBar)
        val searchButton: CardView = findViewById(R.id.feedPageSearchButton)
        val emptyTextView: TextView = findViewById(R.id.feedPageEmptyTextView)
        val pullRefresh: SwipeRefreshLayout = findViewById(R.id.feedPagePullRefresh)
        val swapFollowing: Button = findViewById(R.id.feedPageSwapBtnFollowing)
        val swapMe: Button = findViewById(R.id.feedPageSwapBtnMe)
        recyclerView.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.edgeEffectFactory = BounceEdgeEffectFactory()

        getFeedList(
            recyclerView,
            progressBar, emptyTextView, swapFollowing, swapMe, signedUpUserMap
        )
        getProfilePhoto(profilePhoto, signedUpUserMap)
        navigateToSearchPage(searchButton)

        pullRefresh.setOnRefreshListener {
            recyclerView.visibility = View.GONE
            getFeedList(
                recyclerView,
                progressBar, emptyTextView, swapFollowing, swapMe, signedUpUserMap
            )
            pullRefresh.isRefreshing = false
        }

        swapButtonClicked(
            recyclerView,
            progressBar, emptyTextView, swapFollowing, swapMe, signedUpUserMap
        )
    }

    private fun getFeedList(
        recyclerView: RecyclerView,
        progressBar: ProgressBar,
        emptyTextView: TextView,
        swapFollowing: Button,
        swapMe: Button,
        signedUpUserMap: MutableMap<String, String>
    ) {
        progressBar.visibility = View.VISIBLE
        val feedType: String? = utils.getFeedType(this)
        if (feedType == null) {
            utils.saveFeedType(this, FeedType.Following)
            getFeedFollowing(signedUpUserMap, recyclerView, emptyTextView, progressBar)
        } else {
            when (feedType) {
                FeedType.Following.name -> {
                    swapFollowing.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.feedPageRecyclerItemColor)
                    swapMe.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.darkBlue)
                    getFeedFollowing(signedUpUserMap, recyclerView, emptyTextView, progressBar)
                }
                FeedType.Me.name -> {
                    swapFollowing.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.darkBlue)
                    swapMe.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.feedPageRecyclerItemColor)
                    getFeedMe(signedUpUserMap, recyclerView, emptyTextView, progressBar)
                }
            }
        }
    }

    private fun getFeedFollowing(
        signedUpUserMap: MutableMap<String, String>,
        recyclerView: RecyclerView,
        emptyTextView: TextView,
        progressBar: ProgressBar
    ) {
        mService.getFeedFollowing(
            signedUpUserMap["username"]!!,
            "token ${Security.getToken()}"
        )
            .enqueue(object : Callback<MutableList<FeedPageModel>> {
                override fun onResponse(
                    call: Call<MutableList<FeedPageModel>>,
                    response: Response<MutableList<FeedPageModel>>
                ) {
                    if (response.body()!!.isEmpty()) {
                        progressBar.visibility = View.GONE
                        emptyTextView.visibility = View.VISIBLE
                    } else {
                        emptyTextView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter = FeedPageAdapter(
                            this@FeedActivity,
                            response.body() as MutableList<FeedPageModel>
                        )

                        adapter.notifyDataSetChanged()
                        recyclerView.adapter = adapter

                        progressBar.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<MutableList<FeedPageModel>>, t: Throwable) {
                    println("Error occurred - $t")
                    progressBar.visibility = View.GONE
                    emptyTextView.visibility = View.VISIBLE
                }
            })
    }

    private fun getFeedMe(
        signedUpUserMap: MutableMap<String, String>,
        recyclerView: RecyclerView,
        emptyTextView: TextView,
        progressBar: ProgressBar
    ) {
        mService.getFeedMe(
            signedUpUserMap["username"]!!,
            "token ${Security.getToken()}"
        )
            .enqueue(object : Callback<MutableList<FeedPageModel>> {
                override fun onResponse(
                    call: Call<MutableList<FeedPageModel>>,
                    response: Response<MutableList<FeedPageModel>>
                ) {
                    if (response.body()!!.isEmpty()) {
                        progressBar.visibility = View.GONE
                        emptyTextView.visibility = View.VISIBLE
                    } else {
                        emptyTextView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter = FeedPageAdapter(
                            this@FeedActivity,
                            response.body() as MutableList<FeedPageModel>
                        )

                        adapter.notifyDataSetChanged()
                        recyclerView.adapter = adapter

                        progressBar.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<MutableList<FeedPageModel>>, t: Throwable) {
                    println("Error occurred - $t")
                    progressBar.visibility = View.GONE
                    emptyTextView.visibility = View.VISIBLE
                }
            })
    }

    private fun getProfilePhoto(
        profilePhoto: ImageView,
        signedUpUserMap: MutableMap<String, String>
    ) {
        val photoUrl = signedUpUserMap["photoUrl"]
        Picasso.get().load(photoUrl).into(profilePhoto)
        profilePhoto.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("username", signedUpUserMap["username"])
            intent.putExtra("owner", true)
            startActivity(intent)
        }
    }

    private fun swapButtonClicked(
        recyclerView: RecyclerView,
        progressBar: ProgressBar,
        emptyTextView: TextView,
        swapFollowing: Button,
        swapMe: Button,
        signedUpUserMap: MutableMap<String, String>
    ) {
        swapFollowing.setOnClickListener {
            recyclerView.visibility = View.GONE
            swapFollowing.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.feedPageRecyclerItemColor)
            swapMe.backgroundTintList = ContextCompat.getColorStateList(this, R.color.darkBlue)
            utils.saveFeedType(this, FeedType.Following)
            getFeedList(
                recyclerView,
                progressBar,
                emptyTextView,
                swapFollowing,
                swapMe,
                signedUpUserMap
            )
        }
        swapMe.setOnClickListener {
            recyclerView.visibility = View.GONE
            swapFollowing.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.darkBlue)
            swapMe.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.feedPageRecyclerItemColor)
            utils.saveFeedType(this, FeedType.Me)
            getFeedList(
                recyclerView,
                progressBar,
                emptyTextView,
                swapFollowing,
                swapMe,
                signedUpUserMap
            )
        }
    }

    private fun navigateToSearchPage(searchButton: CardView) {
        searchButton.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }
}
