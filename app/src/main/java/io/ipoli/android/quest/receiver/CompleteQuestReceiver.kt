package io.ipoli.android.quest.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import io.ipoli.android.Constants
import io.ipoli.android.common.di.Module
import io.ipoli.android.common.view.AppWidgetUtil
import io.ipoli.android.myPoliApp
import io.ipoli.android.quest.show.usecase.CompleteTimeRangeUseCase
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/10/2018.
 */
class CompleteQuestReceiver : BroadcastReceiver(), Injects<Module> {

    private val completeTimeRangeUseCase by required { completeTimeRangeUseCase }

    override fun onReceive(context: Context, intent: Intent) {
        inject(myPoliApp.module(context))
        removeTimerNotification(context)
        val questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY)
        val res = goAsync()
        launch(CommonPool) {

            completeTimeRangeUseCase.execute(CompleteTimeRangeUseCase.Params(questId))

            AppWidgetUtil.updateAgendaWidget(context)

            res.finish()
        }

    }

    private fun removeTimerNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(Constants.QUEST_TIMER_NOTIFICATION_ID)
    }
}