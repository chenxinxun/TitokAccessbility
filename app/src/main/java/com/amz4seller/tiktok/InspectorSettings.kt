package com.amz4seller.tiktok

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object InspectorSettings {

    var deviceId = ""
    var SPLASH_ACTIVITY = "com.ss.android.ugc.aweme.splash.SplashActivity"
    var RECORD_NEW_ACTIVITY = "com.ss.android.ugc.aweme.shortvideo.ui.VideoRecordNewActivity"
    var MV_CHOOSE_ACTIVITY = "com.ss.android.ugc.aweme.shortvideo.mvtemplate.choosemedia.MvChoosePhotoActivity"
    var VIDEO_EDIT_ACTIVITY = "com.ss.android.ugc.aweme.shortvideo.cut.VECutVideoActivity"
    var VIDEO_PUBLISH_ACTIVITY = "com.ss.android.ugc.aweme.shortvideo.edit.VEVideoPublishEditActivity"


    //延时操作
    var defaultDelayAction = 5000L
    var delayAction = 5000L

    var defaultMinuteLimit = 30
    var minuteLimit =  30

    var HOST_IP = "10.12.1.58"
    var currentVideoId :AtomicInteger= AtomicInteger(-1)
    var pushing:AtomicBoolean = AtomicBoolean(false)
    var homeState : AtomicBoolean = AtomicBoolean(false)
    fun getDelaySecond():Int {
        return (delayAction / 1000).toInt()
    }

    fun getDelayDefaultSecond():Int{
        return (defaultDelayAction / 1000).toInt()
    }

}