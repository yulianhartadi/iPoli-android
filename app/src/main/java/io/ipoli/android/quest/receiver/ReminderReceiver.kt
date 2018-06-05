package io.ipoli.android.quest.receiver

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.AsyncBroadcastReceiver
import io.ipoli.android.common.IntentUtil
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.notification.NotificationUtil
import io.ipoli.android.common.notification.ScreenUtil
import io.ipoli.android.common.view.AndroidIcon
import io.ipoli.android.common.view.asThemedWrapper
import io.ipoli.android.common.view.largeIcon
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.reminder.PetNotificationPopup
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

                val viewModel = PetNotificationPopup.ViewModel(
                    headline = questName,
                    title = message,
                    body = startTimeMessage,
                    petAvatar = pet.avatar,
                    petState = pet.state
                )
                PetNotificationPopup(
                    viewModel,
                    onDismiss = {
                        notificationManager.cancel(notificationId)
                    },
                    onSnooze = {
                        notificationManager.cancel(notificationId)
                        launch(CommonPool) {
                            snoozeQuestUseCase.execute(it.id)
                        }
                        Toast
                            .makeText(c, c.getString(R.string.remind_in_15), Toast.LENGTH_SHORT)
                            .show()
                    },
                    onStart = {
                        notificationManager.cancel(notificationId)
                        c.startActivity(IntentUtil.showTimer(it.id, c))
                    }
                ).show(c)
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
        val notification = NotificationUtil.createDefaultNotification(
            context = context,
            title = questName,
            icon = icon.toBitmap(),
            message = message,
            sound = sound,
            channelId = Constants.REMINDERS_NOTIFICATION_CHANNEL_ID
        )

        val notificationId = Random().nextInt()

        notificationManager.notify(notificationId, notification)
        ScreenUtil.awakeScreen(context)
        return notificationId
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