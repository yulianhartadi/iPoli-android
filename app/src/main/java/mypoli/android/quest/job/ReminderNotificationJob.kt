package mypoli.android.quest.job

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import mypoli.android.Constants
import mypoli.android.R
import mypoli.android.common.IntentUtil
import mypoli.android.common.datetime.Time
import mypoli.android.common.datetime.toMillis
import mypoli.android.common.di.Module
import mypoli.android.common.view.asThemedWrapper
import mypoli.android.myPoliApp
import mypoli.android.quest.Quest
import mypoli.android.quest.reminder.ReminderNotificationPopup
import mypoli.android.quest.reminder.ReminderNotificationViewModel
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit
import space.traversal.kapsule.Injects
import space.traversal.kapsule.Kapsule
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/26/17.
 */
class ReminderNotificationJob : Job(), Injects<Module> {

    @SuppressLint("NewApi")
    override fun onRunJob(params: Job.Params): Job.Result {

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val kap = Kapsule<Module>()
        val findQuestsToRemindUseCase by kap.required { findQuestToRemindUseCase }
        val snoozeQuestUseCase by kap.required { snoozeQuestUseCase }
        val findPetUseCase by kap.required { findPetUseCase }
        kap.inject(myPoliApp.module(context))

        val c = context.asThemedWrapper()
        val remindAt = params.extras.getLong("remindAtUTC", -1)

        require(remindAt >= 0)

        val remindDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(remindAt),
            ZoneId.systemDefault()
        )
        val quests = findQuestsToRemindUseCase.execute(remindDateTime)
        val pet = findPetUseCase.execute(Unit)

        launch(UI) {
            quests.forEach {

                val reminder = it.reminder!!
                val message =
                    reminder.message.let { if (it.isEmpty()) "Ready for a quest?" else it }

                val startTimeMessage = startTimeMessage(it)

                val questName = it.name
                val notificationId = showNotification(questName, message, notificationManager)

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
                            snoozeQuestUseCase.execute(it.id)
                            Toast.makeText(c, "Quest snoozed", Toast.LENGTH_SHORT).show()
                        }

                        override fun onStart() {
                            notificationManager.cancel(notificationId)
                            c.startActivity(IntentUtil.showTimer(it.id, c))
                        }
                    }).show(c)
            }
        }

        return Job.Result.SUCCESS
    }

    private fun showNotification(
        questName: String,
        message: String,
        notificationManager: NotificationManager
    ): Int {
        val notification = createNotification(questName, message)
        val notificationId = Random().nextInt()
        notificationManager.notify(notificationId, notification)
        return notificationId
    }

    private fun createNotification(title: String, message: String) =
        NotificationCompat.Builder(
            context,
            Constants.NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentTitle(title)
            .setContentText(message)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()

    private fun startTimeMessage(quest: Quest): String {
        val daysDiff = ChronoUnit.DAYS.between(quest.scheduledDate, LocalDate.now())
        return if (daysDiff > 0) {
            "Starts in $daysDiff day(s)"
        } else {
            val minutesDiff = quest.startTime!!.toMinuteOfDay() - Time.now().toMinuteOfDay()

            if (minutesDiff > Time.MINUTES_IN_AN_HOUR) {
                "Starts at ${quest.startTime.toString(false)}"
            } else if (minutesDiff > 0) {
                "Starts in $minutesDiff min"
            } else {
                "Starts now"
            }
        }
    }

    companion object {
        val TAG = "job_reminder_notification_tag"
    }
}

interface ReminderScheduler {
    fun schedule(remindAt: LocalDateTime)
}

class AndroidJobReminderScheduler : ReminderScheduler {
    override fun schedule(remindAt: LocalDateTime) {
        JobManager.instance().cancelAllForTag(ReminderNotificationJob.TAG)

        val bundle = PersistableBundleCompat()
        bundle.putLong("remindAtUTC", remindAt.toMillis())
        JobRequest.Builder(ReminderNotificationJob.TAG)
            .setExtras(bundle)
            .setExact(remindAt.toMillis() - System.currentTimeMillis())
            .build()
            .schedule()
    }
}