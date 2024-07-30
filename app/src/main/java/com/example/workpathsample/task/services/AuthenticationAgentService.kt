package com.example.workpathsample.task.services

import android.text.TextUtils
import android.util.Log
import com.example.workpathsample.R
import com.example.workpathsample.task.InitializationTask
import com.example.workpathsample.util.ActionUtil
import com.hp.workpath.api.Result
import com.hp.workpath.api.access.AbstractAuthenticationService
import com.hp.workpath.api.access.AccessService
import com.hp.workpath.api.access.AuthenticationAttributes
import com.hp.workpath.api.access.AuthenticationAttributes.WindowsBuilder
import com.hp.workpath.api.access.Principal
import com.hp.workpath.api.access.SignInAction
import com.hp.workpath.api.access.UserOverridesAttributes
import com.hp.workpath.api.access.UserPreferencesAttributes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutionException


class AuthenticationAgentService : AbstractAuthenticationService() {
    private lateinit var mInitializationTask: Job
    private var isInitializedSDK = false

    fun showLogResult(msg: String?) {
        showLogResult(msg, null)
    }
    fun showLogResult(msg: String?, result: Result?) {
        var message = msg
        if (result != null) {
            message = msg + "\n"+ build(result)
            if (result.code == Result.RESULT_FAIL) {
                Log.e("AuthenticationAgentService", message)
            } else {
                Log.d("AuthenticationAgentService", message)
            }
        } else {
            message?.let { Log.d("AuthenticationAgentService", it) }
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


    override fun onCreate() {
        super.onCreate()
        initializedSDK()
    }
    private fun initializedSDK() {
        try {
            if (!isInitializedSDK) {
                mInitializationTask = GlobalScope.launch(Dispatchers.Main) {
                    val initStatus = InitializationTask(applicationContext, null).execute()
                    if (initStatus === InitializationTask.InitStatus.NO_ERROR) {
                        isInitializedSDK = true
                        Log.i("AuthenticationAgentService", "Received initializedSDK true")
                    } else {
                        Log.e("AuthenticationAgentService", getString(R.string.sdk_support_missing))
                    }
                }
            }
        } catch (ie: InterruptedException) {
            showLogResult("Workpath.getInstance().initialize ${ie.message}")
        } catch (ee: ExecutionException) {
            showLogResult("Workpath.getInstance().initialize ${ee.message}")
        }
    }


    override fun onSignIn(principal: Principal?) {
        Log.i("AuthenticationAgentService", "Received sign in event: " + principal?.username)
    }

    override fun onSignOut() {
        Log.i("AuthenticationAgentService", "Received sign out event")
    }

    override fun onPrePrompt() {
        Log.i("AuthenticationAgentService", "Received onPrePrompt event")
        try {
            val result = Result()
            val signInAction = SignInAction(ActionUtil.getAction(this), null)
            Log.d("AuthenticationService", "signInAction: ${signInAction.action.name}")
            Log.d("AuthenticationService", "signInAction: ${signInAction.action.ordinal}")
            AccessService.signIn(this@AuthenticationAgentService, signInAction, windowsData, result)
            if (result.code != Result.RESULT_OK) {
                showLogResult("AccessService.signIn", result)
                val failAction = SignInAction(SignInAction.Action.FAIL, result.cause)
                AccessService.signIn(this@AuthenticationAgentService, failAction, null, null)
            }
        } catch (t: Throwable) {
            showLogResult("AccessService.signIn ${t.message}")
        }
    }

    @get:Throws(Exception::class)
    private val windowsData: AuthenticationAttributes
        get() {
            val userOverridesAttributes = UserOverridesAttributes.Builder()
                .addBccAddress(
                    getString(R.string.bcc_address_email_01),
                    getString(R.string.bcc_address_name_01)
                )
                .addBccAddress(
                    getString(R.string.bcc_address_email_02),
                    getString(R.string.bcc_address_name_02)
                )
                .addCcAddress(
                    getString(R.string.cc_address_email_01),
                    getString(R.string.cc_address_name_01)
                )
                .addCcAddress(
                    getString(R.string.cc_address_email_02),
                    getString(R.string.cc_address_name_02)
                )
                .setFrom(
                    getString(R.string.from_address_email),
                    getString(R.string.from_address_name)
                )
                .addToAddress(
                    getString(R.string.to_address_email_01),
                    getString(R.string.to_address_name_01)
                )
                .addToAddress(
                    getString(R.string.to_address_email_02),
                    getString(R.string.to_address_name_02)
                )
                .setMessage(getString(R.string.email_message))
                .setSubject(getString(R.string.email_subject))
                .setFaxBillingCode(getString(R.string.fax_billing_code))
                .setFaxCompanyName(getString(R.string.fax_company_name))
                .build()
            val userPreferencesAttributes = UserPreferencesAttributes.Builder()
                .setAutoLaunchAppAccessPointId(getString(R.string.app_access_point_id))
                .setLanguageCode(getString(R.string.language_code))
                .build()
            return WindowsBuilder()
                .setFullyQualifiedName("//dkdonohoe//HGUPTA")
                .setDisplayName("hgupta")
                .setPassword("password")
                .setUserDomain(getString(R.string.value_domain))
                .setUserEmail(getString(R.string.value_user_email, "hgupta"))
                .setUserName("hgupta")
                .setUserPrincipalName("Hritik Gupta")
                .setHomeFolderPath(getString(R.string.value_home_folder_path))
                .addUserProperty(
                    getString(R.string.value_user_property_key_01),
                    getString(R.string.value_user_property_value_01)
                )
                .addUserProperty(
                    getString(R.string.value_user_property_key_02),
                    getString(R.string.value_user_property_value_02)
                )
                .setUserOverridesAttributes(userOverridesAttributes)
                .setUserPreferencesAttributes(null)
                .build()
        }
}
