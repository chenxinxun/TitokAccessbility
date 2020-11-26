package com.amz4seller.tiktok.newrecord

import android.view.accessibility.AccessibilityNodeInfo
import com.amz4seller.tiktok.InspectorSettings
import com.amz4seller.tiktok.InspectorUtils
import com.amz4seller.tiktok.base.AbstractInspector
import com.amz4seller.tiktok.utils.LogEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecordNewInspector: AbstractInspector() {
    private val actionScope = CoroutineScope(Dispatchers.Default)
    private var actionRecord= ActionRecord()
    private inner class ActionRecord{
        var uploadAction = false
        fun reset(){
            uploadAction = false
        }
    }

    override fun resolveLayout(node: AccessibilityNodeInfo) {
        resolvePush(node)
    }


    private fun resolvePush(node: AccessibilityNodeInfo){
        val uploads = node.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/sr")?:return
        if(uploads.size > 0){
            if(!actionRecord.uploadAction){
                actionRecord.uploadAction = true
                Thread.sleep(3000L)
                LogEx.d(LogEx.TAG_WATCH, "in to record new")
                InspectorUtils.doClickActionDelay(uploads[0])

            }
        }
    }

    override fun matchActivity(activityName: String): Boolean {
        if(activityName.contains("com.")){
            isMatchPage = activityName == InspectorSettings.RECORD_NEW_ACTIVITY

        }
        return isMatchPage
    }

    override fun initState() {
        actionRecord.reset()
    }
}