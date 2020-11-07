package com.amz4seller.tiktok

import android.text.TextUtils

class Blogger {
    /*数据可以为空是为了等待数据全部填充完成后才执行 返回操作*/
    var name:String ?=null
    var note:String ?=null
    var followerNumber:Int? = 0

    fun isDataWaitOk():Boolean{
        return TextUtils.isEmpty(name) || TextUtils.isEmpty(note) ||  followerNumber == null
    }

    override fun toString(): String {
        return "[name:$name, note:$note, followerNumber:$followerNumber]"
    }


}