package mypoli.android.quest

import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import mypoli.android.common.Reward
import mypoli.android.common.di.Module
import mypoli.android.common.view.asThemedWrapper
import mypoli.android.pet.Food
import mypoli.android.quest.view.QuestCompletePopup
import space.traversal.kapsule.Injects

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/15/17.
 */

class QuestCompleteJob : Job(), Injects<Module> {
    override fun onRunJob(params: Params): Result {
        val experience = params.extras.getInt("experience", 0)
        val coins = params.extras.getInt("coins", 0)

        require(experience > 0)
        require(coins > 0)

        val bountyParam = params.extras.getString("bounty", null)
        val bounty = bountyParam?.let {
            Food.valueOf(it)
        }

        val c = context.asThemedWrapper()

        launch(UI) {
            QuestCompletePopup(
                experience,
                coins,
                bounty
            ).show(c)
        }

        return Result.SUCCESS
    }

    companion object {
        val TAG = "job_quest_complete_tag"
    }
}

interface QuestCompleteScheduler {
    fun schedule(reward: Reward)
}

class AndroidJobQuestCompleteScheduler : QuestCompleteScheduler {
    override fun schedule(reward: Reward) {
        val bundle = PersistableBundleCompat()
        bundle.putInt("experience", reward.experience)
        bundle.putInt("coins", reward.coins)
        val bounty = reward.bounty
        if (bounty is Quest.Bounty.Food) {
            bundle.putString("bounty", bounty.food.name)
        }

        JobRequest.Builder(QuestCompleteJob.TAG)
            .setExtras(bundle)
            .startNow()
            .build()
            .schedule()
    }

}