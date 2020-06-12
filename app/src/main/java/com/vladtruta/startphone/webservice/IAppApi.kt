package com.vladtruta.startphone.webservice

import com.vladtruta.startphone.model.responses.ApiTutorialResponse
import com.vladtruta.startphone.model.responses.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface IAppApi {

    @GET("tutorials/{packageName}")
    suspend fun getTutorialsForPackageName(@Path("packageName") packageName: String): BaseResponse<List<ApiTutorialResponse>>

}