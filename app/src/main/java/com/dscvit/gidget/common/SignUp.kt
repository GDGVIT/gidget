package com.dscvit.gidget.common

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class SignUp {
    private val signedUpUserKey = "signUpUserMap"
    fun signUpUser(context: Context, username: String, name: String, photoUrl: String): Boolean {
        try {
            val userMap: MutableMap<String, String> = mutableMapOf()
            userMap["username"] = username
            userMap["name"] = name
            userMap["photoUrl"] = photoUrl
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor: SharedPreferences.Editor = prefs.edit()
            val gson = Gson()
            val signUpUserMap: String = gson.toJson(userMap)
            editor.putString(signedUpUserKey, signUpUserMap)
            editor.apply()
            return true
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to SignUp", Toast.LENGTH_LONG).show()
            return false
        }
    }

    fun isUserSignedUp(context: Context): Boolean {
        return try {
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.contains(signedUpUserKey)
        } catch (e: Exception) {
            false
        }
    }

    fun getSignedUpUserDetails(context: Context): MutableMap<String, String> {
        return try {
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            if (prefs.contains(signedUpUserKey)) {
                val gson = Gson()
                val signedUpUserMap: String = prefs.getString(signedUpUserKey, null).toString()
                val type: Type = object : TypeToken<MutableMap<String?, String?>?>() {}.type
                gson.fromJson(signedUpUserMap, type) as MutableMap<String, String>
            } else mutableMapOf()
        } catch (e: Exception) {
            Toast.makeText(context, "Can't fetch user details", Toast.LENGTH_LONG).show()
            mutableMapOf()
        }
    }
}
