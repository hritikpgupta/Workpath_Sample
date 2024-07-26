package com.example.workpathsample.task

import com.example.workpathsample.MainActivity
import com.hp.workpath.api.access.AccessService
import com.hp.workpath.api.access.Principal

class AuthenticationInfoTask(var context: MainActivity) {

     fun getAuthenticatedUserInfo(): Principal? {
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