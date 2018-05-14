package io.ipoli.android.quest.receiver

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.AsyncBroadcastReceiver
import io.ipoli.android.common.IntentUtil
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.view.AndroidIcon
import io.ipoli.android.common.view.asThemedWrapper
import io.ipoli.android.common.view.largeIcon
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.reminder.ReminderNotificationPopup
import io.ipoli.android.quest.reminder.ReminderNotificationViewModel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit
import space.traversal.kapsule.required
import java.util.*

class ReminderReceiver : AsyncBroadcastReceiver() {

    private val findQuestsToRemindUseCase by required { findQuestsToRemindUseCase }
    private val snoozeQuestUseCase by required { snoozeQuestUseCase }
    private val findPetUseCase by required { findPetUseCase }
    private val reminderScheduler by required { reminderScheduler }

    override suspend fun onReceiveAsync(context: Context, intent: Intent) {

        if (intent.action != ReminderReceiver.ACTION_SHOW_REMINDER) {
            return
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val c = context.asThemedWrapper()
        val remindAt = intent.extras.getLong("remindAtUTC", -1)

        require(remindAt >= 0)

        val remindDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(remindAt),
            ZoneId.systemDefault()
        )

        val quests = findQuestsToRemindUseCase.execute(remindDateTime)
        val pet = findPetUseCase.execute(Unit)

        launch(UI) {
            quests.forEach {

                val reminder = it.reminders.first()
                val message =
                    reminder.message.let { if (it.isEmpty()) "Ready for a quest?" else it }

                val startTimeMessage = startTimeMessage(it)

                val iconicsDrawable = IconicsDrawable(context)
                val icon = it.icon?.let {
                    val androidIcon = AndroidIcon.valueOf(it.name)
                    iconicsDrawable.largeIcon(
                        androidIcon.icon,
                        androidIcon.color
                    )
                } ?: iconicsDrawable.largeIcon(
                    GoogleMaterial.Icon.gmd_notifications_active,
                    R.color.md_blue_500
                )

                val questName = it.name
                val notificationId = showNotification(
                    context,
                    questName,
                    message,
                    icon,
                    notificationManager
                )

                val viewModel = ReminderNotificationViewModel(
                    it.id,
                    questName,
                    message,
                    startTimeMessage,
                    pet
                )
                ReminderNotificationPopup(viewModel,
                    object : ReminderNotificationPopup.OnClickListener {
                        override fun onDismiss() {
                            notificationManager.cancel(notificationId)
                        }

                        override fun onSnooze() {
                            notificationManager.cancel(notificationId)
                            launch(CommonPool) {
                                snoozeQuestUseCase.execute(it.id)
                            }
                            Toast.makeText(c, "Quest snoozed", Toast.LENGTH_SHORT).show()
                        }

                        override fun onStart() {
                            notificationManager.cancel(notificationId)
                            c.startActivity(IntentUtil.showTimer(it.id, c))
                        }
                    }).show(c)
            }
        }
        reminderScheduler.schedule()
    }

    private fun showNotification(
        context: Context,
        questName: String,
        message: String,
        icon: IconicsDrawable,
        notificationManager: NotificationManager
    ): Int {
        val sound =
            Uri.parse("android.resource://" + context.packageName + "/" + R.raw.notification)
        val notification = createNotification(context, questName, icon, message, sound)

        val notificationId = Random().nextInt()

        notificationManager.notify(notificationId, notification)
        return notificationId
    }

    private fun createNotification(
        context: Context,
        title: String,
        icon: IconicsDrawable,
        message: String,
        sound: Uri
    ) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val builder = Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.graphics.drawable.Icon.createWithBitmap(icon.toBitmap()))
                .setLargeIcon(icon.toBitmap())
                .setSound(sound)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(Constants.NOTIFICATION_CHANNEL_ID)
            }

            builder.setDefaults(Notification.DEFAULT_ALL)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .build()
        } else {
            NotificationCompat.Builder(
                context,
                Constants.NOTIFICATION_CHANNEL_ID
            )
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(sound)
                .setLargeIcon(icon.toBitmap())
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .build()
        }

    private fun startTimeMessage(quest: Quest): String {
        val daysDiff = ChronoUnit.DAYS.between(quest.scheduledDate, LocalDate.now())
        return if (daysDiff > 0) {
            "Starts in $daysDiff day(s)"
        } else {
            val minutesDiff = quest.startTime!!.toMinuteOfDay() - Time.now().toMinuteOfDay()

            when {
                minutesDiff > Time.MINUTES_IN_AN_HOUR -> "Starts at ${quest.startTime.toString(false)}"
                minutesDiff > 0 -> "Starts in $minutesDiff min"
                else -> "Starts now"
            }
        }
    }

    companion object {
        const val ACTION_SHOW_REMINDER = "io.ipoli.android.intent.action.SHOW_REMINDER"
    }
}