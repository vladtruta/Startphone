package com.vladtruta.startphone.presentation.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.vladtruta.startphone.R
import com.vladtruta.startphone.databinding.FragmentSignUpBinding
import com.vladtruta.startphone.presentation.viewmodel.OnboardingViewModel
import com.vladtruta.startphone.presentation.viewmodel.SignUpViewModel
import com.vladtruta.startphone.util.UIUtils
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignUpFragment : Fragment(), DatePicker.OnDateChangedListener {
    companion object {
        private const val INITIAL_YEAR = 1970
        private const val INITIAL_MONTH = 1
        private const val INITIAL_DAY = 1

        private const val RC_GOOGLE_SIGN_IN = 534
    }

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    private val signUpViewModel by viewModel<SignUpViewModel>()
    private val onboardingViewModel by sharedViewModel<OnboardingViewModel>()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initGoogleSignIn()
        initViews()
        initActions()
    }

    override fun onResume() {
        super.onResume()

        onboardingViewModel.setContinueButtonText(UIUtils.getString(R.string.text_continue))
        onboardingViewModel.setUserGender(
            signUpViewModel.getGenderFromId(binding.genderRg.checkedRadioButtonId)
        )
        if (onboardingViewModel.getUserDateOfBirth() == null) {
            onboardingViewModel.setUserDateOfBirth(
                signUpViewModel.getDateTimeFromParameters(
                    INITIAL_YEAR,
                    INITIAL_MONTH,
                    INITIAL_DAY
                )
            )
        }
    }

    private fun initGoogleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestId()
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)
    }

    private fun initViews() {
        binding.ageDp.init(INITIAL_YEAR, INITIAL_MONTH - 1, INITIAL_DAY, this)
    }

    override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val dateOfBirth =
            signUpViewModel.getDateTimeFromParameters(year, monthOfYear + 1, dayOfMonth)
        onboardingViewModel.setUserDateOfBirth(dateOfBirth)
    }

    private fun initActions() {
        binding.genderRg.setOnCheckedChangeListener { _, checkedId ->
            val gender = signUpViewModel.getGenderFromId(checkedId)
            onboardingViewModel.setUserGender(gender)
        }

        binding.googleSignInButton.setOnClickListener {
            performGoogleSignIn()
        }
    }

    private fun performGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.let {
                onboardingViewModel.setUserId(it.id!!)
                onboardingViewModel.setUserEmail(it.email!!)

                binding.signedInEmailTv.setTextColor(UIUtils.getColor(android.R.color.holo_green_dark))
                binding.signedInEmailTv.text = it.email

                onboardingViewModel.setContinueButtonEnabled(true)
            }
        } catch (e: ApiException) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            binding.signedInEmailTv.setTextColor(UIUtils.getColor(android.R.color.holo_red_dark))
            binding.signedInEmailTv.text = UIUtils.getString(R.string.try_again)

            onboardingViewModel.setContinueButtonEnabled(false)
        }
    }
}