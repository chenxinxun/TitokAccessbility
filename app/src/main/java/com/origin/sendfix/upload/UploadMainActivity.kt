@file:Suppress("DEPRECATION")

package com.origin.sendfix.upload

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dianping.logan.Logan
import com.dianping.logan.LoganConfig
import com.origin.UploadService
import com.origin.sendfix.BuildConfig
import com.origin.sendfix.InspectorSettings
import com.origin.sendfix.OriginSendFixService
import com.origin.sendfix.R
import com.origin.sendfix.utils.LogEx
import kotlinx.android.synthetic.main.layout_upload_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.net.*
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
class UploadMainActivity : AppCompatActivity() {
    private lateinit var viewModel: DeviceIdViewModel
    private lateinit var mSendLogRunnable: RealSendLogRunnable
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_upload_main)
        val deviceId =  PreferenceManager.getDefaultSharedPreferences(this).getString(
            "device_id",
            "publish01"
        )?:"publish01"
        viewModel = ViewModelProvider.NewInstanceFactory().create(DeviceIdViewModel::class.java)
        device_id.setText(deviceId)
        initLog()
        access_status.text = isAccessibilityServiceEnabled()
        val isAppInstalled = appInstalledOrNot("com.tiktok.follow")
        val isInstall = appInstalledOrNot("com.amz4seller.titokaccessbility")
        val show =if(isAppInstalled){
            if(isInstall){
                "已经安装，请卸载或者停止自动加粉和旧版发布的辅助服务开关"
            } else {
                "已经安装，请卸载或者停止自动加粉的辅助服务开关"
            }
        } else {
            if(isInstall){
                "已经安装，请卸载或者旧版发布的辅助服务开关"
            } else {
                "未安装"
            }

        }
        tip.text = "[版本名："+ BuildConfig.VERSION_NAME + "，版本号:"+BuildConfig.VERSION_CODE + "，是否安装follow自动加粉:$show] 适用-Android系统10以上的tiktok"
        viewModel.context = this
        doGet()
        ip_address.text = getLocalIpAddress()?:"-"
        viewModel.result.observe(this, {

        })
        ip.setText(InspectorSettings.HOST_IP)
        delay.setText(InspectorSettings.getDelaySecond().toString())

        action_open.setOnClickListener {
            autoAccessbilityOpen()
        }

        action_stop.setOnClickListener {
            stopService(Intent(this, UploadService::class.java))
            Toast.makeText(this, "停止成功", Toast.LENGTH_SHORT).show()
            LogEx.d(LogEx.TAG_WATCH, "manual stop service")
           /* lifecycleScope.launch {
                withContext(Dispatchers.Default){
                    videoValidate(4)
                }

            }*/
        }
        action_save.setOnClickListener {
            Toast.makeText(this, "将以新的配置运行", Toast.LENGTH_SHORT).show()
            val delayTime = delay.text?.trim().toString()
            if(TextUtils.isEmpty(delayTime)){
                InspectorSettings.delayAction = InspectorSettings.defaultDelayAction
                delay.setText(InspectorSettings.getDelayDefaultSecond().toString())
            } else {
                val time = delayTime.toInt() * 1000L
                InspectorSettings.delayAction = time
            }
            val ipValue = ip.text.trim().toString()
            if(!TextUtils.isEmpty(ipValue)){
                InspectorSettings.HOST_IP = ipValue
            }
            doGet()
            stopService(Intent(this, UploadService::class.java))
            startService(Intent(this, UploadService::class.java))
            Toast.makeText(this, "启动成功", Toast.LENGTH_SHORT).show()
            LogEx.d(LogEx.TAG_WATCH, "manual start service")
        }
        mSendLogRunnable = RealSendLogRunnable()

        upload_log.setOnClickListener {
            loganSend()
        }

    }

    override fun onResume() {
        super.onResume()
        access_status.text = isAccessibilityServiceEnabled()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        access_status.text = isAccessibilityServiceEnabled()
    }

    private fun autoAccessbilityOpen(){
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivityForResult(intent, 0)
    }

    private fun loganSend() {
        val dataFormat = SimpleDateFormat("yyyy-MM-dd")
        val d: String = dataFormat.format(Date(System.currentTimeMillis()))
        val temp = arrayOfNulls<String>(1)
        temp[0] = d
        Logan.s(temp, mSendLogRunnable)
    }

    private fun isAccessibilityServiceEnabled(): String {
        val expectedComponentName = ComponentName(this, OriginSendFixService::class.java)
        val enabledServicesSetting: String =
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
                ?: return "关闭"
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledService = ComponentName.unflattenFromString(componentNameString)
            if (enabledService != null && enabledService == expectedComponentName) return "开启"
        }
        return "关闭"
    }

    private fun doGet(){
        val setId = device_id.text.trim().toString()
        if(TextUtils.isEmpty(setId)){
            Toast.makeText(this, "先输入设备标识", Toast.LENGTH_SHORT).show()
        }
        viewModel.getDeviceId(setId)

        val memes: SharedPreferences.Editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        memes.putString("device_id", setId).apply()
        memes.commit()
    }

    private fun appInstalledOrNot(uri: String): Boolean {
        val pm = packageManager
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return false
    }

    private fun getLocalIpAddress(): String? {
        try {
            val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val inf: NetworkInterface = en.nextElement()
                val enumIpAdd: Enumeration<InetAddress> = inf.inetAddresses
                while (enumIpAdd.hasMoreElements()) {
                    val ineptAddress: InetAddress = enumIpAdd.nextElement()
                    if (!ineptAddress.isLoopbackAddress && ineptAddress is Inet4Address) {
                        return ineptAddress.getHostAddress()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return "-"
    }

    @Suppress("DEPRECATION")
    private fun handleActionDownLoad(url: String):Boolean{
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
                   "10.%"
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
                   // Cache column indices.

                   val nameColumn =
                       cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                   val sizeColumn =
                       cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

                   while (cursor.moveToNext()) {
                       // Get values of columns for a given video.
                       val name = cursor.getString(nameColumn)
                       val size = cursor.getString(sizeColumn)
                       val nameArgs = name.split(".")
                       if(nameArgs.contains("10")){
                           videoExist = true
                           break
                       }

                   }

                   cursor.close()
               }
               query?.close()
           }catch (e: Exception){

           } finally {

           }
        }

        if(videoExist){
            videoValidate(10)
            LogEx.d(LogEx.TAG_WATCH, "Video already exist")
            return false
        }


        try {
            val photoPath = Environment.DIRECTORY_DCIM + "/Camera"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, 10.toString())
                put(MediaStore.MediaColumns.RELATIVE_PATH, photoPath)//保存路径
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
                    LogEx.d(LogEx.TAG_WATCH, "down $url finish")
                    return true
                }

                // contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            } else {
                return false
            }


        }catch (e: Exception){
            e.printStackTrace()
            LogEx.d(LogEx.TAG_WATCH, "down $url error")
            return false
        } finally {
        }

        videoValidate(10)

    }

    private fun videoValidate(id: Int):Boolean{
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
                        if(size > 1000 && nameArgs.contains("video")){
                            videoValid = true
                            break
                        }
                    }
                    cursor.close()
                }
                query?.close()
            }catch (e: Exception){

            } finally {

            }
        }

        if(!videoValid){
            LogEx.d(LogEx.TAG_WATCH, "Video size invalidate")
        }
        return videoValid
    }


    private fun initLog(){
        val config = LoganConfig.Builder()
            .setCachePath(applicationContext.filesDir.absolutePath)
            .setPath(
                (applicationContext.getExternalFilesDir(null)!!.absolutePath
                        + File.separator) + "logan_v1"
            )
            .setEncryptKey16("0123456789012345".toByteArray())
            .setEncryptIV16("0123456789012345".toByteArray())
            .build()
        Logan.init(config)
    }
}