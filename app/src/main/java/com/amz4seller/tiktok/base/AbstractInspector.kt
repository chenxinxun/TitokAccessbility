package com.amz4seller.tiktok.base

import android.view.accessibility.AccessibilityNodeInfo

abstract class AbstractInspector:Inspector {
    var isMatchPage = false
    fun resolve(node: AccessibilityNodeInfo, activityName:String){
        if(matchActivity(activityName)){
            resolveLayout(node)
        } else {
            initState()
        }
    }
}