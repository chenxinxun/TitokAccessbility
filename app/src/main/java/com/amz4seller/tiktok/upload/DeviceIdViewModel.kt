package com.amz4seller.tiktok.upload

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amz4seller.tiktok.InspectorSettings
import com.amz4seller.tiktok.InspectorUtils
import com.amz4seller.tiktok.base.ApiService
import com.amz4seller.tiktok.utils.LogEx
import kotlinx.coroutines.launch

class DeviceIdViewModel: ViewModel() {
    var deviceId: MutableLiveData<String> = MutableLiveData()
    var id:String = ""
    fun getDeviceId() {
        viewModelScope.launch {
            val retrofit = InspectorUtils.getRetrofit()
            val service = retrofit.create(ApiService::class.java)
            try{
                if(TextUtils.isEmpty(id)){
                    val body =  service.getIdentifyAsync()
                    val bean = body.body()?:return@launch
                    if(bean.status == 1){
                        id = bean.content
                        InspectorSettings.deviceId = id
                        LogEx.d(LogEx.TAG_WATCH, "get device id ${InspectorSettings.deviceId}")
                        deviceId.value =id
                    } else {
                        deviceId.value =""
                    }
                }
            } catch (e:Exception){
                e.printStackTrace()
                LogEx.d(LogEx.TAG_WATCH, "begin get device id request error")
            }
        }
    }
}