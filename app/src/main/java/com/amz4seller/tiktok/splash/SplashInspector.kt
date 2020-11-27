package com.amz4seller.tiktok.splash

import android.view.accessibility.AccessibilityNodeInfo
import com.amz4seller.tiktok.InspectorSettings
import com.amz4seller.tiktok.InspectorUtils
import com.amz4seller.tiktok.base.AbstractInspector
import com.amz4seller.tiktok.utils.BusEvent
import com.amz4seller.tiktok.utils.RxBus
import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicBoolean

class SplashInspector: AbstractInspector() {
    private var workState :AtomicBoolean = AtomicBoolean(false)
    private var downLoadDisposable: Disposable =
        RxBus.listen(BusEvent.EventDownLoadFinish::class.java).subscribe {
            workState.set(true)
        }

    override fun resolveLayout(node: AccessibilityNodeInfo) {
        resolvePush(node)
    }


    private fun resolvePush(node: AccessibilityNodeInfo){
        val pushNodes = node.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/bsc")?:return
        if(pushNodes.size > 0){
            /*上传一次后可能会持续一段时间， 等这个结束后再做后续操作*/
            val isPushNodes = node.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/d1v")
            if((isPushNodes == null || isPushNodes.size == 0) && workState.get()) {
                InspectorUtils.doClickActionDelayUpload(pushNodes[0])
            }
        }
    }

    override fun matchActivity(activityName: String): Boolean {
        if(activityName.contains("com.")){
            isMatchPage = activityName == InspectorSettings.SPLASH_ACTIVITY
        }
        return isMatchPage
    }

    override fun initState() {
        workState.set(false)
    }

    fun doRelease(){
        if(!downLoadDisposable.isDisposed){
            downLoadDisposable.dispose()
        }
    }



}