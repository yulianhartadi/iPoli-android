package mypoli.android.challenge.data

import mypoli.android.common.datetime.Time
import mypoli.android.quest.Color
import mypoli.android.quest.Icon
import org.threeten.bp.DayOfWeek

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
data class Challenge(
    val name: String,
    val category: Category,
    val quests: List<Challenge.Quest>,
    val durationDays: Int = 30
) {

    enum class Category {
        BUILD_SKILL,
        ME_TIME,
        FAMILY_AND_FRIENDS,
        DEEP_WORK,
        ORGANIZE_MY_LIFE,
        HEALTH_AND_FITNESS
    }

    sealed class Quest {
        data class OneTime(
            val text: String,
            val name: String,
            val duration: Int,
            val startTime: Time? = null,
            val color: Color = Color.GREEN,
            val icon: Icon = Icon.STAR,
            val startAtDay: Int? = null,
            val preferredDayOfWeek: DayOfWeek? = null,
            val selected: Boolean = true
        ) : Quest()

        data class Repeating(
            val text: String,
            val name: String,
            val duration: Int,
            val startTime: Time? = null,
            val color: Color = Color.GREEN,
            val icon: Icon = Icon.STAR,
            val startAtDay: Int? = null,
            val weekDays: List<DayOfWeek> = listOf(),
            val selected: Boolean = true
        ) : Quest()
    }
}