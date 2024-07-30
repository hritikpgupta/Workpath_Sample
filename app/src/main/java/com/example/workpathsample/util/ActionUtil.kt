// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
package com.example.workpathsample.util

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.example.workpathsample.task.receiver.AccessoryReceiver.Companion.TAG
import com.hp.workpath.api.Result
import com.hp.workpath.api.access.SignInAction
import com.hp.workpath.api.config.ConfigService
import org.json.JSONObject

object ActionUtil {
    @JvmStatic
    fun getAction(context: Context): SignInAction.Action {
        try {
            if (ConfigService.isSupported(context)) {
                val result = Result()
                val config = ConfigService.getDefaultConfig(context, result)
                showLogResult("ConfigService.getDefaultConfig", result)
                if (result.code == Result.RESULT_OK) {
                    if (config != null) {
                        val action = config.getString("agent_action")
                        Log.i("ActionUtil", "agent_action: $action")
                        return when {
                            SignInAction.Action.CONTINUE.name == action -> {
                                SignInAction.Action.CONTINUE
                            }
                            SignInAction.Action.FAIL.name == action -> {
                                SignInAction.Action.FAIL
                            }
                            SignInAction.Action.HOME.name == action -> {
                                SignInAction.Action.HOME
                            }
                            SignInAction.Action.BACK.name == action -> {
                                SignInAction.Action.BACK
                            }
                            else -> {
                                SignInAction.Action.SUCCESS
                            }
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            showLogResult("ConfigService.getDefaultConfig Error " + t.message)
        }
        return SignInAction.Action.SUCCESS
    }

    @JvmStatic
    @Throws(Throwable::class)
    fun setAction(context: Context, action: String?) {
        if (ConfigService.isSupported(context)) {
            val jsonObject = JSONObject()
            jsonObject.put("agent_action", action)
            val result = ConfigService.setDefaultConfig(context, jsonObject)
            showLogResult("ConfigService.setDefaultConfig(): ", result)
        }
    }

    fun showLogResult(msg: String?) {
        showLogResult(msg, null)
    }
    fun showLogResult(msg: String?, result: Result?) {
        var message = msg
        if (result != null) {
            message = msg + "\n" + build(result)
            if (result.code == Result.RESULT_FAIL) {
                Log.e(TAG, message)
            } else {
                Log.d(TAG, message)
            }
        } else {
            message?.let { Log.d(TAG, it) }
        }
    }
    fun build(result: Result): String {
        val code = if (Result.RESULT_OK == result.code) "RESULT_OK" else "RESULT_FAIL"
        val builder = java.lang.StringBuilder()
        builder.append("[")
        builder.append("\n").append("Code:").append(code)
        if (Result.RESULT_OK != result.code && result.errorCode != null) {
            builder.append(",").append("\n").append("ErrorCode:").append(result.errorCode)
        }
        if (!TextUtils.isEmpty(result.cause)) {
            builder.append(",").append("\n").append("Cause:").append(result.cause)
        }
        builder.append("\n").append("]")
        return builder.toString()
    }

}