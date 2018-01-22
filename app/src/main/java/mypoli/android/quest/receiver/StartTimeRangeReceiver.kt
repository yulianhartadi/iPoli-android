package mypoli.android.quest.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import mypoli.android.Constants
import mypoli.android.common.di.SimpleModule
import mypoli.android.myPoliApp
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 01/22/2018.
 */
class StartTimeRangeReceiver : BroadcastReceiver(), Injects<SimpleModule> {

    private val saveQuestActualDurationUseCase by required { saveQuestActualDurationUseCase }

    override fun onReceive(context: Context, intent: Intent) {
        inject(myPoliApp.simpleModule(context))
        val questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY)
//        saveQuestActualDurationUseCase.execute(
//            SaveQuestActualDurationUseCase.Params(
//                questId
//            )
//        )
    }

}