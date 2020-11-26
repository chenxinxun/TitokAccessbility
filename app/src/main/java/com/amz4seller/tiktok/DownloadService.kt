package com.amz4seller.tiktok

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.JobIntentService
import java.io.*
import java.lang.Exception
import java.net.URL


class DownloadService : JobIntentService() {
    companion object {
        private val jobId = 10001

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, DownloadService::class.java, jobId, work)
        }
    }


    override fun onHandleWork(intent: Intent) {
        val url = ""
        handleActionDownLoad("https://txmov2.a.yximgs.com/upic/2020/11/03/19/BMjAyMDExMDMxOTE4MDhfMTgzNDA0NzM2N18zODY4ODA5Njk3NF8xXzM=_b_B3efe88acd04c1794dc9e296231259cd1.mp4?tag=1-1606388627-xpcwebfeatured-0-se9p87ek3f-0f39fd7dce8cdcb8&clientCacheKey=3xjnx6vw9sq33ii_b.mp4&tt=b&di=da68eb6c&bp=10004")
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
                    val url = URL(url)
                    val connection = url.openConnection()
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
               // contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            } else {

            }

        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}