package com.vladtruta.startphone.presentation.view

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.vladtruta.startphone.R
import com.vladtruta.startphone.databinding.FragmentSystemSetupBinding
import com.vladtruta.startphone.presentation.viewmodel.OnboardingViewModel
import com.vladtruta.startphone.presentation.viewmodel.SystemSetupViewModel
import com.vladtruta.startphone.util.UIUtils
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class SystemSetupFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    companion object {
        private const val RC_DRAW_OVERLAY = 636
        private const val RC_SHOW_LAUNCHER_PICKER = 823

        private const val RC_PERMISSIONS = 265
        private val PERMISSIONS = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private lateinit var binding: FragmentSystemSetupBinding
    
    private val systemSetupViewModel by viewModel<SystemSetupViewModel>()
    private val onboardingViewModel by sharedViewModel<OnboardingViewModel>()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_DRAW_OVERLAY -> {
                if (resultCode == Activity.RESULT_OK) {
                    setDrawOverlayEnabled(true)
                } else {
                    setDrawOverlayEnabled(false)
                }
            }
            AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE -> {
                if (hasPermissions()) {
                    setPermissionsEnabled(true)
                } else {
                    setPermissionsEnabled(false)
                }
            }
            RC_SHOW_LAUNCHER_PICKER -> {
                if (resultCode == Activity.RESULT_OK) {
                    setDefaultLauncherEnabled(true)
                } else {
                    setDefaultLauncherEnabled(false)
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSystemSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        onboardingViewModel.setContinueButtonText(UIUtils.getString(R.string.text_continue))
        
        initActions()
        initObservers()
    }

    private fun initActions() {
        binding.requestPermissionsMb.setOnClickListener { 
            checkPermissions()
        }

        binding.requestOverlayMb.setOnClickListener {
            checkDrawOverlay()
        }

        binding.defaultLauncherMb.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                checkDefaultLauncher()
            }
        }
    }
    
    private fun initObservers() {
        systemSetupViewModel.setupUpdate.observe(viewLifecycleOwner, Observer { 
            it ?: return@Observer
            
            val shouldContinue = it.hasPermissions && it.hasDrawOverlay && it.hasDefaultLauncher
            onboardingViewModel.setContinueButtonEnabled(shouldContinue)
        })
    }

    @AfterPermissionGranted(RC_PERMISSIONS)
    private fun checkPermissions() {
        if (hasPermissions()) {
            setPermissionsEnabled(true)
        } else {
            requestPermissions()
        }
    }

    private fun hasPermissions(): Boolean {
        return EasyPermissions.hasPermissions(requireContext(), *PERMISSIONS)
    }

    private fun requestPermissions() {
        EasyPermissions.requestPermissions(
            this,
            UIUtils.getString(R.string.permissions_rationale),
            RC_PERMISSIONS,
            *PERMISSIONS
        )
    }

    override fun onPermissionsDenied(
        requestCode: Int,
        perms: List<String?>
    ) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // Only some permissions granted, do nothing
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun checkDrawOverlay() {
        if (!Settings.canDrawOverlays(requireContext())) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${activity?.packageName}}")
            )
            startActivityForResult(intent, 0)
        } else {
            setDrawOverlayEnabled(true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun checkDefaultLauncher() {
        val roleManager = activity?.getSystemService(Context.ROLE_SERVICE) as RoleManager
        if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            activity?.startActivityForResult(intent, RC_SHOW_LAUNCHER_PICKER)
        }
    }

    private fun setPermissionsEnabled(enabled: Boolean) {
        systemSetupViewModel.setPermissionsEnabled(enabled)
        
        if (enabled) {
            binding.requestPermissionsTv.setTextColor(UIUtils.getColor(android.R.color.holo_green_dark))
            binding.requestPermissionsTv.text = UIUtils.getString(R.string.success)    
        } else {
            binding.requestPermissionsTv.setTextColor(UIUtils.getColor(android.R.color.holo_red_dark))
            binding.requestPermissionsTv.text = UIUtils.getString(R.string.try_again)    
        }
    }

    private fun setDrawOverlayEnabled(enabled: Boolean) {
        systemSetupViewModel.setDrawOverlayEnabled(enabled)
        
        if (enabled) {
            binding.requestOverlayTv.setTextColor(UIUtils.getColor(android.R.color.holo_green_dark))
            binding.requestOverlayTv.text = UIUtils.getString(R.string.success)
        } else {
            binding.requestOverlayTv.setTextColor(UIUtils.getColor(android.R.color.holo_red_dark))
            binding.requestOverlayTv.text = UIUtils.getString(R.string.try_again)    
        }
    }

    private fun setDefaultLauncherEnabled(enabled: Boolean) {
        systemSetupViewModel.setDefaultLauncherEnabled(enabled)
        
        if (enabled) {
            binding.defaultLauncherTv.setTextColor(UIUtils.getColor(android.R.color.holo_green_dark))
            binding.defaultLauncherTv.text = UIUtils.getString(R.string.success)
        } else {
            binding.defaultLauncherTv.setTextColor(UIUtils.getColor(android.R.color.holo_red_dark))
            binding.defaultLauncherTv.text = UIUtils.getString(R.string.try_again)    
        }
    }
}