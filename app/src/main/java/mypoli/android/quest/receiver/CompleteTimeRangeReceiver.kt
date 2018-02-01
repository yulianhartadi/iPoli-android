package mypoli.android.quest.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import mypoli.android.Constants
import mypoli.android.common.di.Module
import mypoli.android.myPoliApp
import mypoli.android.timer.usecase.CompleteTimeRangeUseCase.Params
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 01/22/2018.
 */
class CompleteTimeRangeReceiver : BroadcastReceiver(), Injects<Module> {

    private val completeTimeRangeUseCase by required { completeTimeRangeUseCase }

    override fun onReceive(context: Context, intent: Intent) {
        inject(myPoliApp.module(context))
        val questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY)
        completeTimeRangeUseCase.execute(Params(questId))

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(Constants.QUEST_TIMER_NOTIFICATION_ID)
    }

}