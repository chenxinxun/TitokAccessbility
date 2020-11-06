package com.amz4seller.tiktok

import android.accessibilityservice.AccessibilityService
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK
import android.widget.TabHost

class TiktokAccessbilityService: AccessibilityService() {
    var blogger = BloggerInspector()
    var follower = FollowerInspector()
    override fun onCreate() {
        super.onCreate()
        blogger.addFollowAction(follower)
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if(event?.eventType ==  AccessibilityEvent.TYPE_VIEW_CLICKED
            || event?.eventType ==  AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED
            ){
            return
        }
        Log.d("Event", event.toString())
        val currentWindow = rootInActiveWindow?:return
        blogger.resolveLayout(currentWindow)
        follower.resolveLayout(currentWindow)
        currentWindow.recycle()
    }

    override fun onInterrupt() {
    }
}