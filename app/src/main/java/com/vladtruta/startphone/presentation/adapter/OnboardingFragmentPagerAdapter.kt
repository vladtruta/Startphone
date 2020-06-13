package com.vladtruta.startphone.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.vladtruta.startphone.R
import com.vladtruta.startphone.presentation.view.*
import com.vladtruta.startphone.util.UIUtils

class OnboardingFragmentPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    companion object {
        private const val ITEM_COUNT = 5

        private val ITEM_TITLES = arrayOf(
            UIUtils.getString(R.string.welcome),
            UIUtils.getString(R.string.sign_up),
            UIUtils.getString(R.string.system_setup),
            UIUtils.getString(R.string.applications),
            UIUtils.getString(R.string.success)
        )

        enum class HOME_TABS {
            WELCOME, SIGN_UP, SYSTEM_SETUP, APPLICATIONS, SUCCESS
        }
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            HOME_TABS.WELCOME.ordinal -> {
                WelcomeFragment()
            }
            HOME_TABS.SIGN_UP.ordinal -> {
                SignUpFragment()
            }
            HOME_TABS.SYSTEM_SETUP.ordinal -> {
                SystemSetupFragment()
            }
            HOME_TABS.APPLICATIONS.ordinal -> {
                VisibleAppsFragment()
            }
            HOME_TABS.SUCCESS.ordinal -> {
                SuccessFragment()
            }
            else -> {
                WelcomeFragment()
            }
        }
    }

    override fun getCount(): Int {
        return ITEM_COUNT
    }
}