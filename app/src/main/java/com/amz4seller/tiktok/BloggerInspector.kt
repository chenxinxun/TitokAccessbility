package com.amz4seller.tiktok

import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

class BloggerInspector:Inspector {
    private var blogger = Blogger()
    private var currentFollower = Blogger()
    private var isBlogger = true
    private var followes = ArrayList<Blogger>()
    private var TAG = "Tiktok log"
    override fun resolveLayout(node: AccessibilityNodeInfo) {
        if(node.childCount > 0) {
            val root = node.getChild(0)?:return
            //必须先执行 有利于判断是 blogger or follower
            val backActionImage = resolveNoteAndBack(root)
            val followerLayout = resolveFollowersLayout(root)?:return
            val number = resolveBloggerNumberView(followerLayout) ?: return

            /*if(followes.size == 0) {
               blogger.followerNumber = number
               Log.d(TAG, "click ${blogger}")
               followerLayout.performAction(AccessibilityNodeInfo.ACTION_CLICK)
           }*/

            if(isBlogger) {
                blogger.followerNumber = number
                Log.d(TAG, "click ${blogger}")
                if (!blogger.isDataWaitOk()){
                    InspectorUtils.doActionDelay(followerLayout)
                }

            } else {
                currentFollower.followerNumber = number
                followes.add(currentFollower)
                Log.d(TAG, "follower - $currentFollower")
                if (number >= InspectorSettings.followersNumbers){
                    followAction(root)
                }

                if(backActionImage !=null){
                    if (!currentFollower.isDataWaitOk()){
                        InspectorUtils.doActionDelay(backActionImage)
                        Log.d(TAG, "return to blogger followers list")
                    }
                }
            }

        }
    }

    /**
     * 点击成为粉丝
     * case 1:  val nodes = node.findAccessibilityNodeInfosByText("Follow") 可能存在推荐关注博主 每个推荐博主都有一个 follow 造成多关注了
     */
    private fun followAction(node: AccessibilityNodeInfo){
        if(node.childCount > 6) {
            val followActionVew  = node.getChild(6)
            if(followActionVew.className == "android.widget.TextView"){
                val content = followActionVew.text?:""
                if(!TextUtils.isEmpty(content) && content == "Follow"){
                    Log.d(TAG, "click follow")
                    InspectorUtils.doActionDelay(followActionVew)
                }
            }
        }
    }

    /**
     * 主博客主页面与 粉丝的页面存在区别
     * 主博主页面是 "dmt.viewpager.DmtViewPager\$c"
     * 粉丝页面主页是  "android.widget.HorizontalScrollView"
     */
    private fun resolveNoteAndBack(container : AccessibilityNodeInfo):AccessibilityNodeInfo?{
        if(container.childCount > 0) {
            //主博客主页面与 粉丝的页面存在区别
            var toolbar :AccessibilityNodeInfo?=null
            when(container.className){
                "dmt.viewpager.DmtViewPager\$c"->{
                    isBlogger = true
                    if(container.childCount > 3){
                        toolbar = container.getChild(1)?:return null
                        val nameView = container.getChild(3)
                        setName(nameView)
                    }
                }
                "android.widget.HorizontalScrollView"->{
                    isBlogger = false
                    if(container.childCount > 2) {
                        val nameView = container.getChild(2)
                        setName(nameView)
                        toolbar = container.getChild(0) ?: return null
                    }
                }
                else ->{
                    isBlogger = false
                }
            }
            if (toolbar ==null){
                return toolbar
            }
            if(toolbar.childCount> 1){
                if(toolbar.className == "android.widget.RelativeLayout"){
                    val backImage = toolbar.getChild(0)?:return null
                    val titleView = toolbar.getChild(1)
                    return if (titleView == null){
                        backImage
                    } else {
                        setNote(titleView)
                        backImage
                    }
                }
            }
        }
        return  null
    }

    private fun setName(nameView: AccessibilityNodeInfo) {
        if (nameView.className == "android.widget.TextView") {
            if (nameView.text == null) {
                return
            }
            val name = nameView.text.toString()
            if(isBlogger){
                blogger.name = name
                Log.d(TAG, "blogger - $blogger")
            } else {
                currentFollower.name = name
                Log.d(TAG, "follower - $currentFollower")
            }
        }
    }

    /**
     * 设置博主的名称备注或者备注
     */
    private fun setNote(nameView: AccessibilityNodeInfo) {
        if (nameView.className == "android.widget.TextView") {
            if (nameView.text == null) {
                return
            }
            val name = nameView.text.toString()
            if(isBlogger){
                blogger.note = name
                Log.d(TAG, "blogger - $blogger")
            } else {
                currentFollower.note = name
                Log.d(TAG, "follower - $currentFollower")
            }
        }
    }

    /**
     * 解析粉丝数量
     */
    private fun resolveBloggerNumberView(container : AccessibilityNodeInfo):Int?{
        var real = 0
        Log.d(TAG, "resolve followers numbers")
        if (container.childCount > 1){
            val followNumberView = container.getChild(0)?:return 0
            if(followNumberView.className == "android.widget.TextView") {
                if (followNumberView.text == null) {
                    return null
                }
                val followerNumber = followNumberView.text.toString()
                real = InspectorUtils.getNumberFromFormat(followerNumber)
            }
        }
        Log.d(TAG, "resolve followers - $real")
        return real
    }

    /**
     * 解析粉丝操作模块区域
     */
    private fun resolveFollowersLayout(node: AccessibilityNodeInfo) : AccessibilityNodeInfo? {
        val nodes = node.findAccessibilityNodeInfosByText("Followers")?:return null
        var actionView: AccessibilityNodeInfo?
        nodes.forEach {
            val content = it.text?:""
            if(!TextUtils.isEmpty(content)){
                if(it.parent?.className?:"" == "android.widget.LinearLayout"){
                    actionView = it.parent
                    return actionView
                }
            }
        }
        return null
    }
}