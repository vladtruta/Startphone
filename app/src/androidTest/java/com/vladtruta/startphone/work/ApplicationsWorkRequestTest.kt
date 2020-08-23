package com.vladtruta.startphone.work

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.ListenableWorker.Result
import androidx.work.testing.TestListenableWorkerBuilder
import com.vladtruta.startphone.model.local.ApplicationInfo
import com.vladtruta.startphone.repository.ApplicationRepository
import com.vladtruta.startphone.repository.IAppRepo
import com.vladtruta.startphone.work.WorkHelper.Companion.TAG_SEND_APPLICATIONS
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockkClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.mock.MockProviderRule
import org.koin.test.mock.declareMock

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ApplicationsWorkRequestTest : KoinTest {

    private lateinit var applicationRepository: IAppRepo

    private val applications = listOf(
        ApplicationInfo(
            "Dialer",
            "com.android.dialer",
            ColorDrawable(Color.TRANSPARENT)
        ),
        ApplicationInfo(
            "Contacts",
            "com.android.contacts",
            ColorDrawable(Color.TRANSPARENT)
        )
    )
    private val packageNames = applications.map { it.packageName }.toTypedArray()
    private val labels = applications.map { it.label }.toTypedArray()
    private val tags = listOf(TAG_SEND_APPLICATIONS)

    private lateinit var context: Context

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        mockkClass(clazz)
    }

    @Before
    fun setUp() {
        declareMock<ApplicationRepository> { applicationRepository = this }
        loadKoinModules(module {
            single(override = true) { applicationRepository }
        })

        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testApplicationsWorkRequest_argumentsValidAndOperationSuccessful_shouldReturnSuccess() {
        val data = Data.Builder()
            .putStringArray(ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_NAMES, packageNames)
            .putStringArray(ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_LABELS, labels)
            .build()

        val worker = TestListenableWorkerBuilder<ApplicationsWorkRequest>(context)
            .setInputData(data)
            .setTags(tags)
            .setRunAttemptCount(0)
            .build()

        Assert.assertArrayEquals(
            worker.inputData.getStringArray(ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_NAMES),
            packageNames
        )
        Assert.assertArrayEquals(
            worker.inputData.getStringArray(ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_LABELS),
            labels
        )
        Assert.assertEquals(worker.tags.first(), tags[0])

        runBlocking {
            coEvery {
                applicationRepository.updateApplications(any())
            } just Runs

            val result = worker.startWork().get()
            Assert.assertThat(result, `is`(Result.success()))
        }
    }

    @Test
    fun testApplicationsWorkRequest_operationNotSuccessfulAndFirstRun_shouldReturnRetry() {
        val data = Data.Builder()
            .putStringArray(ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_NAMES, packageNames)
            .putStringArray(ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_LABELS, labels)
            .build()

        val worker = TestListenableWorkerBuilder<ApplicationsWorkRequest>(context)
            .setInputData(data)
            .setTags(tags)
            .setRunAttemptCount(0)
            .build()

        Assert.assertArrayEquals(
            worker.inputData.getStringArray(ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_NAMES),
            packageNames
        )
        Assert.assertArrayEquals(
            worker.inputData.getStringArray(ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_LABELS),
            labels
        )
        Assert.assertEquals(worker.tags.first(), tags[0])

        runBlocking {
            coEvery {
                applicationRepository.updateApplications(any())
            } throws Exception("Unexpected API Error")

            val result = worker.startWork().get()
            Assert.assertThat(result, `is`(Result.retry()))
        }
    }

    @Test
    fun testApplicationsWorkRequest_operationNotSuccessfulAndExceededRunAttemptCount_shouldReturnFailure() {
        val data = Data.Builder()
            .putStringArray(ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_NAMES, packageNames)
            .putStringArray(ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_LABELS, labels)
            .build()

        val worker = TestListenableWorkerBuilder<ApplicationsWorkRequest>(context)
            .setInputData(data)
            .setTags(tags)
            .setRunAttemptCount(4)
            .build()

        Assert.assertArrayEquals(
            worker.inputData.getStringArray(ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_NAMES),
            packageNames
        )
        Assert.assertArrayEquals(
            worker.inputData.getStringArray(ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_LABELS),
            labels
        )
        Assert.assertEquals(worker.tags.first(), tags[0])

        runBlocking {
            coEvery {
                applicationRepository.updateApplications(any())
            } throws Exception("Unexpected API Error")

            val result = worker.startWork().get()
            Assert.assertThat(result, `is`(Result.failure()))
        }
    }

    @Test
    fun testApplicationsWorkRequest_noPackageNames_shouldReturnFailure() {
        val data = Data.Builder()
            .putStringArray(ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_LABELS, labels)
            .build()

        val worker = TestListenableWorkerBuilder<ApplicationsWorkRequest>(context)
            .setInputData(data)
            .setTags(tags)
            .setRunAttemptCount(0)
            .build()

        Assert.assertArrayEquals(
            worker.inputData.getStringArray(ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_LABELS),
            labels
        )
        Assert.assertEquals(worker.tags.first(), tags[0])

        runBlocking {
            val result = worker.startWork().get()
            Assert.assertThat(result, `is`(Result.failure()))
        }
    }

    @Test
    fun testApplicationsWorkRequest_noLabels_shouldReturnFailure() {
        val data = Data.Builder()
            .putStringArray(ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_NAMES, packageNames)
            .build()

        val worker = TestListenableWorkerBuilder<ApplicationsWorkRequest>(context)
            .setInputData(data)
            .setTags(tags)
            .setRunAttemptCount(0)
            .build()

        Assert.assertArrayEquals(
            worker.inputData.getStringArray(ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_NAMES),
            packageNames
        )
        Assert.assertEquals(worker.tags.first(), tags[0])

        runBlocking {
            val result = worker.startWork().get()
            Assert.assertThat(result, `is`(Result.failure()))
        }
    }
}