package com.origin.sendfix

import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object InspectorUtils {
    private val okHttpClient = OkHttpClient.Builder()
    init {

        okHttpClient.connectTimeout(30, TimeUnit.SECONDS)
        okHttpClient.readTimeout(30, TimeUnit.SECONDS)
        okHttpClient.writeTimeout(30, TimeUnit.SECONDS)
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        okHttpClient.addInterceptor(logging)

    }



    fun doClickActionDelay(node: AccessibilityNodeInfo){
        //阻塞 主ui线程
        Thread.sleep(InspectorSettings.delayAction)
        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        Thread.sleep(1000L)
    }

    fun doClickActionDelayUpload(node: AccessibilityNodeInfo){
        //阻塞 主ui线程
        Thread.sleep(3000L)
        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        Thread.sleep(1000L)
    }



    fun getRetrofit(): Retrofit {
        val baseUrl = "http://${InspectorSettings.HOST_IP}:8080/"
        return Retrofit.Builder()
            .client(okHttpClient.build())
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun showAllElement(node: AccessibilityNodeInfo){
        for (i in 0 until node.childCount){
            val subNode = node.getChild(i)
            if(subNode != null){
                Log.d("Type", i.toString() +" parent["+ subNode.parent.className as String +"] current->"+subNode.className as String + " text:"+node.getChild(i).text + "isScrollable:"+node.isScrollable + " isClickable" + node.isClickable)
                showAllElement(subNode)
            }
        }
    }


}