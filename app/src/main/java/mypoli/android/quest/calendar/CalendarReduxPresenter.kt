package mypoli.android.quest.calendar

import android.content.Context
import mypoli.android.common.redux.AppState
import mypoli.android.common.redux.android.AndroidStatePresenter
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 01/22/2018.
 */
class CalendarReduxPresenter : AndroidStatePresenter<AppState, CalendarViewState> {

    override fun present(state: AppState, context: Context): CalendarViewState {
        return CalendarViewState(
            currentDate = LocalDate.now(),
            datePickerState = CalendarViewState.DatePickerState.INVISIBLE,
            adapterPosition = 0
        )
    }

}