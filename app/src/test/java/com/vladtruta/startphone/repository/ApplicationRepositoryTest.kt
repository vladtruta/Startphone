package com.vladtruta.startphone.repository

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.vladtruta.startphone.model.local.ApplicationInfo
import com.vladtruta.startphone.model.local.Tutorial
import com.vladtruta.startphone.model.requests.*
import com.vladtruta.startphone.model.responses.BaseResponse
import com.vladtruta.startphone.model.responses.TutorialResponse
import com.vladtruta.startphone.util.PreferencesHelper
import com.vladtruta.startphone.webservice.IAppApi
import com.vladtruta.startphone.work.WorkHelper
import io.mockk.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ApplicationRepositoryTest {

    private lateinit var applicationApi: IAppApi
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var workHelper: WorkHelper

    private lateinit var testDispatcher: CoroutineDispatcher
    private lateinit var applicationRepository: ApplicationRepository

    @Before
    fun setUp() {
        applicationApi = mockk()
        preferencesHelper = mockk()
        workHelper = mockk()
        testDispatcher = TestCoroutineDispatcher()
        applicationRepository =
            ApplicationRepository(applicationApi, preferencesHelper, workHelper, testDispatcher)
    }

    @Test
    fun getTutorialsForPackageName_packageNameValid_tutorialResponseValid_shouldReturnListOfTutorials() =
        runBlockingTest {
            val tutorialResponses = listOf(
                TutorialResponse(
                    1,
                    "com.android.contacts",
                    "How do I add someone to my address book?",
                    "http://truta.ro/licenta/how_to_add_address_book.mp4"
                ),
                TutorialResponse(
                    1,
                    "com.android.contacts",
                    "How do I call someone from my address book?",
                    "http://truta.ro/licenta/how_to_call_from_address_book.mp4"
                )
            )
            coEvery { applicationApi.getTutorialsForPackageName(ofType(String::class)) } returns BaseResponse(
                true,
                tutorialResponses
            )

            val tutorials = applicationRepository.getTutorialsForPackageName("com.android.contacts")
            val tutorialsConverted = tutorialResponses.map { it.toTutorial() }
            Assert.assertEquals(tutorials, tutorialsConverted)

            coVerify {
                applicationApi.getTutorialsForPackageName("com.android.contacts")
            }
            confirmVerified(applicationApi)
        }

    @Test
    fun getTutorialsForPackageName_packageNameInvalid_shouldReturnEmptyList() =
        runBlockingTest {
            coEvery { applicationApi.getTutorialsForPackageName(ofType(String::class)) } returns BaseResponse(
                true,
                emptyList()
            )

            val tutorials = applicationRepository.getTutorialsForPackageName("INVALID")
            Assert.assertEquals(tutorials, emptyList<Tutorial>())

            coVerify {
                applicationApi.getTutorialsForPackageName("INVALID")
            }
            confirmVerified(applicationApi)
        }

    @Test
    fun getTutorialsForPackageName_operationNotSuccessful_shouldThrowError() =
        runBlockingTest {
            coEvery { applicationApi.getTutorialsForPackageName(ofType(String::class)) } returns BaseResponse(
                false,
                emptyList(),
                "Unexpected API Failure"
            )

            try {
                applicationRepository.getTutorialsForPackageName("com.android.contacts")
                Assert.fail("applicationRepository.getTutorialsForPackageName() should throw an Exception")
            } catch (e: Exception) {
                Assert.assertEquals(e.message, "Error when trying to get tutorials")
                Assert.assertEquals(
                    e.cause?.message,
                    "API returned unsuccessful result: Unexpected API Failure"
                )
            }

            coVerify {
                applicationApi.getTutorialsForPackageName("com.android.contacts")
            }
            confirmVerified(applicationApi)
        }

    @Test
    fun getTutorialsForPackageName_packageNameValid_tutorialResponseInvalid_shouldThrowError() =
        runBlockingTest {
            val tutorialResponses = listOf(
                TutorialResponse(
                    1,
                    "com.android.contacts",
                    "How do I add someone to my address book?",
                    "http://truta.ro/licenta/how_to_add_address_book.mp4"
                ),
                TutorialResponse(
                    null,
                    "com.android.contacts",
                    "How do I call someone from my address book?",
                    "http://truta.ro/licenta/how_to_call_from_address_book.mp4"
                )
            )
            coEvery { applicationApi.getTutorialsForPackageName(ofType(String::class)) } returns BaseResponse(
                true,
                tutorialResponses
            )

            try {
                applicationRepository.getTutorialsForPackageName("com.android.contacts")
                Assert.fail("applicationRepository.getTutorialsForPackageName() should throw an Exception")
            } catch (e: Exception) {
                Assert.assertEquals(e.message, "Error when trying to get tutorials")
                Assert.assertEquals(
                    e.cause?.message,
                    "Invalid tutorial with id: null for package name: com.android.contacts"
                )
            }

            coVerify {
                applicationApi.getTutorialsForPackageName("com.android.contacts")
            }
            confirmVerified(applicationApi)
        }

    @Test
    fun updateUser_operationSuccessful_shouldSaveTokenAndEmailInSharedPreferences() =
        runBlockingTest {
            coEvery {
                applicationApi.updateUser(ofType(UserRequest::class))
            } returns BaseResponse(true, "auth_token")

            every {
                preferencesHelper.saveAuthorizationToken(ofType(String::class))
            } just Runs

            every {
                preferencesHelper.saveUserEmail(ofType(String::class))
            } just Runs

            applicationRepository.updateUser("1", "email@gmail.com", 'M', LocalDate.now())

            coVerify {
                applicationApi.updateUser(ofType(UserRequest::class))
                preferencesHelper.saveAuthorizationToken("auth_token")
                preferencesHelper.saveUserEmail("email@gmail.com")
            }
            confirmVerified(applicationApi, preferencesHelper)
        }

    @Test
    fun updateUser_operationNotSuccessful_shouldThrowTheErrorFromApiResponse() = runBlockingTest {
        coEvery {
            applicationApi.updateUser(ofType(UserRequest::class))
        } returns BaseResponse(false, "auth_token", "API Error")

        try {
            applicationRepository.updateUser("1", "email@gmail.com", 'M', LocalDate.now())
            Assert.fail("applicationRepository.updateUser() should throw an Exception")
        } catch (e: Exception) {
            Assert.assertEquals(e.message, "Error when trying to sign up")
            Assert.assertEquals(e.cause?.message, "API Error")
        }

        coVerify {
            applicationApi.updateUser(ofType(UserRequest::class))
        }
        confirmVerified(applicationApi)
    }

    @Test
    fun updateApplications_operationSuccessful_shouldFinishWithoutException() =
        runBlockingTest {
            coEvery {
                applicationApi.updateApplications(ofType(ApplicationListRequest::class))
            } returns BaseResponse(true, "", null)

            val applications = listOf(
                ApplicationInfo(
                    "com.android.contacts",
                    "Contacts",
                    ColorDrawable(Color.TRANSPARENT)
                ),
                ApplicationInfo(
                    "com.android.dialer",
                    "Dialer",
                    ColorDrawable(Color.TRANSPARENT)
                )
            )

            applicationRepository.updateApplications(applications)

            coVerify {
                applicationApi.updateApplications(ofType(ApplicationListRequest::class))
            }
            confirmVerified(applicationApi)
        }

    @Test
    fun updateApplications_operationNotSuccessful_shouldEnqueueWorkThenThrowException() =
        runBlockingTest {
            coEvery {
                applicationApi.updateApplications(ofType(ApplicationListRequest::class))
            } returns BaseResponse(false, "", "Unexpected API Failure")

            coEvery {
                workHelper.enqueueApplicationsRequest(any())
            } just Runs

            val applications = listOf(
                ApplicationInfo(
                    "com.android.contacts",
                    "Contacts",
                    ColorDrawable(Color.TRANSPARENT)
                ),
                ApplicationInfo(
                    "com.android.dialer",
                    "Dialer",
                    ColorDrawable(Color.TRANSPARENT)
                )
            )
            try {
                applicationRepository.updateApplications(applications)
                Assert.fail("applicationRepository.updateApplications() should throw an Exception")
            } catch (e: Exception) {
                Assert.assertEquals(e.message, "Error when trying to send applications")
                Assert.assertEquals(e.cause?.message, "Unexpected API Failure")
            }

            coVerify {
                applicationApi.updateApplications(ofType(ApplicationListRequest::class))
                workHelper.enqueueApplicationsRequest(applications)
            }
            confirmVerified(applicationApi, workHelper)
        }

    @Test
    fun updateMissingTutorial_operationSuccessful_shouldFinishWithoutException() =
        runBlockingTest {
            coEvery {
                applicationApi.updateMissingTutorial(ofType(MissingTutorialRequest::class))
            } returns BaseResponse(true, "", null)

            applicationRepository.updateMissingTutorial("com.android.apps.messaging")

            coVerify {
                applicationApi.updateMissingTutorial(ofType(MissingTutorialRequest::class))
            }
            confirmVerified(applicationApi)
        }

    @Test
    fun updateMissingTutorial_operationNotSuccessful_shouldEnqueueWorkThenThrowException() =
        runBlockingTest {
            coEvery {
                applicationApi.updateMissingTutorial(ofType(MissingTutorialRequest::class))
            } returns BaseResponse(false, "", "Unexpected API Failure")

            coEvery {
                workHelper.enqueueMissingTutorialRequest(ofType(String::class))
            } just Runs

            try {
                applicationRepository.updateMissingTutorial("com.android.apps.messaging")
                Assert.fail("applicationRepository.updateMissingTutorial() should throw an Exception")
            } catch (e: Exception) {
                Assert.assertEquals(e.message, "Error when trying to send missing tutorial")
                Assert.assertEquals(e.cause?.message, "Unexpected API Failure")
            }

            coVerify {
                applicationApi.updateMissingTutorial(ofType(MissingTutorialRequest::class))
                workHelper.enqueueMissingTutorialRequest("com.android.apps.messaging")
            }
            confirmVerified(applicationApi, workHelper)
        }

    @Test
    fun updateWatchedTutorial_operationSuccessful_shouldFinishWithoutException() =
        runBlockingTest {
            coEvery {
                applicationApi.updateWatchedTutorial(ofType(WatchedTutorialRequest::class))
            } returns BaseResponse(true, "", null)

            applicationRepository.updateWatchedTutorial(3)

            coVerify {
                applicationApi.updateWatchedTutorial(ofType(WatchedTutorialRequest::class))
            }
            confirmVerified(applicationApi)
        }

    @Test
    fun updateWatchedTutorial_operationNotSuccessful_shouldEnqueueWorkThenThrowException() =
        runBlockingTest {
            coEvery {
                applicationApi.updateWatchedTutorial(ofType(WatchedTutorialRequest::class))
            } returns BaseResponse(false, "", "Unexpected API Failure")

            coEvery {
                workHelper.enqueueWatchedTutorialRequest(ofType(Int::class))
            } just Runs

            try {
                applicationRepository.updateWatchedTutorial(3)
                Assert.fail("applicationRepository.updateWatchedTutorial() should throw an Exception")
            } catch (e: Exception) {
                Assert.assertEquals(e.message, "Error when trying to update watched tutorial, id: 3")
                Assert.assertEquals(e.cause?.message, "Unexpected API Failure")
            }

            coVerify {
                applicationApi.updateWatchedTutorial(ofType(WatchedTutorialRequest::class))
                workHelper.enqueueWatchedTutorialRequest(3)
            }
            confirmVerified(applicationApi, workHelper)
        }

    @Test
    fun updateRatedTutorial_operationSuccessful_shouldFinishWithoutException() =
        runBlockingTest {
            coEvery {
                applicationApi.updateRatedTutorial(ofType(RatedTutorialRequest::class))
            } returns BaseResponse(true, "", null)

            applicationRepository.updateRatedTutorial(3, true)

            coVerify {
                applicationApi.updateRatedTutorial(ofType(RatedTutorialRequest::class))
            }
            confirmVerified(applicationApi)
        }

    @Test
    fun updateRatedTutorial_operationNotSuccessful_shouldEnqueueWorkThenThrowException() =
        runBlockingTest {
            coEvery {
                applicationApi.updateRatedTutorial(ofType(RatedTutorialRequest::class))
            } returns BaseResponse(false, "", "Unexpected API Failure")

            coEvery {
                workHelper.enqueueRatedTutorialRequest(ofType(Int::class), ofType(Boolean::class))
            } just Runs

            try {
                applicationRepository.updateRatedTutorial(3, true)
                Assert.fail("applicationRepository.updateRatedTutorial() should throw an Exception")
            } catch (e: Exception) {
                Assert.assertEquals(e.message, "Error when trying to update rated tutorial")
                Assert.assertEquals(e.cause?.message, "Unexpected API Failure")
            }

            coVerify {
                applicationApi.updateRatedTutorial(ofType(RatedTutorialRequest::class))
                workHelper.enqueueRatedTutorialRequest(3, true)
            }
            confirmVerified(applicationApi, workHelper)
        }
}