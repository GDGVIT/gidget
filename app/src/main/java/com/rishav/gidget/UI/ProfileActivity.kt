package com.rishav.gidget.UI

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.rishav.gidget.Common.Common
import com.rishav.gidget.Common.Security
import com.rishav.gidget.Common.Utils
import com.rishav.gidget.Interface.RetroFitService
import com.rishav.gidget.Models.ProfilePage.ProfilePageModel
import com.rishav.gidget.R
import com.squareup.picasso.Picasso
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {
    lateinit var mService: RetroFitService
    lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val bundle: Bundle = intent.extras!!
        val username: String = bundle.getString("username")!!
        val owner: Boolean = bundle.getBoolean("owner")

        mService = Common.retroFitService
        mAuth = FirebaseAuth.getInstance()

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
                            mAuth.signOut()
                            Realm.removeDefaultConfiguration()
                            Toast.makeText(applicationContext, "Logged out", Toast.LENGTH_LONG)
                                .show()
                            startActivity(Intent(applicationContext, MainActivity::class.java))
                            finishAffinity()
                        }
                    } else {
                        logoutButtonText.text = "Add to widget"
                        logoutButton.setOnClickListener {
                            Utils().addToWidget(mService, true, username, "", context)
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
