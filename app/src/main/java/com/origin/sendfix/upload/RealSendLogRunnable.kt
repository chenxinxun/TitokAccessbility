package com.origin.sendfix.upload

import android.text.TextUtils
import android.util.Log
import com.dianping.logan.SendLogRunnable
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.URL
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection



class RealSendLogRunnable: SendLogRunnable() {
    private var mUploadLogUrl = "http://localhost:3000/logupload"

    override fun sendLog(logFile: File) {
        val success = doSendFileByAction(logFile)
        Log.d("上传日志测试", "日志上传测试结果：$success")
        // Must Call finish after send log
        finish()
        if (logFile.name.contains(".copy")) {
            logFile.delete()
        }
    }

    fun setIp(ip: String) {
        mUploadLogUrl = "http://$ip:3000/logupload"
    }

    private fun getActionHeader(): HashMap<String, String> {
        val map: HashMap<String, String> = HashMap()
        map["Content-Type"] = "binary/octet-stream" //二进制上传
        map["client"] = "android"
        return map
    }

    /**
     * 主动上报
     */
    private fun doSendFileByAction(logFile: File): Boolean {
        var isSuccess = false
        try {
            val fileStream = FileInputStream(logFile)
            val backData = doPostRequest(mUploadLogUrl, fileStream, getActionHeader())
            isSuccess = handleSendLogBackData(backData)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return isSuccess
    }

    private fun doPostRequest(
        url: String,
        inputData: InputStream,
        headerMap: Map<String, String>
    ): ByteArray? {
        var data: ByteArray? = null
        var outputStream: OutputStream? = null
        var inputStream: InputStream? = null
        var c: HttpURLConnection? = null
        val back: ByteArrayOutputStream
        val buffer = ByteArray(2048)
        try {
            val u = URL(url)
            c = u.openConnection() as HttpURLConnection
            if (c is HttpsURLConnection) {
                (c as HttpsURLConnection?)?.hostnameVerifier =
                    HostnameVerifier { _, _ -> true }
            }
            val entrySet = headerMap.entries
            for ((key, value) in entrySet) {
                c.addRequestProperty(key, value)
            }
            c.readTimeout = 15000
            c.connectTimeout = 15000
            c.doInput = true
            c.doOutput = true
            c.requestMethod = "POST"
            outputStream = c.outputStream
            var i: Int
            while (inputData.read(buffer).also { i = it } != -1) {
                outputStream.write(buffer, 0, i)
            }
            outputStream.flush()
            val res: Int = c.responseCode
            if (res == 200) {
                back = ByteArrayOutputStream()
                inputStream = c.inputStream?:return null
                while (inputStream.read(buffer).also { i = it } != -1) {
                    back.write(buffer, 0, i)
                }
                data = back.toByteArray()
            }
        } catch (e: ProtocolException) {
            e.printStackTrace()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            try {
                inputData.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            c?.disconnect()
        }
        return data
    }

    /**
     * 处理上传日志接口返回的数据
     */
    @Throws(JSONException::class)
    private fun handleSendLogBackData(backData: ByteArray?): Boolean {
        var isSuccess = false
        if (backData != null) {
            val data = String(backData)
            if (!TextUtils.isEmpty(data)) {
                val jsonObj = JSONObject(data)
                if (jsonObj.optBoolean("success", false)) {
                    isSuccess = true
                }
            }
        }
        return isSuccess
    }
}