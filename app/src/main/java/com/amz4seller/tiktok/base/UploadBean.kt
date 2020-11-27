package com.amz4seller.tiktok.base

class UploadBean  {
    var content= ArrayList<Video>()
    var status = 0
    inner class Video(){
        var id:Int = 0
    }
}