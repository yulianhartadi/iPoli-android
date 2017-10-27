package io.ipoli.android

import android.view.ContextThemeWrapper
import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import com.evernote.android.job.JobRequest
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.di.JobModule
import io.ipoli.android.reminder.view.ReminderNotificationOverlay
import io.ipoli.android.reminder.view.ReminderNotificationViewModel
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import space.traversal.kapsule.Injects
import space.traversal.kapsule.Kapsule
import timber.log.Timber

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/26/17.
 */
class iPoliJobCreator : JobCreator {
    override fun create(tag: String): Job? {
        when (tag) {
            ReminderNotificationJob.TAG -> return ReminderNotificationJob()
            else -> return null
        }
    }
}

class ReminderNotificationJob : Job(), Injects<ControllerModule> {

    override fun onRunJob(params: Job.Params): Job.Result {

        val kap = Kapsule<JobModule>()
        val delegate = kap.required { findQuestToRemindUseCase }
        kap.inject(iPoliApp.jobModule(context))

        val findQuestsToRemindUseCase = delegate.value!!

        val c = ContextThemeWrapper(context, R.style.Theme_iPoli)

        launch(UI) {

            val quests = findQuestsToRemindUseCase.execute(params.extras.getLong("start", -1))

            quests.forEach {

                val reminder = it.reminder!!
                val message = reminder.message.let { if (it.isEmpty()) "Ready for a quest?" else it }

                ReminderNotificationOverlay(ReminderNotificationViewModel(it.id, it.name, message, "After 5 min"),
                    object : ReminderNotificationOverlay.OnClickListener {
                        override fun onDismiss() {
                        }

                        override fun onSnooze() {
                        }

                        override fun onDone() {
                            Timber.d("DonnnyyyYYY")
                        }
                    }).show(c)
            }
        }

        return Job.Result.SUCCESS
    }

    companion object {

        val TAG = "job_reminder_notification_tag"

        fun scheduleJob() {
            JobRequest.Builder(ReminderNotificationJob.TAG)
                .setExact(200)
                .build()
                .schedule()

        }
    }
}