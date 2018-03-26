package mypoli.android.event.sideeffect

import mypoli.android.common.AppSideEffectHandler
import mypoli.android.common.AppState
import mypoli.android.common.DataLoadedAction
import mypoli.android.common.redux.Action
import mypoli.android.event.calendar.picker.CalendarPickerAction
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/11/2018.
 */
class CalendarSideEffectHandler : AppSideEffectHandler() {

    private val calendarRepository by required { calendarRepository }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is CalendarPickerAction.Load ->
                dispatch(DataLoadedAction.CalendarsChanged(calendarRepository.findAll()))
        }
    }

    override fun canHandle(action: Action) = action === CalendarPickerAction.Load

}