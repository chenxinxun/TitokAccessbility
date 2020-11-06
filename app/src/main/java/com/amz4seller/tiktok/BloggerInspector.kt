package com.amz4seller.tiktok

import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

class BloggerInspector:Inspector {
    var blogger = ""
    var currentFollower = ""
    var followes = HashMap<String, Int>()
    lateinit var followAction:FollowAction
    fun addFollowAction(action:FollowAction){
        followAction = action
    }
    override fun resolveLayout(node: AccessibilityNodeInfo) {
        if(node == null) {
            return
        }
        if(node.childCount > 0) {
            val container = node.getChild(0)?:return
            val actionView = resolveBlogger(container, node)?:return
            var number = 0

            if(actionView != null){
                number = resolveBloggerNumber(actionView)
                Log.d("Tiktok log auto event", "click ${blogger} Followers")
                actionView.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            if(!TextUtils.isEmpty(currentFollower)) {
                followes[currentFollower] = number
                Log.d("Tiktok log follower", "${currentFollower} : $number")
                pressBack(container)
                if(::followAction.isInitialized){
                    followAction.onFollow(currentFollower)
                }
            }
        }
    }

    private fun pressBack(container : AccessibilityNodeInfo){

    }

    private fun resolveBloggerNumber(container : AccessibilityNodeInfo):Int{
        var real = 0
        if (container.childCount > 1){
            val followNumberView = container.getChild(0)?:return 0
            if(followNumberView.className == "android.widget.TextView") {
                val followerNumber = followNumberView.text.toString()
                if(followerNumber.contains("M")){
                    val number = followerNumber.subSequence(0, followerNumber.length-1).toString()
                    real =  (number.toFloat() * 1000_000).toInt()
                }


                if(followerNumber.contains("K")){
                    val number = followerNumber.subSequence(0, followerNumber.length-1).toString()
                    real =  (number.toFloat() * 1000).toInt()
                }

                if (followerNumber.contains("B")){
                    val number = followerNumber.subSequence(0, followerNumber.length-1).toString()
                    real =   (number.toFloat() * 1000_000_000).toInt()
                }
            }
        }

        return real
    }

    private fun resolveBlogger(container : AccessibilityNodeInfo, node: AccessibilityNodeInfo) : AccessibilityNodeInfo? {
        val nodes = node.findAccessibilityNodeInfosByText("Followers")?:return null
        val toolbar = container.getChild(1)?:return null
        var actionView : AccessibilityNodeInfo? = null
        if(toolbar.childCount > 1) {
            val titleView = toolbar.getChild(1)?:return null
            if(titleView.className == "android.widget.TextView") {
                val followerName = titleView.text.toString()
                //第一次进入查看的个人信息页标记为 blogger
                if(TextUtils.isEmpty(blogger)){
                    Log.d("Tiktok log blogger is", blogger)
                    blogger = followerName
                } else {
                    Log.d("Tiktok log follower is", blogger)
                    currentFollower = followerName
                }

                nodes.forEach {
                    val content = it.text?:""
                    if(!TextUtils.isEmpty(content)){
                        if(it.parent?.className?:"" == "android.widget.LinearLayout" && TextUtils.equals(blogger, followerName)){
                            actionView = it.parent
                            return actionView
                        }
                    }
                }
            }
        }
        return null
    }
}