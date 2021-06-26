package com.dscvit.gidget.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.dscvit.gidget.R
import com.dscvit.gidget.common.Common
import com.dscvit.gidget.common.Security
import com.dscvit.gidget.common.Utils
import com.dscvit.gidget.interfaces.RetroFitService
import com.dscvit.gidget.models.profilePage.ProfilePageModel
import com.dscvit.gidget.widget.GidgetWidget
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {
    lateinit var mService: RetroFitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val bundle: Bundle = intent.extras!!
        val username: String = bundle.getString("username")!!
        val owner: Boolean = bundle.getBoolean("owner")

        mService = Common.retroFitService

        val profilePhotoIV: ImageView = findViewById(R.id.profilePagePhoto)
        val nameTV: TextView = findViewById(R.id.profilePageName)
        val usernameTV: TextView = findViewById(R.id.profilePageUsername)
        val followersTV: TextView = findViewById(R.id.profilePageFollowerCount)
        val followingTV: TextView = findViewById(R.id.profilePageFollowingCount)
        val bioTV: TextView = findViewById(R.id.profilePageBio)
        val cityTV: TextView = findViewById(R.id.profilePageCity)
        val logoutButton: CardView = findViewById(R.id.profilePageLogoutButton)
        val logoutButtonText: TextView = findViewById(R.id.profilePageLogoutButtonText)
        val progressBar: ProgressBar = findViewById(R.id.profilepageProgressBar)

        findViewById<ImageView>(R.id.profileBackButton).setOnClickListener { finish() }

        getProfileData(
            this,
            username,
            owner,
            profilePhotoIV,
            nameTV,
            usernameTV,
            followersTV,
            followingTV,
            bioTV,
            cityTV,
            logoutButton,
            logoutButtonText,
            progressBar
        )
    }

    private fun getProfileData(
        context: Context,
        username: String,
        owner: Boolean,
        profilePhotoIV: ImageView,
        nameTV: TextView,
        usernameTV: TextView,
        followersTV: TextView,
        followingTV: TextView,
        bioTV: TextView,
        cityTV: TextView,
        logoutButton: CardView,
        logoutButtonText: TextView,
        progressBar: ProgressBar
    ) {
        val profilePageView: RelativeLayout = findViewById(R.id.profilePageSection0)
        profilePageView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        mService.getProfileInfo(
            username,
            "token ${Security.getToken()}"
        )
            .enqueue(object : Callback<ProfilePageModel> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<ProfilePageModel>,
                    response: Response<ProfilePageModel>
                ) {
                    Picasso.get().load(response.body()!!.avatar_url).into(profilePhotoIV)
                    nameTV.text = response.body()!!.name
                    usernameTV.text = "@${response.body()!!.login}"
                    followersTV.text = response.body()!!.followers.toString()
                    followingTV.text = response.body()!!.following.toString()
                    bioTV.text = response.body()!!.bio
                    cityTV.text = response.body()!!.location

                    progressBar.visibility = View.GONE
                    profilePageView.visibility = View.VISIBLE

                    if (owner) {
                        logoutButtonText.text = "Logout"
                        logoutButton.setOnClickListener {
                            val widgetIntent = Intent(context, GidgetWidget::class.java)
                            widgetIntent.action = Utils.getDeleteWidgetAction()
                            context.sendBroadcast(widgetIntent)

                            Toast.makeText(applicationContext, "Logged out", Toast.LENGTH_LONG).show()
                            startActivity(Intent(applicationContext, MainActivity::class.java))
                            finishAffinity()
                        }
                    } else {
                        logoutButtonText.text = "Add to widget"
                        logoutButton.setOnClickListener {
                            Utils().addToWidget(
                                mService,
                                true,
                                username = username,
                                name = "",
                                repoOwnerAvatarUrl = "",
                                context = context
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<ProfilePageModel>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    profilePageView.visibility = View.VISIBLE
                    println("Error occurred - $t")
                    Toast.makeText(baseContext, "Something went wrong...", Toast.LENGTH_LONG).show()
                }
            })
    }
}
