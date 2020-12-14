package com.origin.sendfix.base

import android.view.accessibility.AccessibilityNodeInfo

interface Inspector {
    fun resolveLayout(node: AccessibilityNodeInfo)
    fun matchActivity(activityName: String):Boolean
    fun initState()

}