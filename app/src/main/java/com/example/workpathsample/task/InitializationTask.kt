package com.example.workpathsample.task

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.workpathsample.CaletaAuthenticationActivity
import com.hp.workpath.api.SsdkUnsupportedException
import com.hp.workpath.api.Workpath
import com.hp.workpath.api.access.AccessService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

class InitializationTask(val context: Context) {

    private var mThrowable: Throwable? = null
    //private val mContextRef: WeakReference<?> = WeakReference(context)

    suspend fun execute() {
        var status = InitStatus.NO_ERROR

        context.run {
            try {
                Log.i("InitializationTask", "Initializing Workpath SDK")
                Workpath.getInstance().initialize(this)
                if (!AccessService.isSupported(this)) {
                    status = InitStatus.NOT_SUPPORTED
                }
            } catch (sue: SsdkUnsupportedException) {
                mThrowable = sue
                status = InitStatus.INIT_EXCEPTION
            } catch (se: SecurityException) {
                mThrowable = se
                status = InitStatus.INIT_EXCEPTION
            } catch (t: Throwable) {
                mThrowable = t
                status = InitStatus.INIT_EXCEPTION
            }

            onPostExecute(status, context)
        }
    }

    private suspend fun onPostExecute(status: InitStatus, context: Context) {
        withContext(Dispatchers.Main) {
            context.run {
                Log.i("InitializationTask", "onPostExecute $status")
                if (status == InitStatus.NO_ERROR) {
                    val principal = AccessService.getCurrentPrincipal(context, null)
                    if (principal != null) {
                        Log.i("InitializationTask", principal.toString())
                    } else {
                        Log.i("InitializationTask", "Principal is null")
                    }
                    //handleComplete()
                } else {
                    //handleException(mThrowable)
                    Log.i("InitializationTask", "Initialization Error")
                    Log.i("InitializationTask", mThrowable.toString())
                }
            }
        }
    }

    enum class InitStatus {
        INIT_EXCEPTION, NOT_SUPPORTED, NO_ERROR
    }
}