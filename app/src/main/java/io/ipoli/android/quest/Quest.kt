package io.ipoli.android.quest

import io.ipoli.android.common.datetime.*
import io.ipoli.android.common.sumByLong
import io.ipoli.android.quest.reminder.picker.ReminderViewModel
import io.ipoli.android.quest.subquest.SubQuest
import io.ipoli.android.repeatingquest.entity.PeriodProgress
import io.ipoli.android.repeatingquest.entity.RepeatPattern
import io.ipoli.android.tag.Tag
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/15/17.
 */

interface Entity {
    val id: String
    val createdAt: Instant
    val updatedAt: Instant
}

data class Reminder(
    val message: String,
    val remindTime: Time,
    val remindDate: LocalDate?
) {
    fun toMillis() =
        LocalDateTime.of(
            remindDate,
            LocalTime.of(remindTime.hours, remindTime.getMinutes())
        ).toMillis()

    companion object {
        fun create(
            reminder: ReminderViewModel?,
            scheduledDate: LocalDate,
            startTime: Time
        ): Reminder? {
            return reminder?.let {
                val questDateTime =
                    LocalDateTime.of(
                        scheduledDate,
                        LocalTime.of(startTime.hours, startTime.getMinutes())
                    )
                val reminderDateTime = questDateTime.minusMinutes(it.minutesFromStart)
                val toLocalTime = reminderDateTime.toLocalTime()
                Reminder(
                    it.message,
                    Time.at(toLocalTime.hour, toLocalTime.minute),
                    reminderDateTime.toLocalDate()
                )
            }
        }
    }
}

fun Reminder.toMinutesFromStart(startDate: LocalDate, startTime: Time): Long {
    val daysDiff = ChronoUnit.DAYS.between(startDate, remindDate)
    val minutesDiff = startTime.toMinuteOfDay() - remindTime.toMinuteOfDay()
    return minutesDiff + Time.MINUTES_IN_A_DAY * daysDiff
}

fun Reminder.toMinutesFromStart(startTime: Time) =
    startTime.toMinuteOfDay() - remindTime.toMinuteOfDay()

enum class ColorPack(val gemPrice: Int) {
    FREE(0),
    BASIC(4)
}

enum class Color(val pack: ColorPack) {
    GREEN(ColorPack.FREE),
    BLUE_GREY(ColorPack.FREE),
    BLUE(ColorPack.BASIC),
    RED(ColorPack.BASIC),
    INDIGO(ColorPack.FREE),
    ORANGE(ColorPack.FREE),
    PINK(ColorPack.BASIC),
    TEAL(ColorPack.BASIC),
    DEEP_ORANGE(ColorPack.BASIC),
    PURPLE(ColorPack.BASIC),
    BROWN(ColorPack.BASIC),
    LIME(ColorPack.BASIC)
}

enum class Icon(val pack: IconPack) {
    HOME(IconPack.FREE),
    FRIENDS(IconPack.BASIC),
    RESTAURANT(IconPack.BASIC),
    PAW(IconPack.BASIC),
    BRIEFCASE(IconPack.FREE),
    BOOK(IconPack.FREE),
    HEART(IconPack.BASIC),
    RUN(IconPack.FREE),
    MED_KIT(IconPack.BASIC),
    TREE(IconPack.BASIC),
    BEER(IconPack.BASIC),
    PLANE(IconPack.FREE),
    COMPASS(IconPack.BASIC),
    LIGHT_BULB(IconPack.BASIC),
    CAR(IconPack.BASIC),
    WRENCH(IconPack.BASIC),
    STAR(IconPack.FREE),
    FITNESS(IconPack.BASIC),
    COFFEE(IconPack.BASIC),
    BUS(IconPack.BASIC),
    ACADEMIC(IconPack.BASIC),
    CAKE(IconPack.BASIC),
    GAME_CONTROLLER(IconPack.BASIC),
    FLASK(IconPack.BASIC),
    SHOPPING_CART(IconPack.BASIC),
    BIKE(IconPack.BASIC),
    TRAIN(IconPack.FREE),
    PIZZA(IconPack.FREE),
    PHONE(IconPack.BASIC),
    CLOUD(IconPack.FREE),
    SUN(IconPack.BASIC),
    AMERICAN_FOOTBALL(IconPack.BASIC),
    TROPHY(IconPack.BASIC),
    FOOTBALL(IconPack.BASIC),
    MONEY(IconPack.BASIC),
    CAMERA(IconPack.BASIC)
}

enum class IconPack(val gemPrice: Int) {
    FREE(0),
    BASIC(6)
}

sealed class BaseQuest(open val id: String)

