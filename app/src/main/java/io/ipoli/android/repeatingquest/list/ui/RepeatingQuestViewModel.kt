package io.ipoli.android.repeatingquest.list.ui

import android.support.annotation.DrawableRes
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.repeatingquest.data.Recurrence
import org.threeten.bp.LocalDate

data class RepeatingQuestViewModel(val name: String,
                                   @DrawableRes val categoryImage: Int,
                                   val categoryColor: Int,
                                   val nextScheduledDate: LocalDate?,
                                   val duration: Int,
                                   val startTime: Time?,
                                   val scheduledCount: Int,
                                   val completedCount: Int,
                                   val remainingCount: Int,
                                   val repeatType: Recurrence.RepeatType)