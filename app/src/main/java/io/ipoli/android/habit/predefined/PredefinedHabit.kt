package io.ipoli.android.habit.predefined

import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import org.threeten.bp.DayOfWeek

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/21/18.
 */
data class PredefinedHabit(
    val name: String,
    val color: Color,
    val icon: Icon,
    val isGood: Boolean,
    val timesADay: Int = 1,
    val days: Set<DayOfWeek>
)