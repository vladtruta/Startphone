package com.vladtruta.startphone.presentation.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vladtruta.startphone.R
import com.vladtruta.startphone.databinding.ActivitySettingsBinding
import com.vladtruta.startphone.model.local.VisibleApplication
import com.vladtruta.startphone.presentation.adapter.VisibleApplicationAdapter
import com.vladtruta.startphone.presentation.viewmodel.SettingsViewModel
import com.vladtruta.startphone.util.UIUtils
import org.koin.android.ext.android.inject


class SettingsActivity : AppCompatActivity(), VisibleApplicationAdapter.VisibleApplicationListener {

    private lateinit var binding: ActivitySettingsBinding

    private val settingsViewModel by inject<SettingsViewModel>()
    private lateinit var visibleApplicationAdapter: VisibleApplicationAdapter

    private lateinit var signInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSignIn()

        initRecyclerView()
        initActions()
        initObservers()
    }

    private fun initRecyclerView() {
        visibleApplicationAdapter = VisibleApplicationAdapter()
        visibleApplicationAdapter.listener = this
        binding.applicationsRv.adapter = visibleApplicationAdapter
    }

    private fun initSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestId()
            .requestEmail()
            .build()
        signInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }

    private fun initActions() {
        binding.backMb.setOnClickListener {
            if (settingsViewModel.areAllAppsHidden()) {
                Toast.makeText(
                    this,
                    UIUtils.getString(R.string.select_at_least_one_app),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                binding.loadingPb.visibility = View.VISIBLE
                settingsViewModel.sendUpdateApplicationsRequest()
            }
        }

        binding.logoutMb.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun initObservers() {
        settingsViewModel.signedInEmail.observe(this, Observer {
            binding.signedInEmailTv.text = it
        })

        settingsViewModel.visibleApplications.observe(this, Observer {
            it ?: return@Observer

            visibleApplicationAdapter.submitList(it)
        })

        settingsViewModel.updateApplicationRequestSent.observe(this, Observer {
            it ?: return@Observer

            if (it == true) {
                finish()
            }
        })
    }

    override fun onVisibleApplicationCheckedChanged(visibleApplication: VisibleApplication) {
        settingsViewModel.updateAppVisibility(
            visibleApplication.applicationInfo.packageName,
            visibleApplication.isVisible
        )
        setResult(Activity.RESULT_OK)
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirm_logout_title)
            .setMessage(R.string.confirm_logout_message)
            .setPositiveButton(R.string.ok) { _, _ ->
                signOut()
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .show()
    }

    private fun signOut() {
        signInClient.signOut()
            .addOnCompleteListener {
                settingsViewModel.signOut()
                openLauncher()
            }
    }

    private fun openLauncher() {
        val intent = Intent(this, LauncherActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
}