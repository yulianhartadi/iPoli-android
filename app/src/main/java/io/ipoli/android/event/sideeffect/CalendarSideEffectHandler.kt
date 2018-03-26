package io.ipoli.android.event.sideeffect

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.event.calendar.picker.CalendarPickerAction
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