package com.vladtruta.startphone.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vladtruta.startphone.model.local.SystemSetup

class SystemSetupViewModel: ViewModel() {

    private val _setupUpdate = MutableLiveData(SystemSetup())
    val setupUpdate: LiveData<SystemSetup> = _setupUpdate

    fun setPermissionsEnabled(enabled: Boolean) {
        val current = _setupUpdate.value!!
        current.hasPermissions = enabled
        _setupUpdate.postValue(current)
    }

    fun setDrawOverlayEnabled(enabled: Boolean) {
        val current = _setupUpdate.value!!
        current.hasDrawOverlay = enabled
        _setupUpdate.postValue(current)
    }
}