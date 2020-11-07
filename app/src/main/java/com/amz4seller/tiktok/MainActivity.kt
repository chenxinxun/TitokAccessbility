package com.amz4seller.tiktok

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        service_switch.setOnCheckedChangeListener { switch, isChecked ->
            InspectorSettings.isServiceOn = isChecked
        }

        service_switch.isChecked = InspectorSettings.isServiceOn
    }
}