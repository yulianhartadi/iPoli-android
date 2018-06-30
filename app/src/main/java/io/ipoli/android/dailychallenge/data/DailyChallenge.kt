package io.ipoli.android.dailychallenge.data

import io.ipoli.android.quest.Entity
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/28/18.
 */
data class DailyChallenge(
    override val id: String = "",
    val date: LocalDate,
    val questIds: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now(),
    val removedAt: Instant? = null
) : Entity