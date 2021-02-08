package com.rishav.gidget

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider


class MainActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        login()
    }

    fun login() {
        findViewById<Button>(R.id.buLogin).setOnClickListener {
            val provider = OAuthProvider.newBuilder("github.com")
            provider.scopes = object : ArrayList<String?>() {
                init {
                    add("user:email")
                }
            }

            if (mAuth!!.pendingAuthResult == null) {
                mAuth!!.startActivityForSignInWithProvider(this, provider.build())
                    .addOnSuccessListener {
                        startActivity(
                            Intent(this, PostLoginActivity::class.java)
                                .putExtra(getString(R.string.email), it.user!!.email)
                                .putExtra(getString(R.string.name), it.user!!.displayName)
                                .putExtra(getString(R.string.UID), it.user!!.uid)
                                .putExtra(getString(R.string.photoUrl), it.user!!.photoUrl)
                                .putExtra(getString(R.string.username), it.additionalUserInfo!!.username)
                        )
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                        println(it)
                    }
            } else {
                val pendingResultTask: Task<AuthResult> = mAuth!!.pendingAuthResult!!
                pendingResultTask.addOnSuccessListener {
                    startActivity(
                        Intent(this, PostLoginActivity::class.java)
                            .putExtra(getString(R.string.email), it.user!!.email)
                            .putExtra(getString(R.string.name), it.user!!.displayName)
                            .putExtra(getString(R.string.UID), it.user!!.uid)
                            .putExtra(getString(R.string.photoUrl), it.user!!.photoUrl)
                            .putExtra(getString(R.string.username), it.additionalUserInfo!!.username)
                    )
                    finish()
                }
                    .addOnFailureListener {
                        Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                        println(it)
                    }
            }
        }
    }
}