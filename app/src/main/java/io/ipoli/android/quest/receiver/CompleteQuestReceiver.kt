package io.ipoli.android.quest.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import io.ipoli.android.Constants
import io.ipoli.android.common.AsyncBroadcastReceiver
import io.ipoli.android.common.di.Module
import io.ipoli.android.common.view.AppWidgetUtil
import io.ipoli.android.myPoliApp
import io.ipoli.android.quest.show.usecase.CompleteTimeRangeUseCase
import kotlinx.coroutines.experimental.android.UI
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/10/2018.
 */
class CompleteQuestReceiver : AsyncBroadcastReceiver() {

    private val completeTimeRangeUseCase by required { completeTimeRangeUseCase }

    override suspend fun onReceiveAsync(context: Context, intent: Intent) {
        val questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY)
        completeTimeRangeUseCase.execute(CompleteTimeRangeUseCase.Params(questId))

        launch(UI) {
            removeTimerNotification(context)
            AppWidgetUtil.updateAgendaWidget(context)
        }
    }

    private fun removeTimerNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(Constants.QUEST_TIMER_NOTIFICATION_ID)
    }
}