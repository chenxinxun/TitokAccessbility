package com.origin

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.text.TextUtils
import com.origin.sendfix.InspectorSettings
import com.origin.sendfix.InspectorUtils
import com.origin.sendfix.base.ApiService
import com.origin.sendfix.utils.BusEvent
import com.origin.sendfix.utils.LogEx
import com.origin.sendfix.utils.RxBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean


class UploadService : Service() {
    private var isBreakLoop:AtomicBoolean = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        LogEx.d(LogEx.TAG_WATCH, "create upload service")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogEx.d(LogEx.TAG_WATCH, "start upload service")
        onHandleWork()
        return START_STICKY
    }


    override fun stopService(name: Intent?): Boolean {
        isBreakLoop.set(true)
        LogEx.d(LogEx.TAG_WATCH, "stop upload service")
        return super.stopService(name)
    }



    private fun onHandleWork() {
        /**
         *  warning
         *  1.Android 8.1 开始需要配置 网络安全请求域名加入 如果是采用的内网ip地址 每次换ip需要重新将这个ip地址添加到配置 识别为白名单 通过清除改配只
         *  2.内网局域网访问，如果当前本地机子有挂代理需要将代理不再代理这个网络
         *
         *  将协程转为 Thread
         */
        Thread {
            val retrofit = InspectorUtils.getRetrofit()
            val service = retrofit.create(ApiService::class.java)
            while (!isBreakLoop.get()){
                try{
                    //确定是有设备并且没有在发布过程
                    if(!TextUtils.isEmpty(InspectorSettings.deviceId) && !InspectorSettings.pushing.get()){
                        val result =  service.getPublishUrl(InspectorSettings.deviceId)
                        val response = result.execute()?:continue
                        val body = response.body()?:continue
                        //下载视频组，每次任务只下载一次
                        if(body.status == 1){
                            if (body.content != null) {
                                val videoId = body.content!!.id
                                val isVideoOk = downLoad(videoId)
                                if(isVideoOk){
                                    Thread.sleep(1000L * 10)
                                    val isVideoFormat = videoValidate(videoId)
                                    if(isVideoFormat){
                                        //延迟发送上传任务
                                        InspectorSettings.currentVideoId.set(videoId)
                                        RxBus.send(BusEvent.EventDownLoadFinish())
                                    }

                                }
                            }
                        }
                        Thread.sleep(1000L * 30)
                    }

                }catch (e: Exception){
                    e.printStackTrace()
                    Thread.sleep(1000L * 30)
                    LogEx.d(LogEx.TAG_WATCH, "get task message request error")
                }

            }
        }.start()

    }

    private fun downLoad(id: Int) :Boolean{
        val baseUrl = "http://${InspectorSettings.HOST_IP}:8080/"
        val url = baseUrl + "tiktok/download?videoId=${id}"
        LogEx.d(LogEx.TAG_WATCH, "begin to down $url")
        return handleActionDownLoad(url, id)
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    @Suppress("DEPRECATION")
    private fun handleActionDownLoad(url: String, id:Int):Boolean{
        try {
            val videoExist = videoExist(id)
            if(videoExist){
                return false
            }
            val photoPath = Environment.DIRECTORY_DCIM + "/Camera"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, id)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, photoPath)
                }//保存路径
                /**
                 * Warnning 参数加上导致文件虽然保存了，但是没法被其他应用识别。可以查看 IS_PENDING 是否代表是持续上传的文件
                 */
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //返回出一个URI
                val insert = contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: return false

                //这个打开了输出流  直接保存视频 视频格式要求去验证下
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
                    contentValues.clear()
                    return true
                }

                // contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            } else {
                return false
            }


        }catch (e: Exception){
            e.printStackTrace()
            LogEx.d(LogEx.TAG_WATCH, "down $url error")
            reportDownloadFail()
            return false
        } finally {

        }

    }


    private fun videoExist(id:Int):Boolean{
        var videoExist = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val projection =  arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.SIZE
                )

                val selection = "${MediaStore.Video.Media.DISPLAY_NAME} like ?"
                val selectionArgs = arrayOf(
                    "$id.%"
                )
                val collection =   MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
                val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} DESC"
                val query = contentResolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )

                query?.use { cursor ->
                    val nameColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                    while (cursor.moveToNext()) {
                        // Get values of columns for a given video.
                        val name = cursor.getString(nameColumn)
                        val nameArgs = name.split(".")
                        if(nameArgs.contains(id.toString())){
                            videoExist = true
                            break
                        }
                    }
                    cursor.close()
                }
                query?.close()
            }catch (e:Exception){

            } finally {

            }
        }

        if(videoExist){
            LogEx.d(LogEx.TAG_WATCH, "Video $id already exist")
        }
        return videoExist
    }


    private fun videoValidate(id:Int):Boolean{
        var videoValid = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val projection =  arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.MIME_TYPE,
                    MediaStore.Video.Media.SIZE
                )

                val selection = "${MediaStore.Video.Media.DISPLAY_NAME} like ?"
                val selectionArgs = arrayOf(
                    "$id.%"
                )
                val collection =   MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
                val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} DESC"
                val query = contentResolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )

                query?.use { cursor ->
                    val typeColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                    val sizeColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                    while (cursor.moveToNext()) {
                        // Get values of columns for a given video.
                        val type = cursor.getString(typeColumn)
                        val size = cursor.getInt(sizeColumn)
                        val nameArgs = type.split("/")
                        //视频大小大于1kB 并且为 video格式
                        if(size > 1000 && nameArgs.contains("video")){
                            videoValid = true
                            break
                        }
                    }
                    cursor.close()
                }
                query?.close()
            }catch (e:Exception){

            } finally {

            }
        }

        if(!videoValid){
            LogEx.d(LogEx.TAG_WATCH, "Video $id size invalidate")
        }
        return videoValid
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
        isBreakLoop.set(true)
        LogEx.d(LogEx.TAG_WATCH, "destroy upload service")
        super.onDestroy()
    }
}