package com.vladtruta.startphone.webservice

import com.vladtruta.startphone.model.requests.ApplicationListRequest
import com.vladtruta.startphone.model.requests.MissingTutorialRequest
import com.vladtruta.startphone.model.requests.UserRequest
import com.vladtruta.startphone.model.requests.WatchedTutorialRequest
import com.vladtruta.startphone.model.responses.BaseResponse
import com.vladtruta.startphone.model.responses.TutorialResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IAppApi {

    @GET("tutorials")
    suspend fun getTutorialsForPackageName(@Query("packageName") packageName: String): BaseResponse<List<TutorialResponse>>

    @POST("user")
    suspend fun updateUser(@Body request: UserRequest): BaseResponse<Any>

    @POST("application")
    suspend fun updateApplications(@Body request: ApplicationListRequest): BaseResponse<Any>

    @POST("missing")
    suspend fun updateMissingTutorial(@Body request: MissingTutorialRequest): BaseResponse<Any>

    @POST("watched")
    suspend fun updateWatchedTutorial(@Body request: WatchedTutorialRequest): BaseResponse<Any>
}