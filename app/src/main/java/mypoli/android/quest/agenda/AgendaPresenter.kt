package mypoli.android.quest.agenda

import android.content.Context
import mypoli.android.common.AppState
import mypoli.android.common.redux.android.AndroidStatePresenter
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/26/2018.
 */
class AgendaPresenter : AndroidStatePresenter<AppState, AgendaViewState> {
    override fun present(state: AppState, context: Context): AgendaViewState {
        return AgendaViewState(LocalDate.now())
    }

}