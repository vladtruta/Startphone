package com.vladtruta.startphone.presentation.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.vladtruta.startphone.databinding.ActivitySettingsBinding
import com.vladtruta.startphone.model.local.VisibleApplication
import com.vladtruta.startphone.presentation.adapter.VisibleApplicationAdapter
import com.vladtruta.startphone.presentation.viewmodel.SettingsViewModel
import org.koin.android.ext.android.inject


class SettingsActivity : AppCompatActivity(), VisibleApplicationAdapter.VisibleApplicationListener {

    private lateinit var binding: ActivitySettingsBinding

    private val settingsViewModel by inject<SettingsViewModel>()
    private lateinit var visibleApplicationAdapter: VisibleApplicationAdapter

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initGoogleSignIn()

        initRecyclerView()
        initObservers()
        initActions()
    }

    private fun initRecyclerView() {
        visibleApplicationAdapter = VisibleApplicationAdapter()
        visibleApplicationAdapter.listener = this
        binding.applicationsRv.adapter = visibleApplicationAdapter
    }

    private fun initGoogleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestId()
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }

    private fun initObservers() {
        settingsViewModel.signedInEmail.observe(this, Observer {
            binding.signedInEmailTv.text = it
        })

        settingsViewModel.visibleApplications.observe(this, Observer {
            it ?: return@Observer

            visibleApplicationAdapter.submitList(it)
        })
    }

    private fun initActions() {
        binding.backMb.setOnClickListener {
            finish()
        }

        binding.logoutMb.setOnClickListener {
            signOut()
        }
    }

    override fun onVisibleApplicationCheckedChanged(visibleApplication: VisibleApplication) {
        settingsViewModel.updateAppVisibility(
            visibleApplication.applicationInfo.packageName,
            visibleApplication.isVisible
        )
        setResult(Activity.RESULT_OK)
    }

    private fun signOut() {
        googleSignInClient.signOut()
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