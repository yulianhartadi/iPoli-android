package mypoli.android.repeatingquest.entity

import mypoli.android.quest.Entity
import org.threeten.bp.Instant

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/14/2018.
 */

sealed class RepeatingPattern {
    object Daily : RepeatingPattern()
}

data class RepeatingQuest(
    override val id: String = "",
    val name: String,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : Entity