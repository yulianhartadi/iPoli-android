package io.ipoli.android.event

import io.ipoli.android.quest.Entity
import org.threeten.bp.Instant

data class Calendar(
    override val id: String = "",
    val name: String,
    val color: Int,
    val isVisible: Boolean,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : Entity