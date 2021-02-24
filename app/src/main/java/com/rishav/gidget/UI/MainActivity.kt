package com.rishav.gidget.UI

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.rishav.gidget.R
import com.rishav.gidget.Realm.SignUp
import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.File


class MainActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

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
        findViewById<Button>(R.id.buLogin).setOnClickListener {
            val provider = OAuthProvider.newBuilder("github.com")
            provider.scopes = object : ArrayList<String?>() {
                init {
                    add("user:email")
                }
            }

            if (mAuth!!.pendingAuthResult == null) {
                newLogin(provider)
            } else
                pendingLogin()
        }
    }

    private fun newLogin(provider: OAuthProvider.Builder) {
        mAuth!!.startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener {

                Realm.init(applicationContext)
                Realm.setDefaultConfiguration(RealmConfiguration.Builder().build())
                val realm: Realm = Realm.getDefaultInstance()
                val results = realm.where(SignUp::class.java).findAll().first()
                if (results!!.email != it.user!!.email) {
                    val signUp = SignUp()
                    signUp.email = it.user!!.email
                    signUp.name = it.user!!.displayName.toString()
                    signUp.photoUrl = it.user!!.photoUrl.toString()
                    signUp.username = it.additionalUserInfo!!.username.toString()

                    realm.beginTransaction()
                    realm.copyToRealm(signUp)
                    realm.commitTransaction()
                }

                startActivity(Intent(this, FeedActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                println(it)
            }
    }

    private fun pendingLogin() {
        val pendingResultTask: Task<AuthResult> = mAuth!!.pendingAuthResult!!
        pendingResultTask.addOnSuccessListener {
            startActivity(Intent(this, FeedActivity::class.java))
            finish()
        }
            .addOnFailureListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                println(it)
            }
    }
}