package io.ipoli.android.quest.subquest

import io.ipoli.android.common.datetime.Time
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/29/2018.
 */
data class SubQuest(
    val name: String,
    val completedAtDate: LocalDate?,
    val completedAtTime: Time?
)