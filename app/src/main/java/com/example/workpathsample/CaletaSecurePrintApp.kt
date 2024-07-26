package com.example.workpathsample

import android.app.Application
import com.example.workpathsample.task.InitializationTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CaletaSecurePrintApp : Application() {
    override fun onCreate() {
        super.onCreate()
        GlobalScope.launch(Dispatchers.Default) {
            InitializationTask(applicationContext).execute()
        }
    }
}