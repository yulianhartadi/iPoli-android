package mypoli.android.timer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.controller_timer.view.*
import kotlinx.android.synthetic.main.item_timer_progress.view.*
import mypoli.android.R
import mypoli.android.common.mvi.MviViewController
import mypoli.android.common.view.attr
import mypoli.android.common.view.enterFullScreen
import mypoli.android.common.view.exitFullScreen
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 6.01.18.
 */
class TimerViewController : MviViewController<TimerViewState, TimerViewController, TimerPresenter, TimerIntent> {

    private lateinit var questId: String

    private lateinit var handler: Handler

    private val presenter by required { timerPresenter }

    constructor(args: Bundle? = null) : super(args)

    constructor(questId: String) : super() {
        this.questId = questId
    }

    override fun createPresenter() = presenter

    private lateinit var updateTimer: () -> Unit

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_timer, container, false)

        handler = Handler(Looper.getMainLooper())
        updateTimer = {
            view.timerProgress.progress = view.timerProgress.progress + 1
            handler.postDelayed(updateTimer, 1000)
        }

        val icon = IconicsDrawable(view.context)
            .icon(Ionicons.Icon.ion_play)
            .color(attr(R.attr.colorAccent))
            .sizeDp(22)

        view.startStop.setImageDrawable(icon)
        handler.postDelayed(updateTimer, 1000)

        view.questName.text = "Do the Laundry"

        for (i in 0..3) {
            view.timerProgressContainer.addView(createProgress(inflater, view, R.drawable.timer_progress_item_complete))
        }
        for (i in 0..5) {
            view.timerProgressContainer.addView(createProgress(inflater, view, R.drawable.timer_progress_item_incomplete))
        }


//        view.timerProgressContainer.addView(createProgress(inflater, view))
//        view.timerProgressContainer.addView(createProgress(inflater, view))

        return view
    }

    private fun createProgress(inflater: LayoutInflater, view: View, progressDrawable: Int): View {
        val progress = inflater.inflate(R.layout.item_timer_progress, view.timerProgressContainer, false)
        progress.timerItemProgress.setBackgroundResource(progressDrawable)
        return progress
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        enterFullScreen()
    }

    override fun onDetach(view: View) {
        exitFullScreen()
        super.onDetach(view)
    }

    override fun render(state: TimerViewState, view: View) {

    }
}