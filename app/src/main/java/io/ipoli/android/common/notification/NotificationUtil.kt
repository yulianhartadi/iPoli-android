package io.ipoli.android.common.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import io.ipoli.android.R

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 05/18/2018.
 */
object NotificationUtil {

    @Suppress("DEPRECATION")
    fun createDefaultNotification(
        context: Context,
        title: String,
        icon: Bitmap,
        message: String,
        sound: Uri,
        channelId: String,
        contentIntent: PendingIntent
    ): Notification =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val builder = Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(contentIntent)
                .setSmallIcon(android.graphics.drawable.Icon.createWithBitmap(icon))
                .setLargeIcon(icon)
                .setSound(sound)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(channelId)
            }

            builder.setDefaults(Notification.DEFAULT_ALL)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .build()
        } else {
            NotificationCompat.Builder(
                context,
                channelId
            )
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(contentIntent)
                .setSound(sound)
                .setLargeIcon(icon)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .build()
        }

}