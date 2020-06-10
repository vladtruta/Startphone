package com.vladtruta.startphone.di

import com.google.gson.Gson
import com.vladtruta.startphone.BuildConfig
import com.vladtruta.startphone.webservice.IOpenWeatherApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    single { provideDefaultOkhttpClient() }
    single { provideOpenWeatherRetrofit(get()) }
    single { provideOpenWeatherApi(get()) }
}

private fun provideDefaultOkhttpClient(): OkHttpClient {
    val okHttpClient = OkHttpClient.Builder()

    if (BuildConfig.DEBUG) {
        okHttpClient.addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
    }

    return okHttpClient.build()
}

private fun provideOpenWeatherRetrofit(client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BuildConfig.OPEN_WEATHER_API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .build()
}

private fun provideOpenWeatherApi(retrofit: Retrofit) = retrofit.create(IOpenWeatherApi::class.java)