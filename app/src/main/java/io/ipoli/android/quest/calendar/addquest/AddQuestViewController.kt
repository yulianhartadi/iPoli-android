package io.ipoli.android.quest.calendar.addquest

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import io.ipoli.android.R
import io.ipoli.android.R.id.*
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.mvi.MviViewController
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.view.RevealAnimator
import io.ipoli.android.iPoliApp
import io.ipoli.android.quest.calendar.CalendarIntent
import io.ipoli.android.quest.calendar.CalendarPresenter
import io.ipoli.android.quest.calendar.CalendarViewController
import io.ipoli.android.quest.calendar.CalendarViewState
import kotlinx.android.synthetic.main.controller_add_quest.view.*
import kotlinx.android.synthetic.main.controller_calendar.view.*
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 11/2/17.
 */
class AddQuestViewController(args: Bundle? = null) :
    MviViewController<AddQuestViewState, AddQuestViewController, AddQuestPresenter, AddQuestIntent>(args),
    Injects<ControllerModule>,
    ViewStateRenderer<AddQuestViewState> {

    private val presenter by required { addQuestPresenter }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_add_quest, container, false)

        return view
    }

    override fun createPresenter() = presenter

    override fun render(state: AddQuestViewState, view: View) {

    }

    override fun onAttach(view: View) {
        super.onAttach(view)

        view.post {
            val questName = view.questName
            ViewUtils.showKeyboard(questName.context, questName)
            questName.requestFocus()
        }
    }

    override fun onContextAvailable(context: Context) {
        inject(iPoliApp.controllerModule(context, router))
    }

}