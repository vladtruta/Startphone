package com.vladtruta.startphone.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.vladtruta.startphone.R
import com.vladtruta.startphone.databinding.FragmentVisibleAppsBinding
import com.vladtruta.startphone.model.local.VisibleApplication
import com.vladtruta.startphone.presentation.adapter.VisibleApplicationAdapter
import com.vladtruta.startphone.presentation.viewmodel.OnboardingViewModel
import com.vladtruta.startphone.presentation.viewmodel.VisibleAppsViewModel
import com.vladtruta.startphone.util.UIUtils
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VisibleAppsFragment : Fragment(), VisibleApplicationAdapter.VisibleApplicationListener {

    private lateinit var binding: FragmentVisibleAppsBinding
    private lateinit var visibleApplicationAdapter: VisibleApplicationAdapter

    private val visibleAppsViewModel by viewModel<VisibleAppsViewModel>()
    private val onboardingViewModel by sharedViewModel<OnboardingViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVisibleAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onboardingViewModel.setContinueButtonText(UIUtils.getString(R.string.text_continue))

        initRecyclerView()
        initObservers()
    }

    private fun initRecyclerView() {
        visibleApplicationAdapter = VisibleApplicationAdapter()
        visibleApplicationAdapter.listener = this
        binding.applicationsRv.adapter = visibleApplicationAdapter
    }

    private fun initObservers() {
        visibleAppsViewModel.applications.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer

            visibleApplicationAdapter.submitList(it)
        })
    }

    override fun onVisibleApplicationCheckedChanged(visibleApplication: VisibleApplication) {
        onboardingViewModel.setContinueButtonEnabled(true)

        if (visibleApplication.isVisible) {
            onboardingViewModel.addVisibleApplication(visibleApplication.applicationInfo)
        } else {
            onboardingViewModel.removeVisibleApplication(visibleApplication.applicationInfo)
        }
    }
}