package io.ipoli.android.quest.calendar.addquest

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.mvi.MviViewController
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.iPoliApp
import io.ipoli.android.quest.calendar.EditTextBackEvent
import io.ipoli.android.quest.calendar.EditTextImeBackListener
import kotlinx.android.synthetic.main.controller_add_quest.view.*
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

        view.questName.setOnEditTextImeBackListener(object : EditTextImeBackListener {
            override fun onImeBack(ctrl: EditTextBackEvent, text: String) {
                close()
            }
        })

//        view.questName.post {
//            view.questName.requestFocus()
//            ViewUtils.showKeyboard(view.questName.context, view.questName)
//        }

        return view
    }

    override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnded(changeHandler, changeType)
        view!!.questName.requestFocus()
        ViewUtils.showKeyboard(view!!.questName.context, view!!.questName)
    }

    private fun close() {
        router.popController(this)
    }

    override fun createPresenter() = presenter

    override fun render(state: AddQuestViewState, view: View) {
//        Timber.d("AAA render")
//        view.questName.requestFocus()
//        ViewUtils.showKeyboard(view.questName.context, view.questName)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        view.post {

        }
    }

    override fun onContextAvailable(context: Context) {
        inject(iPoliApp.controllerModule(context, router))
    }

}