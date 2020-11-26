package com.amz4seller.tiktok

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.*
import com.amz4seller.tiktok.mv.MvChoosePhotoInspector
import com.amz4seller.tiktok.newrecord.RecordNewInspector
import com.amz4seller.tiktok.splash.SplashInspector
import com.amz4seller.tiktok.utils.LogEx
import com.amz4seller.tiktok.utils.LogEx.TAG_WATCH_DOG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TiktokAccessbilityService: AccessibilityService() {
    private lateinit var blogger :BloggerInspector
    private lateinit var followerList : FollowerListInspector
    private lateinit var splashInspector: SplashInspector
    private lateinit var recordNewInspector: RecordNewInspector
    private lateinit var mvChoosePhotoInspector: MvChoosePhotoInspector
    //协程作用 Default 为新
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        blogger = BloggerInspector()
        splashInspector = SplashInspector()
        recordNewInspector = RecordNewInspector()
        mvChoosePhotoInspector = MvChoosePhotoInspector()
        scope.launch {
            blogger.startWatchDog()
        }

        followerList = FollowerListInspector()
        followerList.blogger = blogger
    }

    /**
     * TYPE_VIEW_CLICKED 1
     * TYPE_WINDOW_STATE_CHANGED 32
     * TYPE_WINDOW_CONTENT_CHANGED 2048
     * TYPE_VIEW_SCROLLED 4096
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if(event?.eventType ==  AccessibilityEvent.TYPE_VIEW_CLICKED
            //|| event?.eventType == TYPE_WINDOW_STATE_CHANGED
           // ||  event?.eventType ==  AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            //|| event?.eventType == TYPE_VIEW_SCROLLED
        ){
            return
        }

        if(!InspectorSettings.isServiceOn){
            return
        }


        val currentWindow = rootInActiveWindow?:return
        if(InspectorSettings.isUpload){
            LogEx.d(TAG_WATCH_DOG, "init upload mode")
            val name = event?.className.toString()
            val pushNodes = currentWindow.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/bsc")
            splashInspector.isMatchPage  = pushNodes != null
            if(splashInspector.isMatchPage){
                splashInspector.resolveLayout(currentWindow)
            }

            recordNewInspector.resolve(currentWindow, name)
            mvChoosePhotoInspector.resolve(currentWindow, name)
        } else {
            //解析过滤
            blogger.resolveLayout(currentWindow)
            followerList.resolveLayout(currentWindow)
        }

        currentWindow.recycle()
    }

    private fun getTypeName(type:Int):String{
        return when(type) {
            TYPE_VIEW_CLICKED->"click"
            TYPE_VIEW_SELECTED -> "view selected"
            TYPE_WINDOW_STATE_CHANGED -> "window state changed"
            TYPE_WINDOW_CONTENT_CHANGED -> "window content changed"
            TYPE_VIEW_SCROLLED-> "view scrolled"
            else -> type.toString()
        }
    }
    override fun onInterrupt() {
    }
}