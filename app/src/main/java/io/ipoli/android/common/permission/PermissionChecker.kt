package io.ipoli.android.common.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.content.ContextCompat
import io.ipoli.android.myPoliApp

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/02/2018.
 */
interface PermissionChecker {
    fun canReadCalendar(): Boolean
}

class AndroidPermissionChecker : PermissionChecker {

    override fun canReadCalendar() =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                myPoliApp.instance,
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
        }
}