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
import io.ipoli.android.player.data.Player.Preferences.NotificationStyle
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
    private val playerRepository by required { playerRepository }

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
        val p = playerRepository.find()!!
        val style = p.preferences.reminderNotificationStyle
        val pet = p.pet

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
                val notificationId =
                    if (style == NotificationStyle.NOTIFICATION || style == NotificationStyle.ALL)
                        showNotification(
                            context = context,
                            questId = it.id,
                            questName = questName,
                            message = message,
                            icon = icon,
                            notificationManager = notificationManager
                        ) else null

                if (style == NotificationStyle.POPUP || style == NotificationStyle.ALL) {
                    val viewModel = PetNotificationPopup.ViewModel(
                        headline = questName,
                        title = message,
                        body = startTimeMessage,
                        petAvatar = pet.avatar,
                        petState = pet.state
                    )
                    showPetPopup(viewModel, notificationManager, notificationId, it, c)
                }
            }
        }
        reminderScheduler.schedule()
    }

    private fun showPetPopup(
        viewModel: PetNotificationPopup.ViewModel,
        notificationManager: NotificationManager,
        notificationId: Int?,
        quest: Quest,
        context: Context
    ) {
        PetNotificationPopup(
            viewModel,
            onDismiss = {
                notificationId?.let {
                    notificationManager.cancel(it)
                }
            },
            onSnooze = {
                notificationId?.let {
                    notificationManager.cancel(it)
                }
                launch(CommonPool) {
                    snoozeQuestUseCase.execute(quest.id)
                }
                Toast
                    .makeText(context, context.getString(R.string.remind_in_15), Toast.LENGTH_SHORT)
                    .show()
            },
            onStart = {
                notificationId?.let {
                    notificationManager.cancel(it)
                }
                context.startActivity(IntentUtil.showTimer(quest.id, context))
            }
        ).show(context)
    }

    private fun showNotification(
        context: Context,
        questId: String,
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
            channelId = Constants.REMINDERS_NOTIFICATION_CHANNEL_ID,
            contentIntent = IntentUtil.getActivityPendingIntent(
                context,
                IntentUtil.showTimer(questId, context)
            )
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