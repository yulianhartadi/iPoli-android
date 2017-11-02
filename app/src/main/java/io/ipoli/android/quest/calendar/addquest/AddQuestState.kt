package io.ipoli.android.quest.calendar.addquest

import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.quest.calendar.CalendarIntent
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 11/2/17.
 */
sealed class AddQuestIntent : Intent

object PickDateIntent : AddQuestIntent()

data class AddQuestViewState(
    val name: String
) : ViewState