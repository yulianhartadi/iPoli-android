package mypoli.android.challenge.entity

import mypoli.android.common.datetime.Time
import mypoli.android.quest.Color
import mypoli.android.quest.Entity
import mypoli.android.quest.Icon
import mypoli.android.quest.Quest
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

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
    val end: LocalDate,
    val experience: Int? = null,
    val coins: Int? = null,
    val bounty: Quest.Bounty? = null,
    val completedAtDate: LocalDate? = null,
    val completedAtTime: Time? = null,
    val completedAt: Instant? = null,
    override val createdAt: Instant,
    override val updatedAt: Instant
) : Entity {

    enum class Difficulty {
        EASY, MEDIUM, HARD
    }
}