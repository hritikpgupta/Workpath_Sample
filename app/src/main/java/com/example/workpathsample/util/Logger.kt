package com.example.workpathsample.util

import com.hp.workpath.api.accessory.AccessoryInfo
import com.hp.workpath.api.accessory.hid.HIDAccessoryInfo

object Logger {
    private const val _START = "["
    private const val _END = "]"
    private const val _C = ","
    private const val _EQ = "="

    fun build(hidAccessoryInfo: HIDAccessoryInfo?): String? {
        if (hidAccessoryInfo != null) {
            val logBuilder = StringBuilder()
            logBuilder.append(_START).append(hidAccessoryInfo.registrationType).append(_END)
            logBuilder.append("PID").append(_EQ).append(hidAccessoryInfo.productId).append(_C)
            logBuilder.append("VID").append(_EQ).append(hidAccessoryInfo.vendorId).append(_C)
            logBuilder.append("S/N").append(_EQ).append(hidAccessoryInfo.serialNumber)
            return logBuilder.toString()
        }
        return null
    }
    fun build(accessoryInfo: AccessoryInfo?): String? {
        if (accessoryInfo != null) {
            if (accessoryInfo is HIDAccessoryInfo) {
                val logBuilder = StringBuilder()
                logBuilder.append(_START)
                logBuilder.append("registrationType").append(_EQ).append(accessoryInfo.registrationType).append(_C)
                logBuilder.append("PID").append(_EQ).append(accessoryInfo.productId).append(_C)
                logBuilder.append("VID").append(_EQ).append(accessoryInfo.vendorId).append(_C)
                logBuilder.append("S/N").append(_EQ).append(accessoryInfo.serialNumber)
                logBuilder.append(_END)
                return logBuilder.toString()
            }
        }
        return null
    }
}