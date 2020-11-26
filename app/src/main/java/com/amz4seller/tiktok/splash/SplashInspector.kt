package com.amz4seller.tiktok.splash

import android.view.accessibility.AccessibilityNodeInfo
import com.amz4seller.tiktok.InspectorSettings
import com.amz4seller.tiktok.InspectorUtils
import com.amz4seller.tiktok.base.AbstractInspector
import com.amz4seller.tiktok.utils.LogEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashInspector: AbstractInspector() {

    private val actionScope = CoroutineScope(Dispatchers.Default)
    override fun resolveLayout(node: AccessibilityNodeInfo) {
        resolvePush(node)
    }


    private fun resolvePush(node: AccessibilityNodeInfo){
        val pushNodes = node.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/bsc")?:return
        if(pushNodes.size > 0){
            actionScope.launch {
                delay(3000L)
                LogEx.d(LogEx.TAG_WATCH, "start push click push")
                InspectorUtils.doClickActionDelay(pushNodes[0])
            }
        }
    }

    override fun matchActivity(activityName: String): Boolean {
        if(activityName.contains("com.")){
            isMatchPage = activityName == InspectorSettings.SPLASH_ACTIVITY
        }
        return isMatchPage
    }

    override fun initState() {

    }
}