package com.amz4seller.tiktok

object InspectorSettings {
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
    //延时操作
    var defaultDelayAction = 2000L
    var delayAction = 2000L

    var defaultMinuteLimit = 30
    var minuteLimit =  30

    fun getDelaySecond():Int {
        return (delayAction / 1000).toInt()
    }

    fun getDelayDefaultSecond():Int{
        return (defaultDelayAction / 1000).toInt()
    }

}