package io.ipoli.android.quest.calendar

import android.view.*
import com.bluelinelabs.conductor.Controller
import io.ipoli.android.R


/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */
class CalendarController : Controller() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        parentController?.view?.startActionMode(object : ActionMode.Callback {
            override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {

                return true
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                mode.menuInflater.inflate(R.menu.calendar_quest_edit_menu, menu)
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                return false
            }

            override fun onDestroyActionMode(p0: ActionMode?) {

            }
        })
        return inflater.inflate(R.layout.controller_calendar, container, false)

    }
}