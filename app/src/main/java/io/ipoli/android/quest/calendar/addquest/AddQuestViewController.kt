package io.ipoli.android.quest.calendar.addquest

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
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
import org.threeten.bp.LocalDate
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

    override fun createPresenter() = presenter

    override fun onContextAvailable(context: Context) {
        inject(iPoliApp.controllerModule(context, router))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_add_quest, container, false)

        view.questName.setOnEditTextImeBackListener(object : EditTextImeBackListener {
            override fun onImeBack(ctrl: EditTextBackEvent, text: String) {
                close()
            }
        })

        view.scheduleDate.setOnClickListener {
            send(PickDateIntent)
        }

        return view
    }

    override fun render(state: AddQuestViewState, view: View) {
        state.date?.let {
            view.scheduleDate.drawable.setTint(ContextCompat.getColor(view.context, R.color.colorAccentAlternative))
        }

        if (state.type == StateType.SHOW_DATE_PICKER) {
            val date = if (state.date != null) state.date else LocalDate.now()
            val dialog = DatePickerDialog(view.context, R.style.Theme_iPoli_AlertDialog,
                DatePickerDialog.OnDateSetListener { v, year, month, dayOfMonth ->
                    send(DatePickedIntent(year, month - 1, dayOfMonth))
                }, date.year, date.month.value - 1, date.dayOfMonth)
            dialog.show()
        }
    }

    override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnded(changeHandler, changeType)
        view!!.questName.requestFocus()
        ViewUtils.showKeyboard(view!!.questName.context, view!!.questName)
    }

    private fun close() {
        router.popController(this)
    }

}