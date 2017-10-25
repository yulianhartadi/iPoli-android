package io.ipoli.android.quest

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.store.avatars.data.Avatar
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/15/17.
 */

interface Entity {
    val id: String
}

data class Reminder(
    val id: String,
    val message: String,
    val remindTime: Time,
    val remindDate: LocalDate
)

data class Category(
    val name: String,
    val color: Color
)

enum class Color {
    RED,
    GREEN,
    BLUE,
    PURPLE,
    BROWN,
    ORANGE,
    PINK,
    TEAL,
    DEEP_ORANGE,
    INDIGO,
    BLUE_GREY,
    LIME
}

data class QuestSchedule(val date: LocalDate? = null, val time: Time? = null, val duration: Int)

data class Quest(
    override val id: String = "",
    val name: String,
    val color: Color,
    val category: Category,
    val plannedSchedule: QuestSchedule,
    val actualSchedule: QuestSchedule? = null,
    val originalStartTime: Time? = plannedSchedule.time,
    val reminder: Reminder? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAtDate: LocalDate? = null
) : Entity {
    val isScheduled = plannedSchedule.date != null && plannedSchedule.time != null
    val isCompleted = completedAtDate != null
    val endTime: Time?
        get() {
            if (actualSchedule != null) {
                return Time.of(actualSchedule.time!!.toMinuteOfDay() + actualSchedule.duration)
            }

            if (plannedSchedule.time != null) {
                return Time.of(plannedSchedule.time.toMinuteOfDay() + plannedSchedule.duration)
            }

            return null
        }
}

data class Player(
    override val id: String = "",
    var coins: Int = 0,
    var experience: Int = 0,
    var authProvider: AuthProvider,
    var avatar: Avatar = Avatar.IPOLI_CLASSIC,
    val createdAt: LocalDateTime = LocalDateTime.now()
) : Entity

data class AuthProvider(
    var id: String = "",
    var provider: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var username: String = "",
    var email: String = "",
    var image: String = ""
)