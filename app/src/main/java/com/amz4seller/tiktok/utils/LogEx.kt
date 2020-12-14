package com.amz4seller.tiktok.utils

import android.util.Log


object LogEx {
    var watchDogVisible = true
    var watchVideoVisible  = true
    const val TAG_WATCH_DOG = "watchDog"
    const val TAG_WATCH = "watchVideo"
    fun d(tag:String, msg:String){
        if (watchDogVisible && tag == TAG_WATCH_DOG){
            Log.d(tag, msg)
        }

        if(watchVideoVisible && tag == TAG_WATCH){
            Log.d(tag, msg)
        }
    }
}