package io.ipoli.android.quest.job

import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import io.ipoli.android.common.Reward
import io.ipoli.android.common.di.Module
import io.ipoli.android.common.view.asThemedWrapper
import io.ipoli.android.myPoliApp
import io.ipoli.android.pet.Food
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.view.QuestCompletePopup
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import space.traversal.kapsule.Injects

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/15/17.
 */

interface QuestCompleteScheduler {
    fun schedule(reward: Reward)
}

class AndroidJobQuestCompleteScheduler : QuestCompleteScheduler {
    override fun schedule(reward: Reward) {

        val c = myPoliApp.instance.asThemedWrapper()

        val bounty = reward.bounty
        launch(UI) {
            QuestCompletePopup(
                reward.experience,
                reward.coins,
                if (bounty is Quest.Bounty.Food) {
                    bounty.food
                } else {
                    null
                }
            ).show(c)
        }
    }

}