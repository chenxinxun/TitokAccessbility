package com.amz4seller.tiktok

import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.delay

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
    //    delay(InspectorSettings.delayAction)
        Thread.sleep(InspectorSettings.delayAction)
        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    fun doForwardActionDelay(node: AccessibilityNodeInfo){
        Handler(Looper.getMainLooper()).postDelayed({
            node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        }, InspectorSettings.delayAction
        )
    }
}