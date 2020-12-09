package com.amz4seller.tiktok.splash

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.amz4seller.tiktok.InspectorSettings
import com.amz4seller.tiktok.InspectorUtils
import com.amz4seller.tiktok.base.AbstractInspector
import com.amz4seller.tiktok.base.ApiService
import com.amz4seller.tiktok.utils.BusEvent
import com.amz4seller.tiktok.utils.LogEx
import com.amz4seller.tiktok.utils.RxBus
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.concurrent.atomic.AtomicBoolean

class SplashInspector: AbstractInspector() {
    private var wakeUpAction :AtomicBoolean = AtomicBoolean(false)

    private var downLoadDisposable: Disposable =
        RxBus.listen(BusEvent.EventDownLoadFinish::class.java).subscribe {
            LogEx.d(LogEx.TAG_WATCH, "home page receive down load finish event")
            InspectorSettings.homeState.set(true)
        }

    override fun resolveLayout(node: AccessibilityNodeInfo) {
        resolvePush(node)
        resolveHome(node)
    }


    private fun resolveHome(node: AccessibilityNodeInfo){
        val homeNodes = node.findAccessibilityNodeInfosByText("Home")?:return
        if(homeNodes.size > 0){
            for (i in 0 until homeNodes.size) {
                if (homeNodes[i].text == "Home") {
                    if(wakeUpAction.get()) {
                        InspectorUtils.doClickActionDelayUpload(homeNodes[i])
                        wakeUpAction.set(false)
                    }
                }
            }
        }
    }


    private fun resolvePush(node: AccessibilityNodeInfo){

        val pushingNodes = node.findAccessibilityNodeInfosByText("%")
        val home  = node.findAccessibilityNodeInfosByText("Home")?:return
        if(home.size > 0){
            for (i in 0 until home.size) {
                if(home[i].text == "Home"){
                    val tabHome = home[i]?:return
                    val tabItem = tabHome.parent?:return
                    val tabItemParent = tabItem.parent?:return
                    //InspectorUtils.showAllElement(tabItemParent)

                    if(tabItemParent.childCount > 4){
                        val menu = tabItemParent.getChild(4)?:return
                        val pushing = pushingNodes != null &&  pushingNodes.size > 0
                        if(pushing){
                            InspectorSettings.pushing.set(true)
                        } else {
                            if(InspectorSettings.pushing.get()){
                                CoroutineScope(Dispatchers.Default).launch {
                                    try {
                                        val retrofit = InspectorUtils.getRetrofit()
                                        val service = retrofit.create(ApiService::class.java)
                                        val result = service.setUploadStatus(InspectorSettings.currentVideoId.get(), 1)
                                        val body = result.body()
                                        if(TextUtils.isEmpty(body)){
                                            LogEx.d(LogEx.TAG_WATCH, "report upload success fail")
                                        } else {
                                            LogEx.d(LogEx.TAG_WATCH, "report upload success $body")
                                        }

                                    }catch (e:Exception){
                                        e.printStackTrace()
                                        LogEx.d(LogEx.TAG_WATCH, "report request fail")
                                    } finally {
                                        InspectorSettings.currentVideoId.set(-1)
                                        InspectorSettings.pushing.set(false)
                                    }
                                }
                            }
                            if(InspectorSettings.homeState.get()){
                                LogEx.d(LogEx.TAG_WATCH, "begin auto click + ")
                                InspectorUtils.doClickActionDelayUpload(menu)
                            }
                        }

                    }
                }
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