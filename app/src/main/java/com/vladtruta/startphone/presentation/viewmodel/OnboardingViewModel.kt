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

    private val visibleApplications = mutableListOf<ApplicationInfo>()
    private var userDateOfBirth: DateTime? = null
    private var userGender: Char? = null
    private var userId: String? = null
    private var userEmail: String? = null

    private val _signUpSuccess = MutableLiveData<Result<Unit>>()
    val signUpSuccess: LiveData<Result<Unit>> = _signUpSuccess

    fun setContinueButtonText(text: String) {
        _continueButtonText.postValue(text)
    }

    fun setContinueButtonEnabled(enabled: Boolean) {
        _continueButtonEnabled.postValue(enabled)
    }

    fun setUserDateOfBirth(dateOfBirth: DateTime) {
        userDateOfBirth = dateOfBirth
    }

    fun setUserGender(genderAbbr: Char) {
        userGender = genderAbbr
    }

    fun setUserId(id: String) {
        userId = id
    }

    fun setUserEmail(email: String) {
        userEmail = email
    }

    fun addVisibleApplication(applicationInfo: ApplicationInfo) {
        visibleApplications.add(applicationInfo)
    }

    fun removeVisibleApplication(applicationInfo: ApplicationInfo) {
        visibleApplications.remove(applicationInfo)
    }

    fun signUp() {
        viewModelScope.launch {
            try {
                updateApplicationsAndUser()

                preferencesHelper.saveHelpingHandEnabled(true)
                withContext(Dispatchers.Default) {
                    val visibleApplicationPackages = visibleApplications.map { it.packageName }
                    preferencesHelper.saveVisibleApplications(visibleApplicationPackages)
                }

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
                async { applicationRepository.updateApplications(visibleApplications) },
                async {
                    applicationRepository.updateUser(
                        userId!!,
                        userEmail!!,
                        userGender!!,
                        userDateOfBirth!!
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