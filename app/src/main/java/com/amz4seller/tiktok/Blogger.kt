package com.amz4seller.tiktok

import android.text.TextUtils

class Blogger {
    /*数据可以为空是为了等待数据全部填充完成后才执行 返回操作*/
    var name:String ?=null
    var note:String ?=null
    var followerNumber:Int? = null
    var followingNumber:Int? = null
    var likeNumber :Int? = null

    fun isFollowIsMatch():Boolean{
        if(followingNumber == null || followerNumber == null || likeNumber == null) {
            return false
        }
        return followerNumber!! >= InspectorSettings.followersNumbers && followingNumber!! >= InspectorSettings.followingNumbers && likeNumber!! >= InspectorSettings.likeNumber
    }

    fun isDataNeedWaitOk():Boolean{
        return TextUtils.isEmpty(name) || TextUtils.isEmpty(note) ||  followerNumber == null || name == "@" || followingNumber == null || likeNumber == null
    }

    override fun toString(): String {
        return "[name:$name, note:$note, followerNumber:$followerNumber]"
    }

    fun isSameBlogger(arg:String):Boolean{
        if(name == null || TextUtils.isEmpty(arg)){
            return false
        }
        return arg == name
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || name == null || note == null || followerNumber == null || followingNumber == null || likeNumber == null) {
            return false
        }
        other as Blogger
        if (other.name == null || other.note == null || other.followerNumber == null || followingNumber == null || likeNumber == null) {
            return false
        }
        return other.name == name && other.note == note && followerNumber == other.followerNumber && likeNumber == other.likeNumber && followingNumber == other.followingNumber
    }

}