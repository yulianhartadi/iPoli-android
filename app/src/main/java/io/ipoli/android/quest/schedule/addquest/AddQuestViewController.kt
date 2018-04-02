package io.ipoli.android.quest.schedule.addquest

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.recyclerview.BaseRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.note.NoteDialogViewController
import io.ipoli.android.quest.reminder.picker.ReminderPickerDialogController
import io.ipoli.android.quest.reminder.picker.ReminderViewModel
import io.ipoli.android.quest.schedule.addquest.StateType.*
import io.ipoli.android.repeatingquest.edit.picker.RepeatingPatternPickerDialogController
import kotlinx.android.synthetic.main.controller_add_quest.view.*
import kotlinx.android.synthetic.main.item_add_sub_quest.view.*
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 11/2/17.
 */
class AddQuestViewController(args: Bundle? = null) :
    ReduxViewController<AddQuestAction, AddQuestViewState, AddQuestReducer>(
        args, true
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
                onSaveQuest(view)
            }
            true
        }

        view.duration.setImageDrawable(
            IconicsDrawable(view.context)
                .icon(GoogleMaterial.Icon.gmd_hourglass_empty)
                .colorRes(R.color.md_white)
                .sizeDp(22)
        )

        view.done.setOnClickListener {
            onSaveQuest(view)
        }
        view.subQuestList.layoutManager = LinearLayoutManager(activity!!)
        view.subQuestList.adapter = SubQuestAdapter()

        view.addSubQuest.dispatchOnClick(AddQuestAction.AddSubQuest)

        return view
    }

    private fun onSaveQuest(view: View) {

        val sqs = view.subQuestList.children.map {
            it.subQuestName.text.toString()
        }

        dispatch(
            AddQuestAction.Save(
                name = view.questName.text.toString(),
                subQuestNames = sqs
            )
        )
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

        when (state.type) {
            DATA_LOADED -> {
                renderDate(view, state)
                renderRepeatingPattern(view, state)
                renderStartTime(view, state)
                renderDuration(view, state)
                renderColor(view, state)
                renderIcon(view, state)
                renderReminder(view, state)
                renderNote(view, state)
            }

            DATE_PICKED -> renderDate(view, state)

            REPEATING_PATTERN_PICKED -> renderRepeatingPattern(view, state)

            TIME_PICKED -> renderStartTime(view, state)

            DURATION_PICKED -> renderDuration(view, state)

            COLOR_PICKED -> renderColor(view, state)

            ICON_PICKED -> renderIcon(view, state)

            REMINDER_PICKED -> renderReminder(view, state)

            NOTE_PICKED -> renderNote(view, state)

            SWITCHED_TO_QUEST -> {
                selectScheduleDate(view)
            }

            SWITCHED_TO_REPEATING -> {
                selectRepeatingPattern(view)
            }

            ADD_SUB_QUEST ->
                (view.subQuestList.adapter as SubQuestAdapter).add(
                    SubQuestViewModel(
                        name = "",
                        startInEdit = true
                    )
                )

            VALIDATION_ERROR_EMPTY_NAME ->
                view.questName.error = "Think of a name"

            QUEST_SAVED -> {
                resetForm(view)
            }
        }
    }

    private fun renderRepeatingPattern(
        view: View,
        state: AddQuestViewState
    ) {
        view.repeatingPattern.setOnClickListener {
            selectRepeatingPattern(view)
            RepeatingPatternPickerDialogController(
                state.repeatingPattern,
                { dispatch(AddQuestAction.RepeatingPatternPicked(it)) },
                { dispatch(AddQuestAction.RepeatingPatterPickerCanceled) }
            )
                .show(router, "pick_repeating_pattern_tag")
        }
    }

    private fun renderDate(
        view: View,
        state: AddQuestViewState
    ) {
        view.scheduleDate.setOnClickListener {
            selectScheduleDate(view)
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
    }

    private fun renderReminder(
        view: View,
        state: AddQuestViewState
    ) {
        resetOrColorIcon(state.reminder, view.reminder)
        view.reminder.setOnClickListener {
            ReminderPickerDialogController(object :
                ReminderPickerDialogController.ReminderPickedListener {
                override fun onReminderPicked(reminder: ReminderViewModel?) {
                    dispatch(AddQuestAction.ReminderPicked(reminder))
                }
            }, state.reminder).showDialog(router, "pick_reminder_tag")
        }
    }

    private fun renderIcon(
        view: View,
        state: AddQuestViewState
    ) {
        resetOrColorIcon(state.icon, view.icon)
        view.icon.setOnClickListener {
            IconPickerDialogController({ icon ->
                dispatch(AddQuestAction.IconPicked(icon))
            }, state.icon?.androidIcon).showDialog(
                router,
                "pick_icon_tag"
            )
        }
    }

    private fun renderColor(
        view: View,
        state: AddQuestViewState
    ) {
        state.color?.let {
            applySelectedColor(view.color)
        }
        view.color.setOnClickListener {
            ColorPickerDialogController({
                dispatch(AddQuestAction.ColorPicked(it.color))
            }, state.color?.androidColor).showDialog(
                router,
                "pick_color_tag"
            )
        }
    }

    private fun renderDuration(
        view: View,
        state: AddQuestViewState
    ) {
        state.duration?.let {
            applySelectedColor(view.duration)
        }
        view.duration.setOnClickListener {
            DurationPickerDialogController(object :
                DurationPickerDialogController.DurationPickedListener {
                override fun onDurationPicked(minutes: Int) {
                    dispatch(AddQuestAction.DurationPicked(minutes))
                }

            }, state.duration).showDialog(router, "pick_duration_tag")
        }
    }

    private fun renderStartTime(
        view: View,
        state: AddQuestViewState
    ) {
        resetOrColorIcon(state.time, view.startTime)
        view.startTime.setOnClickListener {
            val startTime = state.time ?: Time.now()
            val dialog = TimePickerDialog(
                view.context,
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
    }

    private fun renderNote(
        view: View,
        state: AddQuestViewState
    ) {
        resetOrColorIcon(state.note, view.note)
        view.note.setOnClickListener {
            NoteDialogViewController(state.note ?: "", { note ->
                dispatch(AddQuestAction.NotePicked(note))
            }).show(router)
        }
    }

    private fun resetForm(view: View) {
        view.questName.setText("")
        (view.subQuestList.adapter as SubQuestAdapter).updateAll(listOf())
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
        view.questName.requestFocus()
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

    data class SubQuestViewModel(
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        val startInEdit: Boolean = false
    )

    inner class SubQuestAdapter :
        BaseRecyclerViewAdapter<SubQuestViewModel>(R.layout.item_add_sub_quest) {
        override fun onBindViewModel(
            vm: SubQuestViewModel,
            view: View,
            holder: SimpleViewHolder
        ) {
            view.subQuestIndicator.backgroundTintList =
                ColorStateList.valueOf(colorRes(R.color.md_white))
            view.subQuestName.setText(vm.name)
            view.removeSubQuest.setOnClickListener {
                removeAt(holder.adapterPosition)
            }

            if (vm.startInEdit) {
                view.subQuestName.requestFocus()
                ViewUtils.showKeyboard(view.context, view.subQuestName)
            }
        }

    }
}