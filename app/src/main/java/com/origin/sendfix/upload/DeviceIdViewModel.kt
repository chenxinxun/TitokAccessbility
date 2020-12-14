package com.origin.sendfix.upload

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.origin.sendfix.InspectorSettings
import com.origin.sendfix.InspectorUtils
import com.origin.sendfix.base.ApiService
import com.origin.sendfix.utils.LogEx
import kotlinx.coroutines.launch
import java.net.NetworkInterface
import java.util.*


class DeviceIdViewModel: ViewModel() {
    lateinit var context: Context
    var result: MutableLiveData<String> = MutableLiveData()
    var id:String = ""
    fun getDeviceId(deviceId: String) {
        viewModelScope.launch {
            val retrofit = InspectorUtils.getRetrofit()
            val service = retrofit.create(ApiService::class.java)
            try{
                val macAddress = getMacAddress()?:"wifi is disabled"
                val body =  service.getIdentifyAsync(deviceId, macAddress)
                val bean = body.body()?:return@launch
                if(bean.status == 1){
                    id = bean.content
                    InspectorSettings.deviceId = id
                    LogEx.d(LogEx.TAG_WATCH, "get device id ${InspectorSettings.deviceId}")
                    result.value =id
                } else {
                    result.value =""
                }
            } catch (e: Exception){
                e.printStackTrace()
                LogEx.d(LogEx.TAG_WATCH, "begin get device id request error")
            }
        }
    }

    private fun getMacAddress(): String? {
        try {
            val all: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (nif.name != "wlan0") {
                    continue
                }
                val macBytes: ByteArray = nif.hardwareAddress ?: return ""
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }
                if (res1.isNotEmpty()) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }
}