package com.origin.sendfix

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.origin.sendfix.mv.MvChoosePhotoInspector
import com.origin.sendfix.newrecord.RecordNewInspector
import com.origin.sendfix.splash.SplashInspector

class OriginSendFixService: AccessibilityService() {
    private lateinit var splashInspector: SplashInspector
    private lateinit var recordNewInspector: RecordNewInspector
    private lateinit var mvChoosePhotoInspector: MvChoosePhotoInspector

    override fun onCreate() {
        super.onCreate()
        splashInspector = SplashInspector()
        recordNewInspector = RecordNewInspector()
        mvChoosePhotoInspector = MvChoosePhotoInspector()

    }

    /**
     * TYPE_VIEW_CLICKED 1
     * TYPE_WINDOW_STATE_CHANGED 32
     * TYPE_WINDOW_CONTENT_CHANGED 2048
     * TYPE_VIEW_SCROLLED 4096
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {


        val currentWindow = rootInActiveWindow?:return
        //上传视频

        val name = event?.className.toString()
        val homeNodes = currentWindow.findAccessibilityNodeInfosByText("Home")
        val meNodes = currentWindow.findAccessibilityNodeInfosByText("Me")
        splashInspector.isMatchPage = homeNodes != null && homeNodes.size > 0 && meNodes!=null && meNodes.size > 0
        if(splashInspector.isMatchPage){
            splashInspector.resolveLayout(currentWindow)
        }

        recordNewInspector.resolve(currentWindow, name)
        mvChoosePhotoInspector.resolve(currentWindow, name)

        currentWindow.recycle()
    }


    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        splashInspector.doRelease()
    }
}