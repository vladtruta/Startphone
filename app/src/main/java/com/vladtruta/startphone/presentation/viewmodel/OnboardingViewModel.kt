package com.vladtruta.startphone.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladtruta.startphone.model.local.ApplicationInfo
import com.vladtruta.startphone.repository.IAppRepo
import com.vladtruta.startphone.util.PreferencesHelper
import kotlinx.coroutines.*
import org.joda.time.DateTime

class OnboardingViewModel(
    private val applicationRepository: IAppRepo,
    private val preferencesHelper: PreferencesHelper
) : ViewModel() {

    private val _continueButtonText = MutableLiveData<String>()
    val continueButtonText: LiveData<String> = _continueButtonText

    private val _continueButtonEnabled = MutableLiveData<Boolean>()
    val continueButtonEnabled: LiveData<Boolean> = _continueButtonEnabled

    private val _visibleApplications = MutableLiveData<List<ApplicationInfo>>(emptyList())

    private val _userDateOfBirth = MutableLiveData<DateTime>()
    private val _userGender = MutableLiveData<Char>()
    private val _userId = MutableLiveData<String>()
    private val _userEmail = MutableLiveData<String>()

    private val _signUpSuccess = MutableLiveData<Result<Unit>>()
    val signUpSuccess: LiveData<Result<Unit>> = _signUpSuccess

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
            val applications = _visibleApplications.value!!
            applications.toMutableList().add(applicationInfo)
            _visibleApplications.postValue(applications)
        }
    }

    fun removeVisibleApplication(applicationInfo: ApplicationInfo) {
        viewModelScope.launch(Dispatchers.Default) {
            val applications = _visibleApplications.value!!
            applications.toMutableList().remove(applicationInfo)
            _visibleApplications.postValue(applications)
        }
    }

    fun signUp() {
        viewModelScope.launch {
            try {
                updateApplicationsAndUser()
                _signUpSuccess.postValue(Result.success(Unit))
            } catch (e: Exception) {
                _signUpSuccess.postValue(Result.failure(e))
            }
        }
    }

    /**
     * Executes both network calls in parallel, on separate coroutines
     */
    private suspend fun updateApplicationsAndUser() {
        coroutineScope {
            val tasks = listOf(
                async { applicationRepository.updateApplications(_visibleApplications.value!!) },
                async {
                    applicationRepository.updateUser(
                        _userId.value!!,
                        _userEmail.value!!,
                        _userGender.value!!,
                        _userDateOfBirth.value!!
                    )
                }
            )
            tasks.awaitAll()
        }
    }

    fun isUserLoggedIn(): Boolean {
        return preferencesHelper.isUserLoggedIn()
    }
}