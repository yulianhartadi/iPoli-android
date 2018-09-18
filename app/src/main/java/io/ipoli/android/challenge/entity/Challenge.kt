package io.ipoli.android.challenge.entity

import io.ipoli.android.common.Reward
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.persistence.EntityWithTags
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.quest.*
import io.ipoli.android.tag.Tag
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/05/2018.
 */
data class Challenge(
    override val id: String = "",
    val name: String,
    val color: Color,
    val icon: Icon? = null,
    val difficulty: Difficulty,
    override val tags: List<Tag> = emptyList(),
    val startDate: LocalDate,
    val endDate: LocalDate,
    val motivations: List<String>,
    val reward: Reward? = null,
    val completedAtDate: LocalDate? = null,
    val completedAtTime: Time? = null,
    val nextDate: LocalDate? = null,
    val nextStartTime: Time? = null,
    val nextDuration: Int? = null,
    val baseQuests: List<BaseQuest> = emptyList(),
    val quests: List<Quest> = emptyList(),
    val repeatingQuests: List<RepeatingQuest> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val progress: Progress = Progress(),
    val trackedValues: List<TrackedValue> = emptyList(),
    val note: String = "",
    val sharingPreference: SharingPreference = SharingPreference.PRIVATE,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now(),
    val removedAt: Instant? = null
) : EntityWithTags {

    data class Progress(
        val completedCount: Int = 0,
        val allCount: Int = 0
    )

    sealed class TrackedValue {

        abstract val id: String
        abstract val history: SortedMap<LocalDate, Log>

        data class Progress(
            override val id: String,
            val completedCount: Int = 0,
            val allCount: Int = 0,
            override val history: SortedMap<LocalDate, Log>
        ) : TrackedValue()

        data class Target(
            override val id: String,
            val name: String,
            val units: String,
            val startValue: Double,
            val targetValue: Double,
            val currentValue: Double,
            val remainingValue: Double,
            val isCumulative: Boolean = false,
            override val history: SortedMap<LocalDate, Log>,
            val cumulativeHistory: SortedMap<LocalDate, Log>? = null
        ) : TrackedValue()

        data class Average(
            override val id: String,
            val name: String,
            val units: String,
            val targetValue: Double,
            val lowerBound: Double,
            val upperBound: Double,
            override val history: SortedMap<LocalDate, Log>
        ) : TrackedValue()

        data class Log(val value: Double, val time: Time, val date: LocalDate)
    }

    enum class Difficulty {
        EASY, NORMAL, HARD, HELL
    }

    val nextEndTime: Time?
        get() = nextStartTime?.plus(nextDuration!!)

    val motivation1: String
        get() = if (motivations.isNotEmpty()) motivations[0] else ""

    val motivation2: String
        get() = if (motivations.size > 1) motivations[1] else ""

    val motivation3: String
        get() = if (motivations.size > 2) motivations[2] else ""

    val isCompleted: Boolean
        get() = completedAtDate != null
}

enum class SharingPreference {
    PRIVATE, FRIENDS
}