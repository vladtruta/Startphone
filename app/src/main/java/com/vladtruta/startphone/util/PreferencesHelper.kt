package com.vladtruta.startphone.util

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class PreferencesHelper(private val application: Application, private val gson: Gson) {
    companion object {
        private const val KEY_AUTH_TOKEN = "KEY_AUTH_TOKEN"
        private const val KEY_EMAIL = "KEY_EMAIL"
        private const val KEY_HELPING_HAND_ENABLED = "KEY_HELPING_HAND_ENABLED"
        private const val KEY_VISIBLE_APPLICATIONS = "KEY_VISIBLE_APPLICATIONS"
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

    fun clearAuthorizationToken() {
        getSharedPreferences().edit { remove(KEY_AUTH_TOKEN) }
    }

    fun isUserLoggedIn(): Boolean {
        return !getAuthorizationToken().isNullOrEmpty()
    }

    fun getUserEmail(): String? {
        return getSharedPreferences().getString(KEY_EMAIL, null)
    }

    fun saveUserEmail(email: String) {
        getSharedPreferences().edit { putString(KEY_EMAIL, email) }
    }

    fun clearUserEmail() {
        getSharedPreferences().edit { remove(KEY_EMAIL) }
    }

    fun saveHelpingHandEnabled(enabled: Boolean) {
        getSharedPreferences().edit { putBoolean(KEY_HELPING_HAND_ENABLED, enabled) }
    }

    fun isHelpingHandEnabled(): Boolean {
        return getSharedPreferences().getBoolean(KEY_HELPING_HAND_ENABLED, false)
    }

    fun saveVisibleApplications(applications: List<String>) {
        getSharedPreferences().edit {
            putString(
                KEY_VISIBLE_APPLICATIONS,
                gson.toJson(applications)
            )
        }
    }

    fun getVisibleApplications(): List<String> {
        val applicationsSerialized =
            getSharedPreferences().getString(KEY_VISIBLE_APPLICATIONS, "{}")
        return gson.fromJson(
            applicationsSerialized,
            object : TypeToken<List<String?>?>() {}.type
        )
    }
}