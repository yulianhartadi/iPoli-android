package mypoli.android.quest.schedule.addquest

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
import mypoli.android.Constants
import mypoli.android.R
import mypoli.android.common.ViewUtils
import mypoli.android.common.datetime.Time
import mypoli.android.common.mvi.MviViewController
import mypoli.android.common.view.*
import mypoli.android.quest.Color
import mypoli.android.quest.Icon
import mypoli.android.quest.schedule.addquest.StateType.*
import mypoli.android.reminder.view.picker.ReminderPickerDialogController
import mypoli.android.reminder.view.picker.ReminderViewModel
import mypoli.android.repeatingquest.picker.RepeatingPatternPickerDialogController
import org.threeten.bp.LocalDate
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 11/2/17.
 */
class AddQuestViewController(args: Bundle? = null) :
    MviViewController<AddQuestViewState, AddQuestViewController, AddQuestPresenter, AddQuestIntent>(
        args
    ) {

    private val presenter by required { addQuestPresenter }

    private var closeListener: () -> Unit = {}

    private lateinit var currentDate: LocalDate

    constructor(closeListener: () -> Unit, currentDate: LocalDate) : this() {
        this.closeListener = closeListener
        this.currentDate = currentDate
    }

    override fun createPresenter() = presenter

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
                send(AddQuestIntent.SaveQuest(view.questName.text.toString()))
            }
            true
        }

        view.duration.setImageDrawable(
            IconicsDrawable(view.context)
                .icon(GoogleMaterial.Icon.gmd_hourglass_empty)
                .colorRes(R.color.md_white)
                .sizeDp(22)
        )

        view.scheduleDate.sendOnClickAndExec(AddQuestIntent.PickDate, {
            selectScheduleDate(view)
        })

        view.startTime.setOnClickListener {
            send(AddQuestIntent.PickTime)
        }

        view.duration.setOnClickListener {
            send(AddQuestIntent.PickDuration)
        }

        view.color.setOnClickListener {
            send(AddQuestIntent.PickColor)
        }

        view.icon.setOnClickListener {
            send(AddQuestIntent.PickIcon)
        }

        view.reminder.setOnClickListener {
            send(AddQuestIntent.PickReminder)
        }

        view.done.setOnClickListener {
            send(AddQuestIntent.SaveQuest(view.questName.text.toString()))
        }

        view.repeatingPattern.sendOnClickAndExec(AddQuestIntent.PickRepeatingPattern, {
            selectRepeatingPattern(view)
        })
        return view
    }

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
                        send(AddQuestIntent.DatePicked(year, month + 1, dayOfMonth))
                    }, date.year, date.month.value - 1, date.dayOfMonth
                )
                datePickerDialog.setOnCancelListener {
                    send(AddQuestIntent.DatePickerCanceled)
                }
                datePickerDialog.show()
            }

            PICK_TIME -> {
                val startTime = state.time ?: Time.now()
                val dialog = TimePickerDialog(view.context,
                    TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                        send(AddQuestIntent.TimePicked(Time.at(hour, minute)))
                    }, startTime.hours, startTime.getMinutes(), false
                )
                dialog.setButton(
                    Dialog.BUTTON_NEUTRAL,
                    view.context.getString(R.string.do_not_know),
                    { _, _ ->
                        send(AddQuestIntent.TimePicked(null))
                    })
                dialog.show()
            }

            PICK_DURATION ->
                DurationPickerDialogController(object :
                    DurationPickerDialogController.DurationPickedListener {
                    override fun onDurationPicked(minutes: Int) {
                        send(AddQuestIntent.DurationPicked(minutes))
                    }

                }, state.duration).showDialog(router, "pick_duration_tag")

            PICK_COLOR ->
                ColorPickerDialogController(object :
                    ColorPickerDialogController.ColorPickedListener {
                    override fun onColorPicked(color: AndroidColor) {
                        send(AddQuestIntent.ColorPicked(Color.valueOf(color.name)))
                    }

                }, state.color?.let { AndroidColor.valueOf(it.name) }).showDialog(
                    router,
                    "pick_color_tag"
                )

            PICK_ICON ->
                IconPickerDialogController({ icon ->
                    send(AddQuestIntent.IconPicked(icon?.let { Icon.valueOf(it.name) }))
                }, state.icon?.let { AndroidIcon.valueOf(it.name) }).showDialog(
                    router,
                    "pick_icon_tag"
                )

            PICK_REMINDER ->
                ReminderPickerDialogController(object :
                    ReminderPickerDialogController.ReminderPickedListener {
                    override fun onReminderPicked(reminder: ReminderViewModel?) {
                        send(AddQuestIntent.ReminderPicked(reminder))
                    }
                }, state.reminder).showDialog(router, "pick_reminder_tag")

            PICK_REPEATING_PATTERN -> {
                RepeatingPatternPickerDialogController(
                    state.repeatingPattern,
                    { send(AddQuestIntent.RepeatingPatternPicked(it)) },
                    { send(AddQuestIntent.RepeatingPatterPickerCanceled) }
                )
                .showDialog(router, "pick_repeating_pattern_tag")
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

            DEFAULT -> {
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
        send(AddQuestIntent.LoadData(currentDate))
    }
}