package io.ipoli.android.quest

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/15/17.
 */
interface QuestCompleteScheduler {
    fun schedule(questId: String)
}

class AndroidJobQuestCompleteScheduler : QuestCompleteScheduler {
    override fun schedule(questId: String) {

    }

}