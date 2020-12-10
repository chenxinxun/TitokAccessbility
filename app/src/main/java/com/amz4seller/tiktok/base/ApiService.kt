package com.amz4seller.tiktok.base

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/tiktok/device")
    suspend fun getIdentifyAsync(): Response<BaseBean>


    @GET("/tiktok/publish")
    fun getPublishUrl(@Query ("deviceId") deviceId:String): Call<UploadBean>

    @PUT("/tiktok/task/{videoId}/{status}")
    suspend fun setUploadStatus(@Path("videoId") videoId:Int, @Path("status")status:Int):Response<Any>
}