package com.vladtruta.startphone.presentation.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.vladtruta.startphone.databinding.ActivityOnboardingBinding
import com.vladtruta.startphone.presentation.adapter.OnboardingFragmentPagerAdapter
import com.vladtruta.startphone.presentation.adapter.OnboardingFragmentPagerAdapter.Companion.HomeTabs.*
import com.vladtruta.startphone.presentation.viewmodel.OnboardingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingActivity : BaseActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingFragmentPagerAdapter: FragmentPagerAdapter

    private val onboardingViewModel by viewModel<OnboardingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)

        setContentView(binding.root)
        initViewPager()
        initObservers()
        initActions()
    }

    private fun initViewPager() {
        onboardingFragmentPagerAdapter = OnboardingFragmentPagerAdapter(supportFragmentManager)
        binding.onboardingVp.adapter = onboardingFragmentPagerAdapter
        binding.dotsIndicator.setViewPager(binding.onboardingVp)
        binding.dotsIndicator.dotsClickable = false
    }

    private fun initActions() {
        binding.onboardingVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                binding.continueMb.isEnabled = false
            }

            override fun onPageScrollStateChanged(state: Int) {
                // Do nothing
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // Do nothing
            }
        })

        binding.continueMb.setOnClickListener {
            when (binding.onboardingVp.currentItem) {
                WELCOME.ordinal -> {
                    binding.onboardingVp.currentItem = binding.onboardingVp.currentItem + 1
                }
                SIGN_UP.ordinal -> {
                    binding.onboardingVp.currentItem = binding.onboardingVp.currentItem + 1
                }
                SYSTEM_SETUP.ordinal -> {
                    binding.onboardingVp.currentItem = binding.onboardingVp.currentItem + 1
                }
                APPLICATIONS.ordinal -> {
                    binding.onboardingVp.currentItem = binding.onboardingVp.currentItem + 1
                }
                SUCCESS.ordinal -> {
                    it.isEnabled = false
                    binding.loadingPb.visibility = View.VISIBLE

                    onboardingViewModel.signUp()
                }
                else -> {
                    // Do nothing
                }
            }
        }
    }

    private fun initObservers() {
        onboardingViewModel.continueButtonText.observe(this, Observer {
            binding.continueMb.text = it
        })

        onboardingViewModel.continueButtonEnabled.observe(this, Observer {
            binding.continueMb.isEnabled = it
        })

        onboardingViewModel.signUpSuccess.observe(this, Observer {
            it.onSuccess {
                openLauncher()
            }.onFailure {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }

            binding.continueMb.isEnabled = true
            binding.loadingPb.visibility = View.GONE
        })
    }

    private fun openLauncher() {
        val intent = Intent(this, LauncherActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
}