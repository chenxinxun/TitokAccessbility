package com.amz4seller.tiktok.mv

import android.view.accessibility.AccessibilityNodeInfo
import com.amz4seller.tiktok.InspectorSettings
import com.amz4seller.tiktok.InspectorSettings.VIDEO_EDIT_ACTIVITY
import com.amz4seller.tiktok.InspectorSettings.VIDEO_PUBLISH_ACTIVITY
import com.amz4seller.tiktok.InspectorUtils
import com.amz4seller.tiktok.base.AbstractInspector

class MvChoosePhotoInspector: AbstractInspector() {
    private inner class ActionRecord{
        var selectVideo = false
        var editVideo = false
        var firstNext = false
        var publishVideo = false
        var postVideo = false
        fun reset(){
            selectVideo = false
            editVideo = false
            publishVideo = false
            postVideo = false
            firstNext = false
        }
    }

    private var actionRecord = ActionRecord()
    override fun resolveLayout(node: AccessibilityNodeInfo) {
        resolveSelectVideo(node)
        resolveNext(node)
        resolveVideoEdit(node)
        resolvePublish(node)
        resolvePost(node)

    }

    private fun resolvePost(node: AccessibilityNodeInfo){
        val postStep = node.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/cau")?:return
        if(postStep.size > 0){
            if(!actionRecord.postVideo){
                actionRecord.postVideo = true
                InspectorUtils.doClickActionDelayUpload(postStep[0])
            }
        }
    }

    private fun resolvePublish(node: AccessibilityNodeInfo){
        val nextStep = node.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/bzx")?:return
        if(nextStep.size > 0){
            if(!actionRecord.publishVideo){
                actionRecord.publishVideo = true
                InspectorUtils.doClickActionDelayUpload(nextStep[0])
            }
        }
    }

    private fun resolveVideoEdit(node: AccessibilityNodeInfo){
        val nextStep = node.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/de5")?:return
        if(nextStep.size > 0){
            if(!actionRecord.editVideo){
                actionRecord.editVideo = true
                InspectorUtils.doClickActionDelayUpload(nextStep[0])
            }

        }
    }

    private fun resolveNext(node: AccessibilityNodeInfo){
        val nextStep = node.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/d32")?:return
        if(nextStep.size > 0){
            if(!actionRecord.firstNext){
                actionRecord.firstNext = true
                InspectorUtils.doClickActionDelayUpload(nextStep[0])
            }

        }
    }

    private fun resolveSelectVideo(node: AccessibilityNodeInfo){
        val videoRecyclerView = node.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/au1")?:return
        if(videoRecyclerView.size > 0){
            if(videoRecyclerView[0].childCount> 0){
                val recyclerView = videoRecyclerView[0]?:return
                if(recyclerView.childCount > 0){
                    val videoItem = recyclerView.getChild(0)?:return
                    val check = videoItem.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/azm")?:return
                    if(videoItem.childCount > 1){
                        if(!actionRecord.selectVideo){
                            actionRecord.selectVideo = true
                            InspectorUtils.doClickActionDelayUpload(check[0])
                        }
                    }
                }

            }

        }
    }



    override fun matchActivity(activityName: String): Boolean {
        if(activityName.contains("com.")){
            isMatchPage = activityName == InspectorSettings.MV_CHOOSE_ACTIVITY || activityName == VIDEO_EDIT_ACTIVITY || activityName == VIDEO_PUBLISH_ACTIVITY

        }
        return isMatchPage
    }

    override fun initState() {
        actionRecord.reset()
    }
}