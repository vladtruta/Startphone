package com.vladtruta.startphone.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladtruta.startphone.R
import com.vladtruta.startphone.model.local.ApplicationInfo
import com.vladtruta.startphone.repository.IAppRepo
import com.vladtruta.startphone.util.UIUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class OnboardingViewModel(private val applicationRepository: IAppRepo) : ViewModel() {

    private val _continueButtonText = MutableLiveData(UIUtils.getString(R.string.text_continue))
    val continueButtonText: LiveData<String> = _continueButtonText

    private val _continueButtonEnabled = MutableLiveData<Boolean>()
    val continueButtonEnabled: LiveData<Boolean> = _continueButtonEnabled

    private val _visibleApplications = MutableLiveData<List<ApplicationInfo>>(emptyList())

    private val _userDateOfBirth = MutableLiveData<DateTime>()
    private val _userGender = MutableLiveData<Char>()
    private val _userId = MutableLiveData<String>()
    private val _userEmail = MutableLiveData<String>()

    fun setContinueButtonText(text: String) {
        _continueButtonText.postValue(text)
    }

    fun setContinueButtonEnabled(enabled: Boolean) {
        _continueButtonEnabled.postValue(enabled)
    }

    fun setUserDateOfBirth(dateOfBirth: DateTime) {
        _userDateOfBirth.postValue(dateOfBirth)
    }

    fun setUserGender(genderAbbr: Char) {
        _userGender.postValue(genderAbbr)
    }

    fun setUserId(id: String) {
        _userId.postValue(id)
    }

    fun setUserEmail(email: String) {
        _userEmail.postValue(email)
    }

    fun addVisibleApplication(applicationInfo: ApplicationInfo) {
        viewModelScope.launch(Dispatchers.Default) {
            val applications = _visibleApplications.value ?: emptyList()
            applications.toMutableList().add(applicationInfo)
            _visibleApplications.postValue(applications)
        }
    }

    fun removeVisibleApplication(applicationInfo: ApplicationInfo) {
        viewModelScope.launch(Dispatchers.Default) {
            val applications = _visibleApplications.value ?: emptyList()
            applications.toMutableList().remove(applicationInfo)
            _visibleApplications.postValue(applications)
        }
    }

    fun signUp() {

    }
}