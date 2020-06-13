package com.vladtruta.startphone.presentation.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.vladtruta.startphone.databinding.ActivityOnboardingBinding
import com.vladtruta.startphone.presentation.adapter.OnboardingFragmentPagerAdapter
import com.vladtruta.startphone.presentation.adapter.OnboardingFragmentPagerAdapter.Companion.HOME_TABS.*
import com.vladtruta.startphone.presentation.viewmodel.OnboardingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingFragmentPagerAdapter: FragmentPagerAdapter

    private val onboardingViewModel by viewModel<OnboardingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initObservers()
        initViewPager()
        initActions()
    }

    private fun initViewPager() {
        onboardingFragmentPagerAdapter = OnboardingFragmentPagerAdapter(supportFragmentManager)
        binding.onboardingVp.adapter = onboardingFragmentPagerAdapter
        binding.dotsIndicator.setViewPager(binding.onboardingVp)
    }

    private fun initActions() {
        binding.onboardingVp.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
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
            when(binding.onboardingVp.currentItem) {
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
    }

    private fun openLauncher() {
        val intent = Intent(this, LauncherActivity::class.java)
        startActivity(intent)
    }
}