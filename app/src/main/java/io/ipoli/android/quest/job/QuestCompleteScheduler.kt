package io.ipoli.android.quest.job

import android.content.Context
import io.ipoli.android.common.Reward
import io.ipoli.android.common.di.Module
import io.ipoli.android.common.view.asThemedWrapper
import io.ipoli.android.myPoliApp
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.view.QuestCompletePopup
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import space.traversal.kapsule.Kapsule

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/15/17.
 */

interface QuestCompleteScheduler {
    fun schedule(reward: Reward)
}

class AndroidJobQuestCompleteScheduler(private val context: Context) : QuestCompleteScheduler {
    override fun schedule(reward: Reward) {

        val c = context.asThemedWrapper()

        val kap = Kapsule<Module>()
        val playerRepository by kap.required { playerRepository }
        kap.inject(myPoliApp.module(context))

        val bounty = reward.bounty

        val petAvatar = playerRepository.find()!!.pet.avatar
        val petHeadImage = AndroidPetAvatar.valueOf(petAvatar.name).headImage
        launch(UI) {
            QuestCompletePopup(
                petHeadImage,
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