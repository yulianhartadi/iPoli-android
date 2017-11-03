package io.ipoli.android.quest.calendar.addquest

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.mvi.MviViewController
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.common.view.ColorPickerDialogController
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

        view.startTime.setOnClickListener {
            send(PickTimeIntent)
        }

        view.color.setOnClickListener {
            send(PickColorIntent)
        }

        return view
    }

    override fun render(state: AddQuestViewState, view: View) {
        state.date?.let {
            view.scheduleDate.drawable.setTint(ContextCompat.getColor(view.context, R.color.colorAccentAlternative))
        }

        state.time?.let {
            view.startTime.drawable.setTint(ContextCompat.getColor(view.context, R.color.colorAccentAlternative))
        }

        state.color?.let {
            view.color.drawable.setTint(ContextCompat.getColor(view.context, R.color.colorAccentAlternative))
        }

        if (state.type == StateType.PICK_DATE) {
            val date = state.date ?: LocalDate.now()
            DatePickerDialog(view.context, R.style.Theme_iPoli_AlertDialog,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    send(DatePickedIntent(year, month + 1, dayOfMonth))
                }, date.year, date.month.value - 1, date.dayOfMonth).show()
        }

        if (state.type == StateType.PICK_TIME) {
            val startTime = state.time ?: Time.now()
            TimePickerDialog(view.context,
                TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                    send(TimePickedIntent(hour, minute))
                }, startTime.hours, startTime.getMinutes(), false).show()
        }

        if (state.type == StateType.PICK_COLOR) {
            ColorPickerDialogController(object : ColorPickerDialogController.ColorPickedListener {
                override fun onColorPicked(color: AndroidColor) {
                    send(ColorPickedIntent(color))
                }

            }, state.color).showDialog(router, "pick_color_tag")
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