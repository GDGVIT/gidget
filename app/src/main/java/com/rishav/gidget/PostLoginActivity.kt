package com.rishav.gidget

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PostLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_login)

        val bundle: Bundle = intent.extras!!
        println(bundle["username"])
    }
}