package com.origin.sendfix.mv

import android.view.accessibility.AccessibilityNodeInfo
import com.origin.sendfix.InspectorSettings
import com.origin.sendfix.InspectorSettings.VIDEO_EDIT_ACTIVITY
import com.origin.sendfix.InspectorSettings.VIDEO_PUBLISH_ACTIVITY
import com.origin.sendfix.InspectorUtils
import com.origin.sendfix.base.AbstractInspector
import com.origin.sendfix.utils.LogEx

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

        val postStep = node.findAccessibilityNodeInfosByText("Post")?:return
        if(postStep.size > 1){
            val post = postStep[1]?:return
            LogEx.d(LogEx.TAG_WATCH, "post is clickable ${post.isClickable}")
            if(post.isClickable){
                if(!actionRecord.postVideo){
                    actionRecord.postVideo = true
                    InspectorUtils.doClickActionDelayUpload(post)
                }
            } else {
                val parent = post.parent?:return
                LogEx.d(LogEx.TAG_WATCH, "post parent is clickable ${parent.isClickable}")
                if(parent.isClickable){
                    if(!actionRecord.postVideo){
                        actionRecord.postVideo = true
                        InspectorUtils.doClickActionDelayUpload(parent)
                    }
                } else {
                    val grandFather = parent.parent?:return
                    InspectorUtils.showAllElement(grandFather)
                    if(grandFather.isClickable){
                        if(!actionRecord.postVideo){
                            actionRecord.postVideo = true
                            InspectorUtils.doClickActionDelayUpload(grandFather)
                        }
                    }
                }

            }
        }

    }

    private fun resolvePublish(node: AccessibilityNodeInfo){
        val nextStep = node.findAccessibilityNodeInfosByText("Next")?:return
        if(nextStep.size > 0){
            for (i in 0 until nextStep.size) {
                if (nextStep[i].text == "Next") {
                    val next = nextStep[i]?:return
                    if(!actionRecord.publishVideo){
                        actionRecord.publishVideo = true
                        InspectorUtils.doClickActionDelayUpload(next)
                    }
                }
            }

        }
    }

    private fun resolveVideoEdit(node: AccessibilityNodeInfo){
        val nextStep = node.findAccessibilityNodeInfosByText("Next")?:return
        if(nextStep.size > 0){
            for (i in 0 until nextStep.size) {
                if (nextStep[i].text == "Next") {
                    val next = nextStep[i]?:return
                    if(!actionRecord.editVideo){
                        actionRecord.editVideo = true
                        InspectorUtils.doClickActionDelayUpload(next)
                    }
                }
            }


        }
    }

    private fun resolveNext(node: AccessibilityNodeInfo){
        val nextNodes = node.findAccessibilityNodeInfosByText("Next")?:return
        if(nextNodes.size > 0){
            for (i in 0 until nextNodes.size) {
                if (nextNodes[i].text == "Next") {
                    val next  = nextNodes[i]?:return
                    if(!actionRecord.firstNext){
                        actionRecord.firstNext = true
                        InspectorUtils.doClickActionDelayUpload(next)
                    }
                }
            }
        }
    }

    private fun resolveSelectVideo(node: AccessibilityNodeInfo){
        val video = node.findAccessibilityNodeInfosByText("Videos")?:return
        if(video.size > 0){
            for (i in 0 until video.size) {
                if (video[i].text == "Videos") {
                    val videoParent = video[i].parent?:return
                    val videoParentParent = videoParent.parent?:return
                    //InspectorUtils.showAllElement(videoParentParent)
                    if(videoParentParent.childCount > 4){
                        val viewPager = videoParentParent.getChild(4)?:return
                        if(viewPager.childCount > 0){
                            val recyclerView = viewPager.getChild(0)?:return
                            if(recyclerView.className == "androidx.recyclerview.widget.RecyclerView")
                                if(recyclerView.childCount > 0){
                                    val videoItem = recyclerView.getChild(0)?:return
                                    if(videoItem.childCount>1){
                                        val check = videoItem.getChild(0)?:return
                                        if(!actionRecord.selectVideo){
                                            InspectorSettings.homeState.set(false)
                                            actionRecord.selectVideo = true
                                            InspectorUtils.doClickActionDelayUpload(check)
                                        }
                                    }

                                }
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