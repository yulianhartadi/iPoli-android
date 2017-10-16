package io.ipoli.android.quest

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.view.Color
import io.ipoli.android.quest.data.Category
import org.threeten.bp.LocalDateTime

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/15/17.
 */
data class Quest(
    val id: String,
    val name: String,
    val color: Color,
    val category: Category,
    val startTime: Time?,
    val duration: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val removedAt: LocalDateTime?
)