package com.amz4seller.tiktok

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo

class FollowerInspector:Inspector {
    private lateinit var actionFollow:AccessibilityNodeInfo
    override fun resolveLayout(node: AccessibilityNodeInfo) {
        if (node == null) {
            return
        }
        if(node.childCount > 2) {
            val viewPageView = node.getChild(3)?:return
            if(viewPageView.className == "androidx.viewpager.widget.ViewPager") {
                if(viewPageView.childCount > 1) {
                    val recyclerView = viewPageView.getChild(1)?:return
                    if(recyclerView.className == "androidx.recyclerview.widget.RecyclerView") {
                        val currentSize = recyclerView.childCount
                        if(currentSize > 0) {
                            val followerItem = recyclerView.getChild(0)?:return
                            if(followerItem.childCount>2){
                                val followerNameView = followerItem.getChild(1)?:return
                                if(followerNameView.className == "android.widget.TextView"){
                                    var selectName = followerNameView.text.toString()
                                }

                                val followerActionButton = followerItem.getChild(2)?:return
                                if(followerActionButton.className == "android.widget.TextView"){
                                    val actionName = followerActionButton.text.toString()
                                    if(TextUtils.equals(actionName, "Follow")){
                                        actionFollow = followerActionButton
                                    }
                                }
                            }
                            InspectorUtils.doActionDelay(followerItem)
                        }
                    }
                }
            }
        }
    }

}