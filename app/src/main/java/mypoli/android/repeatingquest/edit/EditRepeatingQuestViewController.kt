package mypoli.android.repeatingquest.edit

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import kotlinx.android.synthetic.main.controller_edit_repeating_quest.view.*
import mypoli.android.R
import mypoli.android.common.datetime.Time
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.text.DurationFormatter
import mypoli.android.common.view.*
import mypoli.android.reminder.view.picker.ReminderPickerDialogController
import mypoli.android.reminder.view.picker.ReminderViewModel
import mypoli.android.repeatingquest.edit.EditRepeatingQuestViewState.StateType.*
import mypoli.android.repeatingquest.picker.RepeatingPatternPickerDialogController

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/26/18.
 */
class EditRepeatingQuestViewController(args: Bundle? = null) :
    ReduxViewController<EditRepeatingQuestAction, EditRepeatingQuestViewState, EditRepeatingQuestReducer>(
        args
    ) {
    override val reducer = EditRepeatingQuestReducer

    private lateinit var repeatingQuestId: String

    constructor(
        repeatingQuestId: String
    ) : this() {
        this.repeatingQuestId = repeatingQuestId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(
            R.layout.controller_edit_repeating_quest, container, false
        )
        setToolbar(view.toolbar)
        toolbarTitle = ""
        return view
    }

    override fun onCreateLoadAction() =
        EditRepeatingQuestAction.Load(repeatingQuestId)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_repeating_quest_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.actionSave -> {
                dispatch(EditRepeatingQuestAction.Save)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun render(state: EditRepeatingQuestViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                view.questName.setText(state.name)
                view.questName.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {

                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        text: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        dispatch(EditRepeatingQuestAction.ChangeName(text.toString()))
                    }

                })
                renderAll(view, state)
            }

            REPEATING_PATTERN_CHANGED -> {
                renderFrequency(view, state)
            }

            START_TIME_CHANGED -> {
                renderStartTime(view, state)
            }

            DURATION_CHANGED -> {
                renderDuration(view, state)
            }

            REMINDER_CHANGED -> {
                renderReminder(view, state)
            }

            COLOR_CHANGED -> {
                renderColor(view, state)
            }
        }
    }

    private fun renderAll(
        view: View,
        state: EditRepeatingQuestViewState
    ) {
        renderFrequency(view, state)
        renderStartTime(view, state)
        renderDuration(view, state)
        renderReminder(view, state)
        renderColor(view, state)
    }

    private fun renderColor(view: View, state: EditRepeatingQuestViewState) {
        //color layout
        view.questColorContainer.setOnClickListener {
            ColorPickerDialogController(object :
                ColorPickerDialogController.ColorPickedListener {
                override fun onColorPicked(color: AndroidColor) {
                    dispatch(EditRepeatingQuestAction.ChangeColor(color.color))
                }

            }, state.color.androidColor).showDialog(
                router,
                "pick_color_tag"
            )
        }
    }

    private fun renderReminder(
        view: View,
        state: EditRepeatingQuestViewState
    ) {
        view.questReminderValue.text = state.formattedReminder
        view.questReminderContainer.setOnClickListener {
            ReminderPickerDialogController(object :
                ReminderPickerDialogController.ReminderPickedListener {
                override fun onReminderPicked(reminder: ReminderViewModel?) {
                    dispatch(EditRepeatingQuestAction.ChangeReminder(reminder))
                }
            }, state.reminderViewModel).showDialog(router, "pick_reminder_tag")
        }

    }

    private fun renderDuration(
        view: View,
        state: EditRepeatingQuestViewState
    ) {
        view.questDurationValue.text = state.formattedDuration
        view.questDurationContainer.setOnClickListener {
            DurationPickerDialogController(object :
                DurationPickerDialogController.DurationPickedListener {
                override fun onDurationPicked(minutes: Int) {
                    dispatch(EditRepeatingQuestAction.ChangeDuration(minutes))
                }

            }, state.duration).showDialog(router, "pick_duration_tag")
        }
    }

    private fun renderStartTime(
        view: View,
        state: EditRepeatingQuestViewState
    ) {
        view.questStartTimeValue.text = state.formattedStartTime
        view.questStartTimeContainer.setOnClickListener {
            val startTime = state.startTime ?: Time.now()
            val dialog = TimePickerDialog(
                view.context,
                TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                    dispatch(
                        EditRepeatingQuestAction.ChangeStartTime(
                            Time.at(
                                hour,
                                minute
                            )
                        )
                    )
                }, startTime.hours, startTime.getMinutes(), false
            )
            dialog.setButton(
                Dialog.BUTTON_NEUTRAL,
                view.context.getString(R.string.do_not_know),
                { _, _ ->
                    dispatch(EditRepeatingQuestAction.ChangeStartTime(null))
                })
            dialog.show()
        }
    }

    private fun renderFrequency(
        view: View,
        state: EditRepeatingQuestViewState
    ) {
        view.questRepeatPatternValue.text = state.formattedFrequencyType
        view.questFrequencyContainer.setOnClickListener {
            RepeatingPatternPickerDialogController(
                state.repeatingPattern,
                {
                    dispatch(EditRepeatingQuestAction.ChangeRepeatingPattern(it))
                }).showDialog(router, "repeating-pattern")
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
//        showBackButton()
    }

    private val EditRepeatingQuestViewState.formattedDuration: String
        get() = DurationFormatter.formatReadable(view!!.context, duration)

    private val EditRepeatingQuestViewState.formattedStartTime: String
        get() =
            if (startTime != null) startTime.toString()
            else stringRes(R.string.do_not_know)

    private val EditRepeatingQuestViewState.formattedFrequencyType: String
        get() = stringsRes(R.array.repeating_quest_frequencies)[frequencyType.ordinal]

    private val EditRepeatingQuestViewState.formattedReminder: String
        get() {
            if (reminder == null) {
                return stringRes(R.string.do_not_remind)
            } else {
                return reminder.remindTime.toString()
            }
        }
}