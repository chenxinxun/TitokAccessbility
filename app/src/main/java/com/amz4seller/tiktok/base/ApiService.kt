package com.amz4seller.tiktok.base

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/tiktok/device")
    fun getIdentify(): Call<BaseBean>


    @GET("/tiktok/publish")
    fun getPublishUrl(@Query ("deviceId") deviceId:String): Call<UploadBean>
}