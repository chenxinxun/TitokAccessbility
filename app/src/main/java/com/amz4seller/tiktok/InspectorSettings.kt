package com.amz4seller.tiktok

import java.util.concurrent.atomic.AtomicBoolean

object InspectorSettings {
    var SPLASH_ACTIVITY = "com.ss.android.ugc.aweme.splash.SplashActivity"
    var RECORD_NEW_ACTIVITY = "com.ss.android.ugc.aweme.shortvideo.ui.VideoRecordNewActivity"
    var MV_CHOOSE_ACTIVITY = "com.ss.android.ugc.aweme.shortvideo.mvtemplate.choosemedia.MvChoosePhotoActivity"
    var VIDEO_EDIT_ACTIVITY = "com.ss.android.ugc.aweme.shortvideo.cut.VECutVideoActivity"
    var VIDEO_PUBLISH_ACTIVITY = "com.ss.android.ugc.aweme.shortvideo.edit.VEVideoPublishEditActivity"

    var defaultFollowersNumbers = 100
    //粉丝人数基准
    var followersNumbers = 100

    //粉丝自己关注人数
    var defaultFollowingNumbers = 100
    var followingNumbers = 100

    var defaultLikeNumber = 100
    var likeNumber = 100

    var defaultScreenActionNum = 1
    var screenActionNum = 1

    //关注人数上线
    var defaultFollowerLimit = 100
    var followerLimit = 100
    //服务启动开关
    var isServiceOn = false
    var isUpload = false

    //延时操作
    var defaultDelayAction = 5000L
    var delayAction = 5000L

    var defaultMinuteLimit = 30
    var minuteLimit =  30

    var homeState : AtomicBoolean = AtomicBoolean(false)
    fun getDelaySecond():Int {
        return (delayAction / 1000).toInt()
    }

    fun getDelayDefaultSecond():Int{
        return (defaultDelayAction / 1000).toInt()
    }

}