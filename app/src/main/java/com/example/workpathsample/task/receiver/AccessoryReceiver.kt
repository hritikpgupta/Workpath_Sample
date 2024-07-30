package com.example.workpathsample.task.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.hp.workpath.api.Result
import com.hp.workpath.api.SsdkUnsupportedException
import com.hp.workpath.api.Workpath
import com.hp.workpath.api.access.AccessService
import com.hp.workpath.api.access.Principal
import com.hp.workpath.api.accessory.hid.AccessoryService
import java.lang.StringBuilder
import java.lang.ref.WeakReference

class AccessoryReceiver : BroadcastReceiver() {
    private var mContextRef: WeakReference<Context?>? = null
    private var appContext: Context? = null
    private var mAccessoryContextId: String? = null
    private var mOrdinal: Long = 0
    companion object {
        const val TAG = "Accessory Receiver"
        private const val STATUS_ACTION = "com.hp.workpath.action.ACCESSORY_STATUS_ACTION"
        private const val REPORT_ACTION = "com.hp.workpath.action.ACCESSORY_REPORT_ACTION"
        private var isInitializedSDK = false
        private var isFirstResend = true
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        appContext = context?.applicationContext
        mContextRef = WeakReference(appContext)
        if(isFirstResend){
            isFirstResend = false
            initializeWorkpath()
        }
        if(STATUS_ACTION == intent?.action || REPORT_ACTION == intent?.action){
            val action = intent.action
            val vendorId = intent.getIntExtra("ACCESSORY_INFO_VENDER_ID", -1)
            val productId = intent.getIntExtra("ACCESSORY_INFO_PRODUCT_ID", -1)

            if (vendorId != -1 && productId != -1) {
                when (action) {
                    STATUS_ACTION -> onContextChange(intent)
                    REPORT_ACTION -> onReceive(intent)
                }
            }
        }


    }
    private fun initializeWorkpath() {
        try {
            mContextRef?.get()?.let { Workpath.getInstance().initialize(it) }
            isInitializedSDK = true
        } catch (e: SsdkUnsupportedException) {
            e.printStackTrace()
        }
    }

    private fun onContextChange(intent: Intent) {
        val timeStamp = intent.getStringExtra("ACCESSORY_INFO_TIMESTAMP")
        val eventCode = intent.getIntExtra("ACCESSORY_INFO_EVENT_CODE", -1)
        val accessoryContextId = intent.getStringExtra("ACCESSORY_INFO_CONTEXT_ID")
        Log.i(TAG, "vendorId : " + intent.getIntExtra("ACCESSORY_INFO_VENDER_ID", -1))
        Log.i(TAG, "productId : " + intent.getIntExtra("ACCESSORY_INFO_PRODUCT_ID", -1))
        Log.i(TAG, "serialNumber : " + intent.getStringExtra("ACCESSORY_INFO_SERIAL_NUMBER"))
        Log.i(TAG, "RegistrationType : " + intent.getIntExtra("ACCESSORY_INFO_REGISTRATION_TYPE", -1))
        Log.i(TAG, "eventCode : $eventCode")
        Log.i(TAG, "timeStamp : $timeStamp")
        if (eventCode == 0 || eventCode == 1){
            Log.i(TAG, "accessoryContextId: $accessoryContextId")
            Log.i(TAG, StringBuilder().append("eventCode=").append(eventCode)
                .append(", timestamp=").append(timeStamp)
                .append("\n")
                .toString())

            if (!isInitializedSDK) {
                Log.d(TAG, "Initialized SDK  is false")
                return
            }
            Log.d(TAG, "onContextChange, mAccessoryContextId : $mAccessoryContextId")
            if (accessoryContextId != mAccessoryContextId) {
                mAccessoryContextId = accessoryContextId
                accessoryContextId?.let { openAccessory(it) }
            }


        }
        else if (eventCode == 2){
            Log.i(TAG, "Accessory context is expired")
            mAccessoryContextId = null
        }
    }

    private fun onReceive(intent: Intent) {
        Log.i(TAG, "AccessoryObserver onReceive()")
        val reportOrdinal = intent.getLongExtra("ACCESSORY_REPORT_INFO_ORDINAL", -1)
        if (mOrdinal != reportOrdinal){
            mOrdinal = reportOrdinal
            Log.i(TAG, "vendorId : " + intent.getIntExtra("ACCESSORY_INFO_VENDER_ID", -1))
            Log.i(TAG, "productId : " + intent.getIntExtra("ACCESSORY_INFO_PRODUCT_ID", -1))
            Log.i(TAG, "serialNumber : " + intent.getStringExtra("ACCESSORY_INFO_SERIAL_NUMBER"))
            Log.i(TAG, "ordinal : " + intent.getLongExtra("ACCESSORY_REPORT_INFO_ORDINAL", -1))
            Log.i(TAG, "timeStamp : " + intent.getStringExtra("ACCESSORY_REPORT_INFO_TIMESTAMP"))
            Log.i(TAG, "data : " + intent.getStringExtra("ACCESSORY_REPORT_INFO_DATA"))
            val principal = currentPrincipal
            if (principal != null && !principal.isAuthenticated) {
                initiateSignIn()
            } else {
                signOut()
            }
        }
        else{
            // Not sure yet, but this could be the place where we receive the same data from the accessory
        }
    }

    private val currentPrincipal: Principal?
        get() {
            val result = Result()
            Log.d(TAG, "getCurrentPrincipal")
            val principal = appContext?.let { AccessService.getCurrentPrincipal(it, result) }
            return if (result.code == Result.RESULT_OK) {
                showLogResult("AccessService.getCurrentPrincipal(): ", result)
                principal
            } else {
                null
            }
        }

    private fun openAccessory(accessoryContextId: String) {
        val result = Result()
        Log.d(TAG, "Accessory opened: $accessoryContextId")
        appContext?.let { AccessoryService.open(it, accessoryContextId, result) }
        showLogResult("AccessoryService.open(): ", result)
        if (result.code == Result.RESULT_OK) {
            startReadingAccessory(accessoryContextId)
        }
    }
    private fun startReadingAccessory(accessoryContextId: String) {
        val result = Result()
        Log.d(TAG, "Accessory startReading: $accessoryContextId")
        appContext?.let { AccessoryService.startReading(it, accessoryContextId, result) }
        showLogResult("AccessoryService.startReading(): ", result)
    }

    private fun initiateSignIn() {
        val result = Result()
        Log.d(TAG, "initiateSignIn")
        appContext?.let { AccessService.initiateSignIn(it, result) }
        showLogResult("AccessService.initiateSignIn(): ", result)
    }

    private fun signOut() {
        val result = Result()
        Log.d(TAG, "signOut")
        appContext?.let { AccessService.signOut(it, result) }
        showLogResult("AccessService.signOut() ", result)
    }


    //Logger
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