package com.vladtruta.startphone.webservice

import com.vladtruta.startphone.model.responses.ApiTutorialResponse
import com.vladtruta.startphone.model.responses.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface IAppApi {

    @GET("tutorials")
    suspend fun getTutorialsForPackageName(@Query("packageName") packageName: String): BaseResponse<List<ApiTutorialResponse>>

}