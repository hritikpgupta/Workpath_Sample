package com.example.workpathsample.ui.auth

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.example.workpathsample.R
import com.example.workpathsample.task.InitializationTask
import com.example.workpathsample.ui.theme.WorkpathSampleTheme
import com.hp.workpath.api.Result
import com.hp.workpath.api.SsdkUnsupportedException
import com.hp.workpath.api.access.AccessService
import com.hp.workpath.api.access.AuthenticationAttributes
import com.hp.workpath.api.access.AuthenticationAttributes.WindowsBuilder
import com.hp.workpath.api.access.SignInAction
import com.hp.workpath.api.access.UserOverridesAttributes
import com.hp.workpath.api.access.UserPreferencesAttributes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CaletaAuthenticationActivity : ComponentActivity() {
    private lateinit var mAlertDialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkpathSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color.White)
                    ) {
                        LoginForm {
                            val result = Result()
                            val signInAction = SignInAction(SignInAction.Action.SUCCESS, null)
                            AccessService.signIn(
                                this@CaletaAuthenticationActivity,
                                signInAction,
                                windowsData,
                                result
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.Default) {
            InitializationTask(this@CaletaAuthenticationActivity, initializeInterface).execute()
        }
    }

    private var initializeInterface: InitializationTask.InitializeInterface = object :
        InitializationTask.InitializeInterface {
        override fun handleComplete() {}

        override fun handleException(t: Throwable?) {
            this@CaletaAuthenticationActivity.handleException(t)
        }
    }

    private fun handleException(t: Throwable?) {
        var errorMsg = ""
        if (t is SsdkUnsupportedException) {
            errorMsg = when (t.type) {
                SsdkUnsupportedException.LIBRARY_NOT_INSTALLED, SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED -> getString(
                    R.string.sdk_support_missing
                )

                else -> getString(R.string.unknown_error)
            }
        } else {
            t?.message?.run {
                errorMsg = this
            }
        }
        Log.e("CaletaAuthneticationActivity", errorMsg)
        mAlertDialog = AlertDialog.Builder(this@CaletaAuthenticationActivity)
            .setTitle("Error")
            .setMessage(errorMsg)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
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



