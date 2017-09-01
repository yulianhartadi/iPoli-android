package io.ipoli.android.quest.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import io.ipoli.android.R

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */
class CalendarController : Controller() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_calendar, container, false)
    }

}