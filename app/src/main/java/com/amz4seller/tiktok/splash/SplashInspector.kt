package com.amz4seller.tiktok.splash

import android.view.accessibility.AccessibilityNodeInfo
import com.amz4seller.tiktok.InspectorSettings
import com.amz4seller.tiktok.InspectorUtils
import com.amz4seller.tiktok.base.AbstractInspector
import com.amz4seller.tiktok.utils.BusEvent
import com.amz4seller.tiktok.utils.LogEx
import com.amz4seller.tiktok.utils.RxBus
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class SplashInspector: AbstractInspector() {
    private var workState :AtomicBoolean = AtomicBoolean(false)
    private var wakeUpAction :AtomicBoolean = AtomicBoolean(false)
    /**
     * 保持前台操作
     * */
    private val watchDogScope = CoroutineScope(Dispatchers.Default).launch {
        while (true){
            delay(10 * 60 * 1000L)
            wakeUpAction.set(true)
        }
    }
    private var downLoadDisposable: Disposable =
        RxBus.listen(BusEvent.EventDownLoadFinish::class.java).subscribe {
            LogEx.d(LogEx.TAG_WATCH, "home page receive down load finish event")
            workState.set(true)
        }

    override fun resolveLayout(node: AccessibilityNodeInfo) {
        resolvePush(node)
        resolveHome(node)
    }


    private fun resolveHome(node: AccessibilityNodeInfo){
        val homeNodes = node.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/bse")?:return
        if(homeNodes.size > 0){
            if(wakeUpAction.get()) {
                InspectorUtils.doClickActionDelayUpload(homeNodes[0])
                wakeUpAction.set(false)
            }
        }
    }


    private fun resolvePush(node: AccessibilityNodeInfo){
        val pushNodes = node.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/bsc")?:return
        if(pushNodes.size > 0){
            /*上传一次后可能会持续一段时间， 等这个结束后再做后续操作*/
            val isPushNodes = node.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/ca0")
            if((isPushNodes == null || isPushNodes.size == 0 ||  !isPushNodes[0].isVisibleToUser) && workState.get()) {
                LogEx.d(LogEx.TAG_WATCH, "begin auto click + ")
                InspectorUtils.doClickActionDelayUpload(pushNodes[0])
                workState.set(false)
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
    }

    fun doRelease(){
        if(!downLoadDisposable.isDisposed){
            downLoadDisposable.dispose()
        }
    }

}