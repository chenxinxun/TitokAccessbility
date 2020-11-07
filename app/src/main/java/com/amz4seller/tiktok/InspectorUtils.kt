package com.amz4seller.tiktok

import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityNodeInfo
import java.lang.Exception

object InspectorUtils {
    fun getNumberFromFormat(num:String):Int{
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
        try {
            return num.toInt()
        } catch (e:NumberFormatException){
            e.printStackTrace()
        }
        return 0
    }

    fun doActionDelay(node: AccessibilityNodeInfo){
        Handler(Looper.getMainLooper()).postDelayed({
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }, InspectorSettings.delayAction
        )
    }
}