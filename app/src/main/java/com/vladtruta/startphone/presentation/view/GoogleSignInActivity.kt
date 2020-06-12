package com.vladtruta.startphone.presentation.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.vladtruta.startphone.databinding.ActivityGoogleSignInBinding


class GoogleSignInActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "GoogleSignInActivity"

        private const val RC_GOOGLE_SIGN_IN = 534
    }

    private lateinit var binding: ActivityGoogleSignInBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleSignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initGoogleSignIn()
        initViews()
        initActions()
    }

    override fun onStart() {
        super.onStart()

        checkAlreadySignedIn()
    }

    private fun initGoogleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }

    private fun initViews() {
        binding.googleSignInButton.setSize(SignInButton.SIZE_WIDE)
    }

    private fun initActions() {
        binding.googleSignInButton.setOnClickListener {
            signIn()
        }
    }

    private fun checkAlreadySignedIn() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        //updateUI(account)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            //updateUI(account)
        } catch (e: ApiException) {
            Log.e(TAG, "handleSignInResult Failure: ${e.message}", e)
            //updateUI(null)
        }
    }

    private fun getLastSignedInInformation() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            val personName = account.displayName
            val personGivenName = account.givenName
            val personFamilyName = account.familyName
            val personEmail = account.email
            val personId = account.id
            val personPhoto = account.photoUrl
        }
    }

    private fun signOut() {
        googleSignInClient.signOut()
            .addOnCompleteListener(this) {

            }
    }
}
