package io.ipoli.android.onboarding

import io.ipoli.android.habit.predefined.PredefinedHabit
import io.ipoli.android.quest.RepeatingQuest

data class OnboardData(
    val repeatingQuests: Set<Pair<RepeatingQuest, Tag?>>,
    val habits: Set<Pair<PredefinedHabit, Tag?>>
) {
    enum class Tag { WELLNESS, PERSONAL, WORK }
}