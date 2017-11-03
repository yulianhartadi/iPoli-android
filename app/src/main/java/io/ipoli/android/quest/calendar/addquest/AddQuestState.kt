package io.ipoli.android.quest.calendar.addquest

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.view.AndroidColor
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 11/2/17.
 */
sealed class AddQuestIntent : Intent

object PickDateIntent : AddQuestIntent()
object PickTimeIntent : AddQuestIntent()
object PickColorIntent : AddQuestIntent()
data class DatePickedIntent(val year: Int, val month: Int, val day: Int) : AddQuestIntent()
data class TimePickedIntent(val hour: Int, val minute: Int) : AddQuestIntent()
data class ColorPickedIntent(val color: AndroidColor) : AddQuestIntent()

data class AddQuestViewState(
    val type: StateType,
    val name: String,
    val date: LocalDate? = null,
    val time: Time? = null,
    val color: AndroidColor? = null
) : ViewState

enum class StateType {
    DEFAULT,
    PICK_DATE,
    PICK_TIME,
    PICK_COLOR
}