package io.ipoli.android.reminder

import android.view.ContextThemeWrapper
import android.widget.Toast
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import io.ipoli.android.R
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.di.JobModule
import io.ipoli.android.iPoliApp
import io.ipoli.android.quest.Quest
import io.ipoli.android.reminder.view.ReminderNotificationOverlay
import io.ipoli.android.reminder.view.ReminderNotificationViewModel
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import space.traversal.kapsule.Injects
import space.traversal.kapsule.Kapsule

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/26/17.
 */

class ReminderNotificationJob : Job(), Injects<ControllerModule> {

    override fun onRunJob(params: Job.Params): Job.Result {

        val kap = Kapsule<JobModule>()
        val findQuestsToRemindUseCase by kap.required { findQuestToRemindUseCase }
        val snoozeQuestUseCase by kap.required { snoozeQuestUseCase }
        val completeQuestUseCase by kap.required { completeQuestUseCase }
        kap.inject(iPoliApp.jobModule(context))

        val c = ContextThemeWrapper(context, R.style.Theme_iPoli)

        launch(UI) {

            val quests = findQuestsToRemindUseCase.execute(params.extras.getLong("start", -1))

            quests.forEach {

                val reminder = it.reminder!!
                val message = reminder.message.let { if (it.isEmpty()) "Ready for a quest?" else it }

                val startTimeMessage = startTimeMessage(it)

                ReminderNotificationOverlay(ReminderNotificationViewModel(it.id, it.name, message, startTimeMessage),
                    object : ReminderNotificationOverlay.OnClickListener {
                        override fun onDismiss() {
                        }

                        override fun onSnooze() {
                            snoozeQuestUseCase.execute(it.id)
                            Toast.makeText(c, "Quest snoozed", Toast.LENGTH_SHORT).show()
                        }

                        override fun onDone() {
                            completeQuestUseCase.execute(it.id)
                            Toast.makeText(c, "Quest completed", Toast.LENGTH_SHORT).show()
                        }
                    }).show(c)
            }
        }

        return Job.Result.SUCCESS
    }

    private fun startTimeMessage(quest: Quest): String {
        val daysDiff = ChronoUnit.DAYS.between(quest.scheduleDate, LocalDate.now())
        return if (daysDiff > 0) {
            "Starts in $daysDiff day(s)"
        } else {
            val minutesDiff = quest.startTime!!.toMinuteOfDay() - Time.now().toMinuteOfDay()

            if (minutesDiff > Time.MINUTES_IN_AN_HOUR) {
                "Starts at ${quest.startTime.toString(false)}"
            } else {
                "Starts in $minutesDiff min"
            }
        }
    }

    companion object {
        val TAG = "job_reminder_notification_tag"
    }
}

class ReminderScheduler {
    fun schedule(time: Long) {
        JobManager.instance().cancelAllForTag(ReminderNotificationJob.TAG)

        val bundle = PersistableBundleCompat()
        bundle.putLong("start", time)
        JobRequest.Builder(ReminderNotificationJob.TAG)
            .setExtras(bundle)
//                    .setExact(dateTime.toMillis() - System.currentTimeMillis())
            .setExact(100)
            .build()
            .schedule()
    }

}