data class Quest(
    override val id: String = "",
    val name: String,
    val color: Color,
    val icon: Icon? = null,
    val tags: List<Tag> = listOf(),
    val startTime: Time? = null,
    val duration: Int,
    val priority: Priority = Priority.NOT_IMPORTANT_NOT_URGENT,
    val preferredStartTime: TimePreference = TimePreference.ANY,
    val reminders: List<Reminder> = listOf(),
    val subQuests: List<SubQuest> = listOf(),
    val startDate: LocalDate? = null,
    val dueDate: LocalDate? = null,
    val scheduledDate: LocalDate?,
    val originalScheduledDate: LocalDate? = scheduledDate,
    val timeRanges: List<TimeRange> = listOf(),
    val timeRangesToComplete: List<TimeRange> = listOf(),
    val totalPomodoros: Int? = null,
    val completedAtDate: LocalDate? = null,
    val completedAtTime: Time? = null,
    val experience: Int? = null,
    val coins: Int? = null,
    val bounty: Bounty? = null,
    val isRemoved: Boolean = false,
    val repeatingQuestId: String? = null,
    val challengeId: String? = null,
    val note: String = "",
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : BaseQuest(id), Entity {

    sealed class Bounty {
        object None : Bounty()
        data class Food(val food: io.ipoli.android.pet.Food) : Bounty()
    }

    val isCompleted = completedAtDate != null
    val endTime: Time?
        get() = startTime?.plus(duration)
    val isScheduled = startTime != null

    val actualStart: Instant?
        get() =
            if (hasTimer) timeRanges.first().start else null

    val actualStartTime: Time?
        get() {
            if (isCompleted) {
                val localCompletedAt = LocalDateTime.of(
                    completedAtDate,
                    LocalTime.of(completedAtTime!!.hours, completedAtTime.getMinutes())
                )
                val result = localCompletedAt.minusSeconds(actualDuration.longValue)
                return Time.at(result.hour, result.minute)
            }
            return startTime
        }

    val actualDuration: Duration<Second>
        get() {

            if (hasCountDownTimer) {
                val timeRange = timeRanges.first()
                return if (isCompleted) {
                    timeRange.actualDuration()
                } else {
                    (Instant.now() - timeRange.start!!).milliseconds.asSeconds
                }
            }

            if (hasPomodoroTimer) {
                return timeRanges.sumByLong { it.actualDuration().longValue }.seconds
            }
            return duration.minutes.asSeconds
        }

    val hasTimer: Boolean
        get() = hasCountDownTimer || hasPomodoroTimer

    val hasCountDownTimer: Boolean
        get() = timeRanges.firstOrNull()?.type == TimeRange.Type.COUNTDOWN

    val hasPomodoroTimer: Boolean
        get() = timeRanges.firstOrNull()?.type == TimeRange.Type.POMODORO_WORK

    val isStarted: Boolean
        get() = !isCompleted &&
            (hasCountDownTimer ||
                (hasPomodoroTimer && timeRanges.last().end == null))

    fun hasCompletedAllTimeRanges() = timeRanges.sumBy { it.duration } >= duration

    val isFromRepeatingQuest = repeatingQuestId != null

    val isFromChallenge = challengeId != null
}

data class TimeRange(
    val type: Type,
    val duration: Int,
    val start: Instant? = null,
    val end: Instant? = null
) {
    enum class Type {
        COUNTDOWN,
        POMODORO_WORK,
        POMODORO_SHORT_BREAK,
        POMODORO_LONG_BREAK
    }

    fun actualDuration(): Duration<Second> {
        if (start != null && end != null) {
            return (end - start).toEpochMilli().milliseconds.asSeconds
        }
        return duration.minutes.asSeconds
    }
}

enum class Priority {
    IMPORTANT_URGENT,
    IMPORTANT_NOT_URGENT,
    NOT_IMPORTANT_URGENT,
    NOT_IMPORTANT_NOT_URGENT
}

data class RepeatingQuest(
    override val id: String = "",
    val name: String,
    val color: Color,
    val icon: Icon? = null,
    val tags: List<Tag> = listOf(),
    val startTime: Time? = null,
    val duration: Int,
    val priority: Priority = Priority.NOT_IMPORTANT_NOT_URGENT,
    val preferredStartTime: TimePreference = TimePreference.ANY,
    val reminders: List<Reminder> = listOf(),
    val repeatPattern: RepeatPattern,
    val subQuests: List<SubQuest> = listOf(),
    val nextDate: LocalDate? = null,
    val periodProgress: PeriodProgress? = null,
    val challengeId: String? = null,
    val note: String = "",
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : BaseQuest(id), Entity {
    val start
        get() = repeatPattern.start

    val end
        get() = repeatPattern.end

    val isCompleted
        get() = if (end == null) false else LocalDate.now().isAfter(end)

    val endTime: Time?
        get() = startTime?.plus(duration)

    val isFlexible: Boolean
        get() = repeatPattern is RepeatPattern.Flexible

    val isFixed: Boolean
        get() = !isFlexible
}