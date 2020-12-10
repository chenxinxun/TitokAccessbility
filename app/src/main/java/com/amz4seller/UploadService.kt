package com.amz4seller

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.text.TextUtils
import com.amz4seller.tiktok.InspectorSettings
import com.amz4seller.tiktok.InspectorUtils
import com.amz4seller.tiktok.base.ApiService
import com.amz4seller.tiktok.utils.BusEvent
import com.amz4seller.tiktok.utils.LogEx
import com.amz4seller.tiktok.utils.RxBus
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URL

class UploadService : Service() {
    var isBreakLoop = false
    override fun onCreate() {
        super.onCreate()
        LogEx.d(LogEx.TAG_WATCH, "create upload service")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogEx.d(LogEx.TAG_WATCH, "start upload service")
        onHandleWork()
        return super.onStartCommand(intent, flags, startId)
    }


    override fun stopService(name: Intent?): Boolean {
        isBreakLoop = true
        LogEx.d(LogEx.TAG_WATCH, "stop upload service")
        return super.stopService(name)
    }

    private fun onHandleWork() {
        /**
         *  warning
         *  1.Android 8.1 开始需要配置 网络安全请求域名加入 如果是采用的内网ip地址 每次换ip需要重新将这个ip地址添加到配置 识别为白名单 通过清除改配只
         *  2.内网局域网访问，如果当前本地机子有挂代理需要将代理不再代理这个网络
         */
        val retrofit = InspectorUtils.getRetrofit()
        val service = retrofit.create(ApiService::class.java)
        CoroutineScope(Dispatchers.Default).launch {
            while (!isBreakLoop){
                try{
                    //确定是有设备并且没有在发布过程
                    if(!TextUtils.isEmpty(InspectorSettings.deviceId) && !InspectorSettings.pushing.get()){
                        val result =  service.getPublishUrl(InspectorSettings.deviceId)
                        val response = result.execute()?: return@launch

                        val body = response.body()?: return@launch
                        //下载视频组，每次任务只下载一次
                        if(body.status == 1){
                            if (body.content == null){
                                //test manual 手动执行任务
                                //downLoad(3)
                            } else {
                                downLoad(body.content!!.id)
                            }
                        }
                        delay(1000L * 30)
                    }

                }catch (e:Exception){
                    e.printStackTrace()
                    delay(1000L * 30)
                    LogEx.d(LogEx.TAG_WATCH, "get task message request error")
                }

            }

        }


    }

    private fun downLoad(id: Int){
        val baseUrl = "http://${InspectorSettings.HOST_IP}:8080/"
        val url = baseUrl + "tiktok/download?videoId=${id}"
        LogEx.d(LogEx.TAG_WATCH, "begin to down $url")
        handleActionDownLoad(url, id)
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionDownLoad(url: String, videoId:Int) {
        try {
            val name = System.currentTimeMillis()
            val photoPath = Environment.DIRECTORY_DCIM + "/Camera"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                // put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, photoPath)//保存路径
                /**
                 * Warnning 参数加上导致文件虽然保存了，但是没法被其他应用识别。可以查看 IS_PENDING 是否代表是持续上传的文件
                 */
                // put(MediaStore.MediaColumns.IS_PENDING, true)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //返回出一个URI
                val insert = contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: return

                //这个打开了输出流  直接保存视频
                contentResolver.openOutputStream(insert).use { outputStream ->
                    val videoUrl = URL(url)
                    val connection = videoUrl.openConnection()
                    connection.connect()
                    val input: InputStream = BufferedInputStream(connection.getInputStream())
                    val buffer = ByteArray(4096)
                    var len: Int
                    while (input.read(buffer).also { len = it } != -1) {
                        outputStream?.write(buffer, 0, len)
                    }
                    outputStream?.flush()
                    outputStream?.close()
                    input.close()
                }
                contentValues.clear()
                LogEx.d(LogEx.TAG_WATCH, "down $url finish and send down load finish event")
                InspectorSettings.currentVideoId.set(videoId)
                RxBus.send(BusEvent.EventDownLoadFinish())
                // contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            }


        }catch (e: Exception){
            e.printStackTrace()
            LogEx.d(LogEx.TAG_WATCH, "down $url error")
            reportDownloadFail()
        } finally {
        }
    }

    private fun reportDownloadFail(){
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val retrofit = InspectorUtils.getRetrofit()
                val service = retrofit.create(ApiService::class.java)
                val result = service.setUploadStatus(InspectorSettings.currentVideoId.get(), 0)
                val body = result.body()
                if(body == null){
                    LogEx.d(LogEx.TAG_WATCH, "report upload success fail")
                } else {
                    LogEx.d(LogEx.TAG_WATCH, "report upload success $body")
                }

            }catch (e: java.lang.Exception){
                e.printStackTrace()
                LogEx.d(LogEx.TAG_WATCH, "report request fail")
            } finally {
                InspectorSettings.currentVideoId.set(-1)
                InspectorSettings.pushing.set(false)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onDestroy() {
        super.onDestroy()
        isBreakLoop = true
        LogEx.d(LogEx.TAG_WATCH, "create upload service")
    }
}