package io.ipoli.android.dailychallenge.data

import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.quest.Entity
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/28/18.
 */
data class DailyChallenge(
    override val id: String = "",
    val questIds: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : Entity {
    val date: LocalDate
        get() = id.toLong().startOfDayUTC
}