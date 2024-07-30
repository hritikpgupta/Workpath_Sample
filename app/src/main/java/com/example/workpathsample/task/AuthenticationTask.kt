package com.example.workpathsample.task

import android.content.Context
import com.example.workpathsample.ui.home.MainActivity
import com.hp.workpath.api.access.AccessService
import com.hp.workpath.api.access.Principal

class AuthenticationTask(private var context: Context) {

     suspend fun getAuthenticatedUserInfo(): Principal? {
         try {
             val principal = AccessService.getCurrentPrincipal(context, null)
             if (principal != null) {
                 if (principal.userEmail == null) {
                     return null
                 } else {
                     return principal
                 }
             }
         }catch (e: Exception){
             e.printStackTrace()
         }
         return null
    }
}