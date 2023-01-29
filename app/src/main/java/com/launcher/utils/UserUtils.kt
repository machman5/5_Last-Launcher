package com.launcher.utils

import android.annotation.TargetApi
import android.content.Context
import android.os.Process
import android.os.UserHandle
import android.os.UserManager

// not in use
@Deprecated("")
@TargetApi(21)
class UserUtils(context: Context) {
    private val userManager: UserManager

    init {
        userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    }

    private fun getSerial(user: UserHandle?): Long {
        return userManager.getSerialNumberForUser(user)
    }

    @Suppress("unused")
    fun getUser(serial: Long): UserHandle {
        return userManager.getUserForSerialNumber(serial)
    }

    @Suppress("unused")
    val currentSerial: Long
        get() = getSerial(currentUser)

    private val currentUser: UserHandle
        get() = Process.myUserHandle()
}
