package com.amz4seller.tiktok

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.*

class TiktokAccessbilityService: AccessibilityService() {
    private lateinit var blogger :BloggerInspector
    private lateinit var followerList : FollowerListInspector

    override fun onCreate() {
        super.onCreate()
        blogger = BloggerInspector()
        followerList = FollowerListInspector()
        followerList.blogger = blogger
    }

    /**
     * TYPE_VIEW_CLICKED 1
     * TYPE_WINDOW_STATE_CHANGED 32
     * TYPE_WINDOW_CONTENT_CHANGED 2048
     * TYPE_VIEW_SCROLLED 4096
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d("Event type", getTypeName(event?.eventType?:0))
        if(event?.eventType ==  AccessibilityEvent.TYPE_VIEW_CLICKED
            //|| event?.eventType == TYPE_WINDOW_STATE_CHANGED
           // ||  event?.eventType ==  AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            //|| event?.eventType == TYPE_VIEW_SCROLLED
        ){
            return
        }

        if(!InspectorSettings.isServiceOn){
            return
        }


        val currentWindow = rootInActiveWindow?:return
        //解析过滤
        blogger.resolveLayout(currentWindow)
        followerList.resolveLayout(currentWindow)
        currentWindow.recycle()
    }

    private fun getTypeName(type:Int):String{
        return when(type) {
            TYPE_VIEW_CLICKED->"click"
            TYPE_VIEW_SELECTED -> "view selected"
            TYPE_WINDOW_STATE_CHANGED -> "window state changed"
            TYPE_WINDOW_CONTENT_CHANGED -> "window content changed"
            TYPE_VIEW_SCROLLED-> "view scrolled"
            else -> type.toString()
        }
    }
    override fun onInterrupt() {
    }
}