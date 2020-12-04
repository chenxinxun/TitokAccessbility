package com.amz4seller.tiktok.upload

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.amz4seller.tiktok.DownloadService
import com.amz4seller.tiktok.InspectorSettings
import com.amz4seller.tiktok.R
import kotlinx.android.synthetic.main.layout_upload_main.action_save
import kotlinx.android.synthetic.main.layout_upload_main.delay
import kotlinx.android.synthetic.main.layout_upload_main.service_switch

class UploadMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_upload_main)
        service_switch.isChecked = InspectorSettings.isServiceOn
        delay.setText(InspectorSettings.getDelaySecond().toString())
        service_switch.setOnCheckedChangeListener { _, isChecked ->
            InspectorSettings.isServiceOn = isChecked
        }

        action_save.setOnClickListener {
            val delayTime = delay.text?.trim().toString()
            if(TextUtils.isEmpty(delayTime)){
                InspectorSettings.delayAction = InspectorSettings.defaultDelayAction
                delay.setText(InspectorSettings.getDelayDefaultSecond().toString())
            } else {
                val time = delayTime.toInt() * 1000L
                InspectorSettings.delayAction = time
            }
        }
        DownloadService.enqueueWork(this, Intent())
    }
}