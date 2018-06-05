package io.ipoli.android.common.notification

import android.content.Context
import android.os.PowerManager
import io.ipoli.android.common.datetime.seconds

object ScreenUtil {

    fun awakeScreen(context: Context) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        @Suppress("DEPRECATION")
        val lock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                .or(PowerManager.ACQUIRE_CAUSES_WAKEUP)
                .or(PowerManager.ON_AFTER_RELEASE),
            "SHOW_SCREEN"
        )
        lock.acquire(10.seconds.millisValue)
    }
}