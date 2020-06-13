package com.vladtruta.startphone.util

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class PreferencesHelper(private val application: Application) {
    companion object {
        private const val KEY_AUTH_TOKEN = "KEY_AUTH_TOKEN"
    }

    private fun getSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(application)
    }

    fun saveAuthorizationToken(token: String) {
        getSharedPreferences().edit { putString(KEY_AUTH_TOKEN, token) }
    }

    fun getAuthorizationToken(): String? {
        return getSharedPreferences().getString(KEY_AUTH_TOKEN, null)
    }

    fun isUserLoggedIn(): Boolean {
        return !getAuthorizationToken().isNullOrEmpty()
    }
}