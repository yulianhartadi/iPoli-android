package io.ipoli.android.habit.data

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.persistence.EntityWithTags
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.tag.Tag
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/16/18.
 */
data class Habit(
    override val id: String = "",
    val name: String,
    val color: Color,
    val icon: Icon,
    override val tags: List<Tag> = listOf(),
    val days: Set<DayOfWeek>,
    val isGood: Boolean,
    val timesADay: Int = 1,
    val challengeId: String? = null,
    val note: String = "",
    val history: Map<LocalDate, CompletedEntry> = emptyMap(),
    val currentStreak: Int = 0,
    val prevStreak: Int = 0,
    val bestStreak: Int = 0,
    val isRemoved: Boolean = false,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now(),
    val removedAt: Instant? = null
) : EntityWithTags {

    fun isCompletedFor(date: LocalDate = LocalDate.now()): Boolean {
        if (!shouldBeDoneOn(date)) return true

        if (!history.containsKey(date)) return false

        if (history[date]!!.completedCount >= timesADay) return true

        return false
    }

    fun completedForDateCount(date: LocalDate = LocalDate.now()): Int {
        if (!history.containsKey(date)) return 0
        return history[date]!!.completedCount
    }

    fun shouldBeDoneOn(date: LocalDate = LocalDate.now()) = days.contains(date.dayOfWeek)
}

data class CompletedEntry(
    val completedAtTimes: List<Time> = emptyList(),
    val experience: Int? = null,
    val coins: Int? = null
) {
    val completedCount = completedAtTimes.size

    fun complete(time: Time = Time.now()) =
        copy(
            completedAtTimes = completedAtTimes + time
        )

    fun undoLastComplete() =
        copy(
            completedAtTimes = completedAtTimes.dropLast(1)
        )
}