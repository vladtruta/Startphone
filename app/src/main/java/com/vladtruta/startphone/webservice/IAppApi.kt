package com.vladtruta.startphone.webservice

import com.vladtruta.startphone.model.requests.*
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
    suspend fun updateUser(@Body request: UserRequest): BaseResponse<String>

    @POST("application")
    suspend fun updateApplications(@Body request: ApplicationListRequest): BaseResponse<Any>

    @POST("missing")
    suspend fun updateMissingTutorial(@Body request: MissingTutorialRequest): BaseResponse<Any>

    @POST("watched")
    suspend fun updateWatchedTutorial(@Body request: WatchedTutorialRequest): BaseResponse<Any>

    @POST("rated")
    suspend fun updateRatedTutorial(@Body request: RatedTutorialRequest): BaseResponse<Any>
}