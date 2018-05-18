package io.ipoli.android.planday.job

import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.IntentUtil
import io.ipoli.android.common.NotificationUtil
import io.ipoli.android.common.datetime.*
import io.ipoli.android.common.di.Module
import io.ipoli.android.common.view.asThemedWrapper
import io.ipoli.android.myPoliApp
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.planday.usecase.FindNextPlanDayTimeUseCase
import io.ipoli.android.quest.reminder.PetNotificationPopup
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.threeten.bp.LocalDate
import space.traversal.kapsule.Kapsule
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 05/18/2018.
 */

class PlanDayJob : Job() {

    override fun onRunJob(params: Params): Result {

        val kap = Kapsule<Module>()
        val findPetUseCase by kap.required { findPetUseCase }
        val planDayScheduler by kap.required { planDayScheduler }
        kap.inject(myPoliApp.module(context))

        val pet = findPetUseCase.execute(Unit)
        launch(UI) {
            val vm = PetNotificationPopup.ViewModel(
                headline = "Time to plan your day",
                title = null,
                body = null,
                petAvatar = pet.avatar,
                petState = pet.state
            )
            val c = getContext().asThemedWrapper()

            val icon = BitmapFactory.decodeResource(
                c.resources,
                AndroidPetAvatar.valueOf(pet.avatar.name).headImage
            )

            val sound =
                Uri.parse("android.resource://" + c.packageName + "/" + R.raw.notification)

            val notification = NotificationUtil.createDefaultNotification(
                context = c,
                icon = icon,
                title = "Time to plan your day",
                message = "Amazing new day ahead!",
                sound = sound,
                channelId = Constants.PLAN_DAY_NOTIFICATION_CHANNEL_ID
            )

            val notificationId = Random().nextInt()

            val notificationManager =
                c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.notify(notificationId, notification)

            PetNotificationPopup(vm,
                onDismiss = {
                    notificationManager.cancel(notificationId)
                },
                onSnooze = {
                    notificationManager.cancel(notificationId)
                    launch(CommonPool) {
                        planDayScheduler.scheduleAfter(15.minutes)
                    }
                    Toast.makeText(c, c.getString(R.string.plan_day_snooze), Toast.LENGTH_SHORT)
                        .show()
                },
                onStart = {
                    notificationManager.cancel(notificationId)
                    c.startActivity(IntentUtil.startPlanDay(c))
                }).show(c)
        }

        return Result.SUCCESS
    }

    companion object {
        const val TAG = "job_plan_day_tag"
    }
}

interface PlanDayScheduler {
    fun scheduleAfter(minutes: Duration<Minute>)
    fun scheduleForNextTime()
}

class AndroidPlanDayScheduler : PlanDayScheduler {

    override fun scheduleAfter(minutes: Duration<Minute>) {
        JobRequest.Builder(PlanDayJob.TAG)
            .setUpdateCurrent(true)
            .setExact(minutes.millisValue)
            .build()
            .schedule()
    }

    override fun scheduleForNextTime() {
        launch(CommonPool) {
            val context = myPoliApp.instance

            val kap = Kapsule<Module>()
            val findNextPlanDayTimeUseCase by kap.required { findNextPlanDayTimeUseCase }
            kap.inject(myPoliApp.module(context))

            val scheduleTime = findNextPlanDayTimeUseCase.execute(
                FindNextPlanDayTimeUseCase.Params(
                    currentDate = LocalDate.now(),
                    currentTime = Time.now().plus(1)
                )
            )

            if (scheduleTime == null) {
                JobManager.instance().cancelAllForTag(PlanDayJob.TAG)
            } else {
                JobRequest.Builder(PlanDayJob.TAG)
                    .setUpdateCurrent(true)
                    .setExact(scheduleTime.toMillis() - System.currentTimeMillis())
                    .build()
                    .schedule()
            }
        }
    }

}

