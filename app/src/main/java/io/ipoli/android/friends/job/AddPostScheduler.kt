package io.ipoli.android.friends.job

import io.ipoli.android.MyPoliApp
import io.ipoli.android.common.IntentUtil

interface AddPostScheduler {
    fun scheduleForQuest(questId: String, challengeId: String)
    fun scheduleForHabit(habitId: String, challengeId: String)
    fun scheduleForChallenge(challengeId: String)
}

class AndroidAddPostScheduler : AddPostScheduler {

    override fun scheduleForQuest(questId: String, challengeId: String) {
        MyPoliApp.instance.startActivity(
            IntentUtil.showAddQuestPost(
                questId,
                challengeId,
                MyPoliApp.instance
            )
        )
    }

    override fun scheduleForHabit(habitId: String, challengeId: String) {
        MyPoliApp.instance.startActivity(
            IntentUtil.showAddHabitPost(
                habitId,
                challengeId,
                MyPoliApp.instance
            )
        )
    }

    override fun scheduleForChallenge(challengeId: String) {
        MyPoliApp.instance.startActivity(
            IntentUtil.showAddChallengePost(
                challengeId,
                MyPoliApp.instance
            )
        )
    }
}