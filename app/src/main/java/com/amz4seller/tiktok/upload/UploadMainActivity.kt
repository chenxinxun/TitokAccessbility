package com.amz4seller.tiktok.upload

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.amz4seller.UploadService
import com.amz4seller.tiktok.BuildConfig
import com.amz4seller.tiktok.InspectorSettings
import com.amz4seller.tiktok.R
import kotlinx.android.synthetic.main.layout_upload_main.*
import kotlin.random.Random


class UploadMainActivity : AppCompatActivity() {
    private lateinit var viewModel: DeviceIdViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_upload_main)

        viewModel = ViewModelProvider.NewInstanceFactory().create(DeviceIdViewModel::class.java)
        device_id.setText("publish01")
        val isAppInstalled = appInstalledOrNot("com.tiktok.follow")
        val show =if(isAppInstalled){
            "已经安装，请卸载或者停止自动加粉的辅助服务开关"
        } else {
            "未安装"
        }
        tip.text = "[版本名："+ BuildConfig.VERSION_NAME + "，版本号:"+BuildConfig.VERSION_CODE + "，是否安装follow自动加粉:$show] 适用-Android系统10以上的tiktok"
        viewModel.context = this
        doGet()
        viewModel.result.observe(this, {

        })
        ip.setText(InspectorSettings.HOST_IP)
        delay.setText(InspectorSettings.getDelaySecond().toString())

        action_stop.setOnClickListener {
            stopService(Intent(this, UploadService::class.java))
            Toast.makeText(this, "停止成功", Toast.LENGTH_SHORT).show()
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
        }

    }

    private fun doGet(){
        val setId = device_id.text.trim().toString()
        if(TextUtils.isEmpty(setId)){
            Toast.makeText(this, "先输入设备标识", Toast.LENGTH_SHORT).show()
        }
        viewModel.getDeviceId(setId)
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
}