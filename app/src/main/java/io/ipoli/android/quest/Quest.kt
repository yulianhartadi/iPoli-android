package io.ipoli.android.quest

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.toMillis
import io.ipoli.android.player.ExperienceForLevelGenerator
import io.ipoli.android.store.avatars.data.Avatar
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/15/17.
 */

interface Entity {
    val id: String
}

data class Reminder(
    val message: String,
    val remindTime: Time,
    val remindDate: LocalDate
) {
    fun toMillis() =
        LocalDateTime.of(remindDate, LocalTime.of(remindTime.hours, remindTime.getMinutes())).toMillis()

}

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

enum class Icon {
    HOME,
    CAMERA
}

data class Quest(
    override val id: String = "",
    val name: String,
    val color: Color,
    val category: Category,
    val startTime: Time? = null,
    val scheduledDate: LocalDate,
    val duration: Int,
    val reminder: Reminder? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAtDate: LocalDate? = null,
    val completedAtTime: Time? = null,
    val experience: Int? = null
) : Entity {
    val isCompleted = completedAtDate != null
    val endTime: Time?
        get() = startTime?.let { Time.of(it.toMinuteOfDay() + duration) }
    val isScheduled = startTime != null
}

data class Player(
    override val id: String = "",
    val level: Int = 1,
    val coins: Int = 0,
    val experience: Long = 0,
    val authProvider: AuthProvider,
    val avatar: Avatar = Avatar.IPOLI_CLASSIC,
    val createdAt: LocalDateTime = LocalDateTime.now()
) : Entity {
    fun addExperience(experience: Int): Player {
        val newXp = experience + this.experience
        return copy(
            experience = newXp,
            level = nextLevel(newXp)
        )
    }

    private fun nextLevel(newXp: Long): Int {
        var newLevel = level
        while (newXp >= ExperienceForLevelGenerator.forLevel(newLevel + 1)) {
            newLevel++
        }
        return newLevel
    }

    fun removeExperience(experience: Int): Player {
        val newXp = Math.max(this.experience - experience, 0)
        return copy(
            experience = newXp,
            level = prevLevel(newXp)
        )
    }

    private fun prevLevel(newXp: Long): Int {
        var newLevel = level
        while (newLevel - 1 > 0 && newXp < ExperienceForLevelGenerator.forLevel(newLevel)) {
            newLevel--
        }
        return newLevel
    }
}

data class AuthProvider(
    val id: String = "",
    val provider: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val email: String = "",
    val image: String = ""
)