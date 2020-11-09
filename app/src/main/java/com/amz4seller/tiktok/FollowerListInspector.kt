package com.amz4seller.tiktok

import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

class FollowerListInspector:Inspector {
    lateinit var blogger : BloggerInspector
    private var TAG = "Tiktok log"
    private var actionIndex = 0
    override fun resolveLayout(node: AccessibilityNodeInfo) {
        val viewPageView = inspectViewPager(node)?:return
        val recycleView = inspectRecycleView(viewPageView)?:return
        if( recycleView.childCount > 0) {
            if (::blogger.isInitialized){
                val items = recycleView.childCount
                if(actionIndex < items -1 && actionIndex < InspectorSettings.screenActionNum){
                    val followerItem = recycleView.getChild(actionIndex)?:return
                    if(!checkAlreadyAction(followerItem)){
                        InspectorUtils.doClickActionDelay(followerItem)
                    }
                    actionIndex++
                } else {
                    InspectorUtils.doForwardActionDelay(recycleView)
                    actionIndex = 0 // reInit
                    Log.d(TAG, "scroll forward")
                }

            }
        }

    }

    private fun checkAlreadyAction(node: AccessibilityNodeInfo):Boolean{
        val nameView = node.getChild(0)?:return false
        if(nameView.className == "android.widget.TextView"){
            val content = nameView.text?:return false
            if(!TextUtils.isEmpty(content)){
                val compare = StringBuffer()
                compare.append("@").append(content.toString())
                val name = compare.toString()
                val result = blogger.followes.filter { it.isSameBlogger(name) }
                if(result.isNotEmpty()){
                    return true
                }
            }
        }
        return false
    }

    private fun inspectViewPager(node: AccessibilityNodeInfo):AccessibilityNodeInfo?{
        if(node.childCount > 2) {
            val viewPageView = node.getChild(3) ?: return null
            if (viewPageView.className == "androidx.viewpager.widget.ViewPager") {
                return viewPageView
            }
        }
        return null
    }

    private fun inspectRecycleView(node: AccessibilityNodeInfo):AccessibilityNodeInfo?{
        if(node.childCount > 1) {
            val recyclerView = node.getChild(1) ?: return null
            if (recyclerView.className == "androidx.recyclerview.widget.RecyclerView") {
                return recyclerView
            }
        }
        return null
    }
}