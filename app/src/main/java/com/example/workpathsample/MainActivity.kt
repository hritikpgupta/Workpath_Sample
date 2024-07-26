package com.example.workpathsample

import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.workpathsample.task.AuthenticationInfoTask
import com.example.workpathsample.ui.theme.WorkpathSampleTheme
import com.hp.workpath.api.access.Principal

class MainActivity : ComponentActivity() {

    private lateinit var authenticationInfoTask: AuthenticationInfoTask
    private  var principal: Principal? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        authenticationInfoTask = AuthenticationInfoTask(this)
        principal = authenticationInfoTask.getAuthenticatedUserInfo()
        if(principal != null){
            Log.i("MainActivity", principal.toString())
        }else{
            Log.i("MainActivity", "Principal is null")
        }
        setContent {
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
                            name = principal?.username ?: "World",
                            modifier = Modifier.padding(innerPadding)
                        )
                        Greeting(
                            name = if(principal != null) principal.toString() else "Not Authenticated",
                            modifier = Modifier.padding(innerPadding)
                        )

                    }
                }
            }
        }
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
