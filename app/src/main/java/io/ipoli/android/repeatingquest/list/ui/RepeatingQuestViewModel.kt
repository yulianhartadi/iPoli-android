package io.ipoli.android.repeatingquest.list.ui

import android.support.annotation.DrawableRes
import io.ipoli.android.repeatingquest.data.RepeatingQuest

data class RepeatingQuestViewModel(val name: String, @DrawableRes val categoryImage: Int) {
    companion object {
        fun create(repeatingQuest: RepeatingQuest): RepeatingQuestViewModel =
            RepeatingQuestViewModel(repeatingQuest.name!!,
                repeatingQuest.categoryType.colorfulImage)
    }
}