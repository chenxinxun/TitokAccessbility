package com.amz4seller.tiktok

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amz4seller.tiktok.utils.LogEx
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LogEx.watchVideoVisible = true
        checkReadWritePermission()

        service_switch.setOnCheckedChangeListener { _, isChecked ->
            InspectorSettings.isServiceOn = isChecked
            if(isChecked){
                DownloadService.enqueueWork(this, Intent())
            }
        }

        service_upload.setOnCheckedChangeListener { _, isChecked ->
            InspectorSettings.isUpload = isChecked
        }

        service_switch.isChecked = InspectorSettings.isServiceOn
        delay.setText(InspectorSettings.getDelaySecond().toString())
        follower_num.setText(InspectorSettings.followersNumbers.toString())

        following_num.setText(InspectorSettings.followersNumbers.toString())
        like_num.setText(InspectorSettings.likeNumber.toString())
        action_number.setText(InspectorSettings.screenActionNum.toString())
        action_minute.setText(InspectorSettings.minuteLimit.toString())
        action_save.setOnClickListener {
            val followNum = follower_num.text?.trim().toString()
            val followingNum = following_num.text?.trim().toString()
            val actionNumber = action_number.text?.trim().toString()
            val actionMinute = action_minute.text?.trim().toString()
            val likeNumber = like_num.text?.trim().toString()
            val delayTime = delay.text?.trim().toString()
            if(!TextUtils.isEmpty(actionNumber)){
                if (actionNumber.toInt() > 8) {
                    Toast.makeText(this, "超过8个可能会出现超屏操作", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            if(TextUtils.isEmpty(followNum)){
                InspectorSettings.followersNumbers = InspectorSettings.defaultFollowersNumbers
                follower_num.setText(InspectorSettings.defaultFollowersNumbers.toString())
            } else {
                InspectorSettings.followersNumbers = followNum.toIntOrNull()?:InspectorSettings.defaultFollowersNumbers
            }

            if(TextUtils.isEmpty(followingNum)){
                InspectorSettings.followingNumbers = InspectorSettings.defaultFollowingNumbers
                following_num.setText(InspectorSettings.defaultFollowingNumbers.toString())
            } else {
                InspectorSettings.followingNumbers = followingNum.toIntOrNull()?:InspectorSettings.defaultFollowingNumbers
            }

            if(TextUtils.isEmpty(likeNumber)) {
                InspectorSettings.likeNumber = InspectorSettings.defaultLikeNumber
                like_num.setText(InspectorSettings.defaultLikeNumber.toString())
            } else {
                InspectorSettings.likeNumber = likeNumber.toIntOrNull()?:InspectorSettings.defaultLikeNumber
            }

            if(TextUtils.isEmpty(actionNumber)){
                action_number.setText(InspectorSettings.defaultScreenActionNum.toString())
                InspectorSettings.screenActionNum = InspectorSettings.defaultScreenActionNum
            } else {
                InspectorSettings.screenActionNum = actionNumber.toIntOrNull()?:InspectorSettings.defaultScreenActionNum
            }

            if(TextUtils.isEmpty(actionMinute)){
                action_minute.setText(InspectorSettings.defaultMinuteLimit.toString())
                InspectorSettings.minuteLimit = InspectorSettings.defaultMinuteLimit
            } else {
                InspectorSettings.minuteLimit = actionMinute.toIntOrNull()?:InspectorSettings.defaultMinuteLimit
            }


            if(TextUtils.isEmpty(delayTime)){
                InspectorSettings.delayAction = InspectorSettings.defaultDelayAction
                delay.setText(InspectorSettings.getDelayDefaultSecond().toString())
            } else {
                val time = delayTime.toInt() * 1000L
                InspectorSettings.delayAction = time
            }
            Toast.makeText(this, "不配置将使用默认值", Toast.LENGTH_SHORT).show()
        }


    }


    private fun checkReadWritePermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1002
                )

            }
        }
    }
}