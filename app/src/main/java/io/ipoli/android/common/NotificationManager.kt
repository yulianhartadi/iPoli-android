package io.ipoli.android.common

import android.content.Context
import android.support.v4.app.NotificationManagerCompat
import io.ipoli.android.Constants

interface NotificationManager {
    fun removeTimerNotification()
}

class AndroidNotificationManager(private val context: Context) : NotificationManager {

    override fun removeTimerNotification() {
        NotificationManagerCompat.from(context).cancel(Constants.QUEST_TIMER_NOTIFICATION_ID)
    }

}