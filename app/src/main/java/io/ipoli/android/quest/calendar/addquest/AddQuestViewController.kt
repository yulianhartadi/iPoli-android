package io.ipoli.android.quest.calendar.addquest

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.Toast
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import io.ipoli.android.R
import io.ipoli.android.R.id.questName
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.mvi.MviViewController
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.common.view.ColorPickerDialogController
import io.ipoli.android.common.view.DurationPickerDialogController
import io.ipoli.android.common.view.color
import io.ipoli.android.iPoliApp
import io.ipoli.android.quest.calendar.EditTextBackEvent
import io.ipoli.android.quest.calendar.EditTextImeBackListener
import io.ipoli.android.reminder.view.picker.ReminderPickerDialogController
import io.ipoli.android.reminder.view.picker.ReminderViewModel
import kotlinx.android.synthetic.main.controller_add_quest.view.*
import kotlinx.android.synthetic.main.dialog_reminder_picker.view.*
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

        view.questName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                send(SaveQuestIntent(view.questName.text.toString()))
            }
            true
        }

        view.scheduleDate.setOnClickListener {
            send(PickDateIntent)
        }

        view.startTime.setOnClickListener {
            send(PickTimeIntent)
        }

        view.duration.setOnClickListener {
            send(PickDurationIntent)
        }

        view.color.setOnClickListener {
            send(PickColorIntent)
        }

        view.reminder.setOnClickListener {
            send(PickReminderIntent)
        }

        view.done.setOnClickListener {
            send(SaveQuestIntent(view.questName.text.toString()))
        }

        return view
    }

    override fun render(state: AddQuestViewState, view: View) {
        colorSelectedIcons(state, view)

        when {
            state.type == StateType.PICK_DATE -> {
                val date = state.date ?: LocalDate.now()
                DatePickerDialog(view.context, R.style.Theme_iPoli_AlertDialog,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        send(DatePickedIntent(year, month + 1, dayOfMonth))
                    }, date.year, date.month.value - 1, date.dayOfMonth).show()
            }

            state.type == StateType.PICK_TIME -> {
                val startTime = state.time ?: Time.now()
                TimePickerDialog(view.context,
                    TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                        send(TimePickedIntent(hour, minute))
                    }, startTime.hours, startTime.getMinutes(), false).show()
            }

            state.type == StateType.PICK_DURATION ->
                DurationPickerDialogController(object : DurationPickerDialogController.DurationPickedListener {
                    override fun onDurationPicked(minutes: Int) {
                        send(DurationPickedIntent(minutes))
                    }

                }, state.duration).showDialog(router, "pick_duration_tag")

            state.type == StateType.PICK_COLOR ->
                ColorPickerDialogController(object : ColorPickerDialogController.ColorPickedListener {
                    override fun onColorPicked(color: AndroidColor) {
                        send(ColorPickedIntent(color))
                    }

                }, state.color).showDialog(router, "pick_color_tag")

            state.type == StateType.PICK_REMINDER ->
                ReminderPickerDialogController(object : ReminderPickerDialogController.ReminderPickedListener {
                    override fun onReminderPicked(reminder: ReminderViewModel?) {
                        send(ReminderPickedIntent(reminder))
                    }
                }, state.reminder).showDialog(router, "pick_reminder_tag")

            state.type == StateType.VALIDATION_ERROR_EMPTY_NAME ->
                view.questName.error = "Think of a name"

            state.type == StateType.VALIDATION_ERROR_EMPTY_START_TIME -> {
                Toast.makeText(view.context, "When will it start?", Toast.LENGTH_SHORT).show()
                playStartTimeErrorAnimation(view.startTime)
            }

            state.type == StateType.QUEST_SAVED -> {
                resetForm(view)
            }
        }
    }

    private fun playStartTimeErrorAnimation(view: View) {
        val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1.4f, 1f)
        val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1.4f, 1f)
        val shakeAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 50f, 0f, -50f, 0f)
        shakeAnimator.repeatCount = 4

        val set = AnimatorSet()
        set.playTogether(scaleXAnimator, scaleYAnimator, shakeAnimator)
        set.duration = 200
        set.start()
    }

    private fun resetForm(view: View) {
        view.questName.setText("")
        view.scheduleDate.drawable.setTintList(null)
        view.startTime.drawable.setTintList(null)
        view.duration.drawable.setTintList(null)
        view.color.drawable.setTintList(null)
        view.reminder.drawable.setTintList(null)
    }

    private fun colorSelectedIcons(state: AddQuestViewState, view: View) {
        state.date?.let {
            colorSelectedIcon(view.scheduleDate)
        }

        state.time?.let {
            colorSelectedIcon(view.startTime)
        }

        state.duration?.let {
            colorSelectedIcon(view.duration)
        }

        state.color?.let {
            colorSelectedIcon(view.color)
        }

        state.reminder?.let {
            colorSelectedIcon(view.reminder)
        }
    }

    private fun colorSelectedIcon(view: ImageView) {
        view.drawable.setTint(color(R.color.colorAccentAlternative))
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