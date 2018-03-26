package io.ipoli.android.quest.schedule.addquest

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.support.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.controller_add_quest.view.*
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.quest.reminder.picker.ReminderPickerDialogController
import io.ipoli.android.quest.reminder.picker.ReminderViewModel
import io.ipoli.android.quest.schedule.addquest.StateType.*
import io.ipoli.android.repeatingquest.edit.picker.RepeatingPatternPickerDialogController
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 11/2/17.
 */
class AddQuestViewController(args: Bundle? = null) :
    ReduxViewController<AddQuestAction, AddQuestViewState, AddQuestReducer>(
        args
    ) {
    override val reducer = AddQuestReducer

    private var closeListener: () -> Unit = {}

    private lateinit var currentDate: LocalDate

    constructor(closeListener: () -> Unit, currentDate: LocalDate) : this() {
        this.closeListener = closeListener
        this.currentDate = currentDate
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.controller_add_quest, container, false)

        view.questName.setOnEditTextImeBackListener(object : EditTextImeBackListener {
            override fun onImeBack(ctrl: EditTextBackEvent, text: String) {
                resetForm(view)
                closeListener()
                view.questName.setOnEditTextImeBackListener(null)
            }
        })

        view.questName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dispatch(AddQuestAction.Save(view.questName.text.toString()))
            }
            true
        }

        view.duration.setImageDrawable(
            IconicsDrawable(view.context)
                .icon(GoogleMaterial.Icon.gmd_hourglass_empty)
                .colorRes(R.color.md_white)
                .sizeDp(22)
        )

        view.scheduleDate.dispatchOnClickAndExec(AddQuestAction.PickDate, {
            selectScheduleDate(view)
        })
        view.repeatingPattern.dispatchOnClickAndExec(AddQuestAction.PickRepeatingPattern, {
            selectRepeatingPattern(view)
        })

        view.startTime.dispatchOnClick(AddQuestAction.PickTime)
        view.duration.dispatchOnClick(AddQuestAction.PickDuration)
        view.color.dispatchOnClick(AddQuestAction.PickColor)
        view.icon.dispatchOnClick(AddQuestAction.PickIcon)
        view.reminder.dispatchOnClick(AddQuestAction.PickReminder)
        view.done.setOnClickListener {  dispatch(AddQuestAction.Save(view.questName.text.toString()))}

        return view
    }

    override fun onCreateLoadAction() = AddQuestAction.Load(currentDate)

    private fun selectRepeatingPattern(view: View) {
        TransitionManager.beginDelayedTransition(view as ViewGroup)
        view.scheduleDate.setBackgroundResource(R.drawable.quest_type_left_bordered_background)
        view.repeatingPattern.setBackgroundResource(R.drawable.quest_type_right_solid_background)
    }

    private fun selectScheduleDate(view: View) {
        TransitionManager.beginDelayedTransition(view as ViewGroup)
        view.scheduleDate.setBackgroundResource(R.drawable.quest_type_left_solid_background)
        view.repeatingPattern.setBackgroundResource(R.drawable.quest_type_right_bordered_background)
    }

    override fun render(state: AddQuestViewState, view: View) {
        colorSelectedIcons(state, view)

        when (state.type) {
            PICK_DATE -> {
                val date = state.date ?: LocalDate.now()
                val datePickerDialog = DatePickerDialog(
                    view.context, R.style.Theme_myPoli_AlertDialog,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        dispatch(AddQuestAction.DatePicked(LocalDate.of(year, month + 1, dayOfMonth)))
                    }, date.year, date.month.value - 1, date.dayOfMonth
                )
                datePickerDialog.setOnCancelListener {
                    dispatch(AddQuestAction.DatePickerCanceled)
                }
                datePickerDialog.show()
            }

            PICK_TIME -> {
                val startTime = state.time ?: Time.now()
                val dialog = TimePickerDialog(view.context,
                    TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                        dispatch(AddQuestAction.TimePicked(Time.at(hour, minute)))
                    }, startTime.hours, startTime.getMinutes(), false
                )
                dialog.setButton(
                    Dialog.BUTTON_NEUTRAL,
                    view.context.getString(R.string.do_not_know),
                    { _, _ ->
                        dispatch(AddQuestAction.TimePicked(null))
                    })
                dialog.show()
            }

            PICK_DURATION ->
                DurationPickerDialogController(object :
                    DurationPickerDialogController.DurationPickedListener {
                    override fun onDurationPicked(minutes: Int) {
                        dispatch(AddQuestAction.DurationPicked(minutes))
                    }

                }, state.duration).showDialog(router, "pick_duration_tag")

            PICK_COLOR ->
                ColorPickerDialogController({
                    dispatch(AddQuestAction.ColorPicked(it.color))
                }, state.color?.androidColor).showDialog(
                    router,
                    "pick_color_tag"
                )

            PICK_ICON ->
                IconPickerDialogController({ icon ->
                    dispatch(AddQuestAction.IconPicked(icon))
                }, state.icon?.androidIcon).showDialog(
                    router,
                    "pick_icon_tag"
                )

            PICK_REMINDER ->
                ReminderPickerDialogController(object :
                    ReminderPickerDialogController.ReminderPickedListener {
                    override fun onReminderPicked(reminder: ReminderViewModel?) {
                        dispatch(AddQuestAction.ReminderPicked(reminder))
                    }
                }, state.reminder).showDialog(router, "pick_reminder_tag")

            PICK_REPEATING_PATTERN -> {
                RepeatingPatternPickerDialogController(
                    state.repeatingPattern,
                    { dispatch(AddQuestAction.RepeatingPatternPicked(it)) },
                    { dispatch(AddQuestAction.RepeatingPatterPickerCanceled) }
                )
                .show(router, "pick_repeating_pattern_tag")
            }

            SWITCHED_TO_QUEST -> {
                selectScheduleDate(view)
            }

            SWITCHED_TO_REPEATING -> {
                selectRepeatingPattern(view)
            }

            VALIDATION_ERROR_EMPTY_NAME ->
                view.questName.error = "Think of a name"

            QUEST_SAVED -> {
                resetForm(view)
            }

            DATA_LOADED -> {
            }

        }
    }

    private fun resetForm(view: View) {
        view.questName.setText("")
        listOf<ImageView>(
            view.scheduleDate,
            view.startTime,
            view.duration,
            view.color,
            view.icon,
            view.reminder,
            view.repeatingPattern
        )
            .forEach { resetColor(it) }
        selectScheduleDate(view)
    }

    private fun colorSelectedIcons(state: AddQuestViewState, view: View) {
        state.duration?.let {
            applySelectedColor(view.duration)
        }

        state.color?.let {
            applySelectedColor(view.color)
        }

        resetOrColorIcon(state.time, view.startTime)
        resetOrColorIcon(state.icon, view.icon)
        resetOrColorIcon(state.reminder, view.reminder)
    }

    private fun resetOrColorIcon(stateData: Any?, iconView: ImageView) {
        if (stateData == null) {
            resetColor(iconView)
        } else {
            applySelectedColor(iconView)
        }
    }

    private fun resetColor(view: ImageView) {
        view.drawable.alpha = Constants.NO_TRANSPARENCY_ALPHA
    }

    private fun applySelectedColor(view: ImageView) {
        view.drawable.alpha = Constants.MEDIUM_ALPHA
    }

    override fun onDetach(view: View) {
        view.questName.setOnEditTextImeBackListener(null)
        super.onDetach(view)
        router.popController(this)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        view.postDelayed({
            view.questName.requestFocus()
            ViewUtils.showKeyboard(view.questName.context, view.questName)
        }, shortAnimTime)
    }
}