package com.dscvit.gidget.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dscvit.gidget.R
import com.dscvit.gidget.common.Common
import com.dscvit.gidget.common.Security
import com.dscvit.gidget.models.authModel.AccessToken
import com.dscvit.gidget.models.profilePage.ProfilePageModel
import com.dscvit.gidget.realm.SignUp
import io.realm.Realm
import io.realm.RealmConfiguration
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var loginButton: Button
    private val clientID: String = Security.getClientId()
    private val clientSecret: String = Security.getClientSecret()
    private val redirectUrl: String = "futurestudio://callback"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Realm.init(applicationContext)
        val config: RealmConfiguration? = Realm.getDefaultConfiguration()
        if (config != null) {
            if (File(config.path).exists()) {
                startActivity(Intent(this, FeedActivity::class.java))
                finish()
            } else
                loginButtonOnTap()
        } else
            loginButtonOnTap()
    }

    private fun loginButtonOnTap() {
        progressBar = findViewById(R.id.mainPageProgressBar)
        loginButton = findViewById(R.id.buLogin)

        loginButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            loginButton.visibility = View.INVISIBLE

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/login/oauth/authorize?client_id=$clientID&scope=user:email&redirect_uri=$redirectUrl")
            )
            startActivity(intent)
        }
    }

    override fun onResume() {
        val uri: Uri? = intent.data
        if (uri != null && uri.toString().startsWith(redirectUrl)) {
            val code: String? = uri.getQueryParameter("code")

            if (code != null) {
                Common.authService.getAccessToken(clientID, clientSecret, code, redirectUrl)
                    .enqueue(object : Callback<AccessToken> {
                        override fun onResponse(
                            call: Call<AccessToken>,
                            response: Response<AccessToken>
                        ) {
                            if (response.body()!!.access_token != null) {
                                val accessToken: String = response.body()!!.access_token!!
                                Common.retroFitService.getAuthenticatedUser("token $accessToken")
                                    .enqueue(object : Callback<ProfilePageModel> {
                                        override fun onResponse(
                                            call: Call<ProfilePageModel>,
                                            response: Response<ProfilePageModel>
                                        ) {
                                            val user = response.body()
                                            Realm.init(applicationContext)
                                            Realm.setDefaultConfiguration(
                                                RealmConfiguration.Builder().build()
                                            )
                                            val realm: Realm = Realm.getDefaultInstance()
                                            if (realm.isEmpty && user != null) {
                                                val signUp = SignUp()
                                                signUp.name = user.name!!
                                                signUp.photoUrl = user.avatar_url!!
                                                signUp.username = user.login!!
                                                realm.beginTransaction()
                                                realm.copyToRealm(signUp)
                                                realm.commitTransaction()
                                            } else {
                                                val results =
                                                    realm.where(SignUp::class.java).findAll()
                                                        .first()
                                                if (user != null && results!!.username != user.login) {
                                                    val signUp = SignUp()
                                                    signUp.name = user.name!!
                                                    signUp.photoUrl = user.avatar_url!!
                                                    signUp.username = user.login!!
                                                    realm.beginTransaction()
                                                    realm.copyToRealm(signUp)
                                                    realm.commitTransaction()
                                                }
                                            }
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Logged in",
                                                Toast.LENGTH_LONG
                                            )
                                                .show()
                                            startActivity(
                                                Intent(
                                                    this@MainActivity,
                                                    FeedActivity::class.java
                                                )
                                            )
                                            finish()
                                        }

                                        override fun onFailure(
                                            call: Call<ProfilePageModel>,
                                            t: Throwable
                                        ) {
//                                            progressBar.visibility = View.INVISIBLE
//                                            loginButton.visibility = View.VISIBLE
                                            Toast.makeText(this@MainActivity, "Login error", Toast.LENGTH_SHORT).show()
                                            println(t.message)
                                        }
                                    })
                            }
                        }

                        override fun onFailure(call: Call<AccessToken>, t: Throwable) {
//                            progressBar.visibility = View.INVISIBLE
//                            loginButton.visibility = View.VISIBLE
                            Toast.makeText(this@MainActivity, "Login error", Toast.LENGTH_SHORT).show()
                            println(t.message)
                        }
                    })
            } else if (uri.getQueryParameter("error") != null) println("Null Code")
        }
        super.onResume()
    }
}
