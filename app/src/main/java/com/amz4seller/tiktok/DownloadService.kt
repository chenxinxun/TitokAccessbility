package com.amz4seller.tiktok

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import androidx.core.app.JobIntentService
import com.amz4seller.tiktok.base.ApiService
import com.amz4seller.tiktok.utils.BusEvent
import com.amz4seller.tiktok.utils.LogEx
import com.amz4seller.tiktok.utils.LogEx.TAG_WATCH
import com.amz4seller.tiktok.utils.RxBus
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URL
import java.util.concurrent.TimeUnit


class DownloadService : JobIntentService() {
    var baseUrl = "http://10.12.1.58:8080/"
    var id = ""


    private fun downLoad(id: Int){
        val url = baseUrl + "tiktok/download?videoId=${id}"
        LogEx.d(TAG_WATCH, "begin to down $url")
        handleActionDownLoad(url)
    }

    companion object {
        private const val jobId = 10001

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, DownloadService::class.java, jobId, work)
        }

    }


    override fun onHandleWork(intent: Intent) {
        /**
         *  warning
         *  1.Android 8.1 开始需要配置 网络安全请求域名加入 如果是采用的内网ip地址 每次换ip需要重新将这个ip地址添加到配置 识别为白名单 通过清除改配只
         *  2.内网局域网访问，如果当前本地机子有挂代理需要将代理不再代理这个网络
         */
        val retrofit = InspectorUtils.getRetrofit()
        val service = retrofit.create(ApiService::class.java)
        try{
            if(TextUtils.isEmpty(id)){
                val result =  service.getIdentify()
                val response = result.execute()
                val body = response.body()?:return
                if(body.status == 1){
                    id = body.content
                }

            }
        } catch (e:Exception){
            e.printStackTrace()
            LogEx.d(TAG_WATCH, "begin get device id request error")
        }



        while (true){
            try{
                if(TextUtils.isEmpty(id)){
                    continue
                }
                val result =  service.getPublishUrl(id)
                val response = result.execute()
                val bean = response.body()?:return
                //下载视频组，每次任务只下载一次
                if(bean.status == 1){
                    if (bean.content == null){
                        //test manual 手动执行任务
                        //downLoad(3)
                    } else {
                        downLoad(bean.content!!.id)
                    }
                }
                Thread.sleep(1000L * 30)
            }catch (e:Exception){
                e.printStackTrace()
                LogEx.d(TAG_WATCH, "get task message request error")
            }

        }

    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionDownLoad(url: String) {
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
                LogEx.d(TAG_WATCH, "down $url finish and send down load finish event")
                RxBus.send(BusEvent.EventDownLoadFinish())
               // contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            } else {

            }


        }catch (e: Exception){
            e.printStackTrace()
            LogEx.d(TAG_WATCH, "down $url error")
        } finally {
        }
    }
}