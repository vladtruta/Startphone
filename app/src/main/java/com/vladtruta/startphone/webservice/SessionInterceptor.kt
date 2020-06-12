package com.vladtruta.startphone.webservice

import okhttp3.Interceptor
import okhttp3.Response

class SessionInterceptor : Interceptor {
    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val requestBuilder = original.newBuilder()
       // if (SessionUtils.isUserLoggedIn()) {
            //requestBuilder.addHeader(HEADER_AUTHORIZATION, SessionUtils.getAuthorizationToken()!!)
        //}

        return chain.proceed(requestBuilder.build())
    }
}