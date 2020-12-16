package com.origin.sendfix.utils

import android.util.Log
import com.dianping.logan.Logan


object LogEx {
    var watchDogVisible = true
    var watchVideoVisible  = true
    const val TAG_WATCH_DOG = "watchDog"
    const val TAG_WATCH = "watchVideo"
    fun d(tag:String, msg:String){
        if (watchDogVisible && tag == TAG_WATCH_DOG){
            Log.d(tag, msg)
            Logan.w(msg, 1)
            Logan.f()
        }

        if(watchVideoVisible && tag == TAG_WATCH){
            Log.d(tag, msg)
            Logan.w(msg, 1)
            Logan.f()
        }
    }
}