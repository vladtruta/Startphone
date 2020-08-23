package com.vladtruta.startphone.repository

import com.vladtruta.startphone.model.responses.WeatherResponse
import com.vladtruta.startphone.webservice.IOpenWeatherApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class WeatherRepositoryTest {

    private lateinit var openWeatherApi: IOpenWeatherApi

    private lateinit var testDispatcher: CoroutineDispatcher
    private lateinit var weatherRepository: WeatherRepository

    @Before
    fun setUp() {
        openWeatherApi = mockk()
        testDispatcher = TestCoroutineDispatcher()
        weatherRepository = WeatherRepository(openWeatherApi, testDispatcher)
    }

    @Test
    fun getCurrentWeatherInfo_weatherValid_shouldReturnWeather() = runBlockingTest {
        coEvery {
            openWeatherApi.getCurrentWeatherInfo(ofType(Double::class), ofType(Double::class))
        } returns WeatherResponse(
            WeatherResponse.ApiCurrent(
                31.5,
                listOf(WeatherResponse.ApiCurrent.ApiWeather("clear", "https://icon-url.com"))
            )
        )

        val weather = weatherRepository.getCurrentWeatherInfo(40.5, 45.5)
        Assert.assertEquals(weather.main, "clear")
        Assert.assertEquals(weather.temperature, 31.5, 0.0)
        Assert.assertEquals(weather.iconUrl, "https://icon-url.com")

        coVerify {
            openWeatherApi.getCurrentWeatherInfo(40.5, 45.5)
        }
        confirmVerified(openWeatherApi)
    }

    @Test
    fun getCurrentWeatherInfo_weatherInvalid_shouldThrowException() = runBlockingTest {
        coEvery {
            openWeatherApi.getCurrentWeatherInfo(ofType(Double::class), ofType(Double::class))
        } returns WeatherResponse(
            WeatherResponse.ApiCurrent(
                null,
                listOf(WeatherResponse.ApiCurrent.ApiWeather(null, "https://icon-url.com"))
            )
        )

        try {
            weatherRepository.getCurrentWeatherInfo(40.5, 45.5)
            Assert.fail("weatherRepository.getCurrentWeatherInfo() should throw an Exception")
        } catch(e: Exception) {
            Assert.assertEquals(e.message, "Failed to get weather info")
            Assert.assertEquals(e.cause?.message, "Invalid weather info for lat: 40.5, long: 45.5")
        }

        coVerify {
            openWeatherApi.getCurrentWeatherInfo(40.5, 45.5)
        }
        confirmVerified(openWeatherApi)
    }
}