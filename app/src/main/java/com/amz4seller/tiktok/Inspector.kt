package com.amz4seller.tiktok

import android.view.accessibility.AccessibilityNodeInfo

interface Inspector {
    fun resolveLayout(node: AccessibilityNodeInfo)
}