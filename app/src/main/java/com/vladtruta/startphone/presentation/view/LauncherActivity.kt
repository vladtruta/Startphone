package com.vladtruta.startphone.presentation.view

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.vladtruta.startphone.R
import com.vladtruta.startphone.util.PreferencesHelper
import org.koin.android.ext.android.inject

class LauncherActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "LauncherActivity"

        private const val RC_SHOW_LAUNCHER_PICKER = 823
    }

    private val preferencesHelper by inject<PreferencesHelper>()
    private val roleManager by inject<RoleManager>()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_SHOW_LAUNCHER_PICKER -> {
                if (resultCode == Activity.RESULT_OK) {
                    showHome()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        checkDefaultLauncher()
                    } else {
                        showHome()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        if (preferencesHelper.isUserLoggedIn()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                checkDefaultLauncher()
            } else {
                showHome()
            }
        } else {
            showOnboarding()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkDefaultLauncher() {
        if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            startActivityForResult(intent, RC_SHOW_LAUNCHER_PICKER)
        }
    }

    private fun showOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun showHome() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
}