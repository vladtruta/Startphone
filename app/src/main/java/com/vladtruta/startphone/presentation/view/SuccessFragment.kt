package com.vladtruta.startphone.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vladtruta.startphone.R
import com.vladtruta.startphone.presentation.viewmodel.OnboardingViewModel
import com.vladtruta.startphone.util.UIUtils
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SuccessFragment : Fragment() {

    private val onboardingViewModel by sharedViewModel<OnboardingViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_success, container, false)
    }

    override fun onResume() {
        super.onResume()

        onboardingViewModel.setContinueButtonText(UIUtils.getString(R.string.open_startphone))
        onboardingViewModel.setContinueButtonEnabled(true)
    }
}