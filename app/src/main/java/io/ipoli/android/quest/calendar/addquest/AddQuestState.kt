package io.ipoli.android.quest.calendar.addquest

import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 11/2/17.
 */
sealed class AddQuestIntent : Intent

object PickDateIntent : AddQuestIntent()
data class DatePickedIntent(val year: Int, val month: Int, val day: Int) : AddQuestIntent()

data class AddQuestViewState(
    val type: StateType,
    val name: String,
    val date: LocalDate? = null
) : ViewState

enum class StateType {
    LOADING, SHOW_DATE_PICKER, DEFAULT
}