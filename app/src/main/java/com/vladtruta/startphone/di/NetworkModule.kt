package com.vladtruta.startphone.di

import com.google.gson.Gson
import com.vladtruta.startphone.BuildConfig
import com.vladtruta.startphone.util.PreferencesHelper
import com.vladtruta.startphone.webservice.IAppApi
import com.vladtruta.startphone.webservice.IOpenWeatherApi
import com.vladtruta.startphone.webservice.SessionInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    single { provideDefaultOkhttpClient(get()) }

    single { provideOpenWeatherApi(provideOpenWeatherRetrofit(get(), get())) }

    single { provideApplicationApi(provideApplicationRetrofit(get(), get())) }
}

private fun provideDefaultOkhttpClient(preferencesHelper: PreferencesHelper): OkHttpClient {
    val okHttpClient = OkHttpClient.Builder()

    if (BuildConfig.DEBUG) {
        okHttpClient.addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
    }

    okHttpClient.addInterceptor(SessionInterceptor(preferencesHelper))

    return okHttpClient.build()
}

private fun provideOpenWeatherRetrofit(client: OkHttpClient, gson: Gson): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BuildConfig.OPEN_WEATHER_API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}

private fun provideOpenWeatherApi(retrofit: Retrofit): IOpenWeatherApi {
    return retrofit.create(IOpenWeatherApi::class.java)
}

private fun provideApplicationRetrofit(client: OkHttpClient, gson: Gson): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BuildConfig.APPLICATION_API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}

private fun provideApplicationApi(retrofit: Retrofit): IAppApi {
    return retrofit.create(IAppApi::class.java)
}