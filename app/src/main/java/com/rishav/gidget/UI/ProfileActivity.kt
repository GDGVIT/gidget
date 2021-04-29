package com.rishav.gidget.UI

import android.annotation.SuppressLint
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
import com.google.firebase.auth.FirebaseAuth
import com.rishav.gidget.Common.Common
import com.rishav.gidget.Interface.RetroFitService
import com.rishav.gidget.Models.ProfilePage.ProfilePageModel
import com.rishav.gidget.R
import com.rishav.gidget.Realm.SignUp
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

        Realm.init(applicationContext)
        val realm: Realm = Realm.getDefaultInstance()
        val results = realm.where(SignUp::class.java).findAll().first()

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
        val progressBar: ProgressBar = findViewById(R.id.profilepageProgressBar)

        findViewById<ImageView>(R.id.profileBackButton).setOnClickListener { finish() }

        getProfileData(
            results!!,
            profilePhotoIV,
            nameTV,
            usernameTV,
            followersTV,
            followingTV,
            bioTV,
            cityTV,
            logoutButton,
            progressBar
        )
    }

    private fun getProfileData(
        results: SignUp,
        profilePhotoIV: ImageView,
        nameTV: TextView,
        usernameTV: TextView,
        followersTV: TextView,
        followingTV: TextView,
        bioTV: TextView,
        cityTV: TextView,
        logoutButton: CardView,
        progressBar: ProgressBar
    ) {
        val profilePageView: RelativeLayout = findViewById(R.id.profilePageSection0)
        profilePageView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        mService.getProfileInfo(results.username, System.getenv("token") ?: "null")
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

                    logoutButton.setOnClickListener {
                        mAuth.signOut()
                        Realm.removeDefaultConfiguration()
                        Toast.makeText(applicationContext, "Logged out", Toast.LENGTH_LONG).show()
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        finishAffinity()
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
