package com.amz4seller.tiktok.upload

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.amz4seller.tiktok.DownloadService
import com.amz4seller.tiktok.InspectorSettings
import com.amz4seller.tiktok.R
import kotlinx.android.synthetic.main.layout_upload_main.*
import kotlinx.android.synthetic.main.layout_upload_main.delay

class UploadMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_upload_main)
        ip.setText(InspectorSettings.HOST_IP)
        delay.setText(InspectorSettings.getDelaySecond().toString())


        action_save.setOnClickListener {
            Toast.makeText(this, "保存启动前确定应用后台服务已停，应用已经停止,否则将设置无效", Toast.LENGTH_SHORT).show()
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
            DownloadService.enqueueWork(this, Intent())
        }

    }
}