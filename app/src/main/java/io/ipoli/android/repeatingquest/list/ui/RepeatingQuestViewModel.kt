package io.ipoli.android.repeatingquest.list.ui

import io.ipoli.android.repeatingquest.data.RepeatingQuest

data class RepeatingQuestViewModel(val name: String) {
    companion object {
        fun create(repeatingQuest: RepeatingQuest): RepeatingQuestViewModel =
            RepeatingQuestViewModel(repeatingQuest.name!!)
    }
}