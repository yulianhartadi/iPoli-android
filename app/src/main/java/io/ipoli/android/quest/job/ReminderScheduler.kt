package io.ipoli.android.quest.job

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import io.ipoli.android.common.IntentUtil
import io.ipoli.android.common.datetime.toMillis
import io.ipoli.android.common.di.Module
import io.ipoli.android.myPoliApp
import io.ipoli.android.quest.receiver.ReminderReceiver
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import org.threeten.bp.LocalDateTime
import space.traversal.kapsule.Kapsule

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/26/17.
 */

interface ReminderScheduler {
    fun schedule()
}

class AndroidJobReminderScheduler(private val context: Context) : ReminderScheduler {
    override fun schedule() {

        launch(CommonPool) {

            val c = this@AndroidJobReminderScheduler.context

            val kap = Kapsule<Module>()
            val questRepository by kap.required { questRepository }
            kap.inject(myPoliApp.module(c))

            val remindAt = questRepository.findNextReminderTime()

            val alarmManager = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (remindAt == null) {
                alarmManager.cancel(createOperationIntent(c))
                return@launch
            }

            val show = IntentUtil.getActivityPendingIntent(c, IntentUtil.startApp(c))

            val clockInfo = AlarmManager.AlarmClockInfo(remindAt.toMillis(), show)

            alarmManager.setAlarmClock(
                clockInfo,
                createOperationIntent(c, remindAt)
            )
        }

    }

    private fun createOperationIntent(
        context: Context,
        remindAt: LocalDateTime? = null
    ) =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, ReminderReceiver::class.java).apply {
                remindAt?.let {
                    putExtra("remindAtUTC", it.toMillis())
                }
                action = ReminderReceiver.ACTION_SHOW_REMINDER
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )
}