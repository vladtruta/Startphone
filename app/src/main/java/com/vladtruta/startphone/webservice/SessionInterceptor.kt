package com.vladtruta.startphone.webservice

import com.vladtruta.startphone.util.PreferencesHelper
import okhttp3.Interceptor
import okhttp3.Response

class SessionInterceptor(private val preferencesHelper: PreferencesHelper) : Interceptor {
    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val requestBuilder = original.newBuilder()
        if (preferencesHelper.isUserLoggedIn()) {
            requestBuilder.addHeader(HEADER_AUTHORIZATION, "Bearer ${preferencesHelper.getAuthorizationToken()!!}")
        }

        return chain.proceed(requestBuilder.build())
    }
}