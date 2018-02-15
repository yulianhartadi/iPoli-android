package mypoli.android.repeatingquest.entity

import mypoli.android.common.datetime.Time
import mypoli.android.quest.*
import org.threeten.bp.Instant

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/14/2018.
 */

sealed class RepeatingPattern {
    object Daily : RepeatingPattern()
    data class Yearly(val dayOfMonth: Int, val month: Int) : RepeatingPattern()
}

data class RepeatingQuest(
    override val id: String = "",
    val name: String,
    val color: Color,
    val icon: Icon? = null,
    val category: Category,
    val startTime: Time? = null,
    val duration: Int,
    val reminder: Reminder? = null,
    val repeatingPattern: RepeatingPattern,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : Entity