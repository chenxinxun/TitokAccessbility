package com.origin.sendfix.upload

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.lifecycle.ViewModelProvider
import com.origin.UploadService
import com.origin.sendfix.BuildConfig
import com.origin.sendfix.InspectorSettings
import com.origin.sendfix.R
import kotlinx.android.synthetic.main.layout_upload_main.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException


class UploadMainActivity : AppCompatActivity() {
    private lateinit var viewModel: DeviceIdViewModel
    companion object {
        private val DEVICE = preferencesKey<String>("device_id")
    }
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_upload_main)
        val deviceId =  PreferenceManager.getDefaultSharedPreferences(this).getString("device_id", "publish01")?:"publish01"
        viewModel = ViewModelProvider.NewInstanceFactory().create(DeviceIdViewModel::class.java)
        device_id.setText(deviceId)

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

    fun <T> DataStore<Preferences>.getValueFlow(
            key: Preferences.Key<T>,
            defaultValue: T
    ): Flow<T> {
        return this.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }.map { preferences ->
                    preferences[key] ?: defaultValue
                }
    }

    suspend fun <T> DataStore<Preferences>.setValue(key: Preferences.Key<T>, value: T) {
        this.edit { preferences ->
            preferences[key] = value
        }
    }
}