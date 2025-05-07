package com.example.grow.util

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREF_NAME = "UserSession"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_USER_ID = "userId"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveLoginSession(context: Context, userId: Int) {
        val editor = getPreferences(context).edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putInt(KEY_USER_ID, userId)
        editor.apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserId(context: Context): Int {
        return getPreferences(context).getInt(KEY_USER_ID, 0)
    }

    fun clearSession(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }
}