package com.amz4seller.tiktok

import android.accessibilityservice.AccessibilityService
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_SCROLLED
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
import android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK
import android.widget.TabHost

class TiktokAccessbilityService: AccessibilityService() {
    var blogger = BloggerInspector()
    var follower = FollowerInspector()

    /**
     * TYPE_VIEW_CLICKED 1
     * TYPE_WINDOW_STATE_CHANGED 32
     * TYPE_WINDOW_CONTENT_CHANGED 2048
     * TYPE_VIEW_SCROLLED 4096
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d("Event type", event?.eventType.toString())
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
        blogger.resolveLayout(currentWindow)
        follower.resolveLayout(currentWindow)
        currentWindow.recycle()
    }

    override fun onInterrupt() {
    }
}