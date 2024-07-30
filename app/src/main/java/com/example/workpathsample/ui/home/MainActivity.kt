package com.example.workpathsample.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.workpathsample.R
import com.example.workpathsample.task.AuthenticationTask
import com.example.workpathsample.task.InitializationTask
import com.example.workpathsample.ui.theme.WorkpathSampleTheme
import com.hp.workpath.api.SsdkUnsupportedException
import com.hp.workpath.api.access.Principal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var mAlertDialog: AlertDialog

    private lateinit var authenticationTask: AuthenticationTask
    private var principal: Principal? = null

    var currentPrincipal = MutableStateFlow(principal)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val _currentPrincipal = currentPrincipal.collectAsState()

            WorkpathSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Greeting(
                            name = _currentPrincipal.value?.username ?: "World",
                            modifier = Modifier.padding(innerPadding)
                        )
                        Greeting(
                            name = if (_currentPrincipal.value != null) _currentPrincipal.value.toString() else "Not Authenticated",
                            modifier = Modifier.padding(innerPadding)
                        )

                    }
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        // call init task
        lifecycleScope.launch(Dispatchers.Default) {
            InitializationTask(this@MainActivity, initializeInterface).execute()
            authenticationTask = AuthenticationTask(applicationContext)
            principal = authenticationTask.getAuthenticatedUserInfo()
            if (principal != null) {
                currentPrincipal.value = principal
                Log.i("MainActivity", principal.toString())
            } else {
                Log.i("MainActivity", "Principal is null")
            }
        }
    }
    private var initializeInterface: InitializationTask.InitializeInterface = object :
        InitializationTask.InitializeInterface {
        override fun handleComplete() {
        }

        override fun handleException(t: Throwable?) {
            this@MainActivity.handleException(t)
        }
    }
    fun handleException(t: Throwable?) {
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
        mAlertDialog = AlertDialog.Builder(this@MainActivity)
            .setTitle("Error")
            .setMessage(errorMsg)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }




}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        fontSize = 24.sp,
        modifier = modifier
    )
}
