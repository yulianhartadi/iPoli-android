package io.ipoli.android.tag

import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Entity
import io.ipoli.android.quest.Icon
import org.threeten.bp.Instant

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/03/2018.
 */
data class Tag(
    override val id: String = "",
    val name: String,
    val color: Color,
    val icon: Icon? = null,
    val isFavorite: Boolean,
    val questCount: Int = -1,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : Entity