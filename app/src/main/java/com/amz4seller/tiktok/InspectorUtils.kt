package com.amz4seller.tiktok

import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

object InspectorUtils {

    fun getNumberFromFormat(num:String):Int?{
        if(num.contains("M")){
            val number = num.subSequence(0, num.length-1).toString()
            return (number.toFloat() * 1000_000).toInt()
        }
        if(num.contains("K")){
            val number = num.subSequence(0, num.length-1).toString()
            return (number.toFloat() * 1000).toInt()
        }
        if(num.contains("B")){
            val number = num.subSequence(0, num.length-1).toString()
            return (number.toFloat() * 1000_000_000).toInt()
        }
        return try {
            num.toInt()
        } catch (e:NumberFormatException){
            null
        }
    }

    fun doClickActionDelay(node: AccessibilityNodeInfo){
        //阻塞 主ui线程
        Thread.sleep(InspectorSettings.delayAction)
        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        Thread.sleep(1000L)
    }

    fun doClickActionDelayUpload(node: AccessibilityNodeInfo){
        //阻塞 主ui线程
        Thread.sleep(3000L)
        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        Thread.sleep(1000L)
    }

    fun doClickActionDelayByTime(node: AccessibilityNodeInfo, time:Long){
        //阻塞 主ui线程
        Thread.sleep(time)
        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }


    fun doForwardActionDelay(node: AccessibilityNodeInfo){
        //阻塞 主ui线程
        Thread.sleep(InspectorSettings.delayAction)
        node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        Thread.sleep(1000L)
    }

    fun showAllElement(node: AccessibilityNodeInfo){
        for (i in 0 until node.childCount){
            val subNode = node.getChild(i)
            if(subNode != null){
                Log.d("Type", i.toString() +" parent["+ subNode.parent.className as String +"] current->"+subNode.className as String + " text:"+node.getChild(i).text + "isScrollable:"+node.isScrollable + " isClickable" + node.isClickable)
                showAllElement(subNode)
            }
        }
    }


}