package io.ipoli.android.common.permission

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.support.v4.content.ContextCompat

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/02/2018.
 */
interface PermissionChecker {
    fun canReadCalendar(): Boolean

    fun canReadAppUsageStats(): Boolean
}

class AndroidPermissionChecker(private val context: Context) : PermissionChecker {

    override fun canReadCalendar() =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
        }

    override fun canReadAppUsageStats(): Boolean {
        val opsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = opsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}