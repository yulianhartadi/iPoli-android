package io.ipoli.android.repeatingquest.edit

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.*
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.text.DurationFormatter
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.recyclerview.BaseRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.ReorderItemHelper
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.note.NoteDialogViewController
import io.ipoli.android.quest.reminder.picker.ReminderPickerDialogController
import io.ipoli.android.quest.reminder.picker.ReminderViewModel
import io.ipoli.android.repeatingquest.edit.EditRepeatingQuestViewState.StateType.*
import io.ipoli.android.repeatingquest.edit.picker.RepeatingPatternPickerDialogController
import kotlinx.android.synthetic.main.controller_edit_repeating_quest.view.*
import kotlinx.android.synthetic.main.item_edit_repeating_quest_sub_quest.view.*
import java.util.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 2/26/18.
 */
class EditRepeatingQuestViewController(args: Bundle? = null) :
    ReduxViewController<EditRepeatingQuestAction, EditRepeatingQuestViewState, EditRepeatingQuestReducer>(
        args, renderDuplicateStates = true
    ) {
    override val reducer = EditRepeatingQuestReducer

    private lateinit var repeatingQuestId: String

    private lateinit var touchHelper: ItemTouchHelper

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

        view.subQuestList.layoutManager = LinearLayoutManager(activity!!)
        view.subQuestList.adapter = SubQuestsAdapter()

        val dragHelper =
            ReorderItemHelper(
                onItemMoved = { oldPosition, newPosition ->
                    (view.subQuestList.adapter as SubQuestsAdapter).move(oldPosition, newPosition)
                }
            )

        touchHelper = ItemTouchHelper(dragHelper)
        touchHelper.attachToRecyclerView(view.subQuestList)

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
            android.R.id.home -> {
                router.popCurrentController()
                true
            }
            R.id.actionSave -> {

                val name = view!!.questName.text.toString()

                val subQuestNames = view!!.subQuestList.children.map {
                    it.editSubQuestName.text.toString()
                }

                dispatch(EditRepeatingQuestAction.Save(name, subQuestNames))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun render(state: EditRepeatingQuestViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                view.questName.setText(state.name)
                renderAll(view, state)
            }

            REPEATING_PATTERN_CHANGED -> {
                renderRepeatType(view, state)
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

            ICON_CHANGED -> {
                renderIcon(view, state)
            }

            NOTE_CHANGED -> {
                renderNote(view, state)
            }

            VALIDATION_ERROR_EMPTY_NAME -> {
                view.questNameLayout.error = "Think of a name"
            }

            SUB_QUEST_ADDED -> {
                (view.subQuestList.adapter as SubQuestsAdapter).add(
                    SubQuestViewModel(
                        id = UUID.randomUUID().toString(),
                        name = "",
                        startInEdit = true
                    )
                )
            }

            QUEST_SAVED -> {
                router.popController(this)
            }
        }
    }

    private fun renderAll(
        view: View,
        state: EditRepeatingQuestViewState
    ) {
        renderSubQuests(view, state)
        renderRepeatType(view, state)
        renderStartTime(view, state)
        renderDuration(view, state)
        renderReminder(view, state)
        renderColor(view, state)
        renderIcon(view, state)
        renderNote(view, state)
    }

    private fun renderNote(view: View, state: EditRepeatingQuestViewState) {
        view.questNoteValue.text = state.noteText
        view.questNoteContainer.setOnClickListener {
            NoteDialogViewController(state.note ?: "", { note ->
                dispatch(EditRepeatingQuestAction.ChangeNote(note))
            }).show(router)
        }
    }

    private fun renderSubQuests(view: View, state: EditRepeatingQuestViewState) {
        val adapter = view.subQuestList.adapter as SubQuestsAdapter
        adapter.updateAll(state.subQuestViewModels)
        view.addSubQuest.dispatchOnClick(EditRepeatingQuestAction.AddSubQuest)
    }

    private fun colorLayout(
        view: View,
        state: EditRepeatingQuestViewState
    ) {
        val color500 = colorRes(state.color500)
        val color700 = colorRes(state.color700)
        view.appbar.setBackgroundColor(color500)
        view.toolbar.setBackgroundColor(color500)
        view.toolbarCollapsingContainer.setContentScrimColor(color500)
        activity?.window?.navigationBarColor = color500
        activity?.window?.statusBarColor = color700
    }

    private fun renderIcon(view: View, state: EditRepeatingQuestViewState) {
        view.questIconIcon.setImageDrawable(state.iconDrawable)
        view.questIconContainer.setOnClickListener {
            IconPickerDialogController({ icon ->
                dispatch(EditRepeatingQuestAction.ChangeIcon(icon))
            }, state.icon?.androidIcon).showDialog(
                router,
                "pick_icon_tag"
            )
        }
    }

    private fun renderColor(view: View, state: EditRepeatingQuestViewState) {
        colorLayout(view, state)
        view.questColorContainer.setOnClickListener {
            ColorPickerDialogController({
                dispatch(EditRepeatingQuestAction.ChangeColor(it.color))
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

    private fun renderRepeatType(
        view: View,
        state: EditRepeatingQuestViewState
    ) {
        view.questRepeatPatternValue.text = state.formattedRepeatType
        view.questRepeatContainer.setOnClickListener {
            RepeatingPatternPickerDialogController(
                state.repeatingPattern,
                {
                    dispatch(EditRepeatingQuestAction.ChangeRepeatingPattern(it))
                }).show(router, "repeating-pattern")
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        showBackButton()
    }

    data class SubQuestViewModel(val id: String, val name: String, val startInEdit: Boolean = false)

    inner class SubQuestsAdapter :
        BaseRecyclerViewAdapter<SubQuestViewModel>(
            R.layout.item_edit_repeating_quest_sub_quest
        ) {
        override fun onBindViewModel(
            vm: SubQuestViewModel,
            view: View,
            holder: SimpleViewHolder
        ) {
            view.subQuestIndicator.backgroundTintList =
                ColorStateList.valueOf(colorRes(R.color.md_dark_text_54))
            view.editSubQuestName.setText(vm.name)

            view.reorderButton.setOnTouchListener { v, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    touchHelper.startDrag(holder)
                }
                false
            }

            view.removeButton.setOnClickListener {
                removeAt(holder.adapterPosition)
            }

            view.editSubQuestName.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    startEdit(view)
                }
            }

            if (vm.startInEdit) {
                startEdit(view)
            }
        }

        private fun startEdit(view: View) {
            disableEditForAllSubQuests()
            view.reorderButton.gone()
            view.removeButton.visible()
            view.editSubQuestName.requestFocus()
            ViewUtils.showKeyboard(view.context, view.editSubQuestName)
            view.editSubQuestName.setSelection(view.editSubQuestName.length())
        }
    }

    private fun disableEditForAllSubQuests() {
        view!!.subQuestList.children.forEach {
            it.removeButton.gone()
            it.reorderButton.visible()
        }
    }

    private val EditRepeatingQuestViewState.subQuestViewModels: List<SubQuestViewModel>
        get() = subQuestNames.map {
            SubQuestViewModel(id = UUID.randomUUID().toString(), name = it, startInEdit = false)
        }

    private val EditRepeatingQuestViewState.formattedDuration: String
        get() = DurationFormatter.formatReadable(view!!.context, duration)

    private val EditRepeatingQuestViewState.formattedStartTime: String
        get() =
            startTime?.toString() ?: stringRes(R.string.do_not_know)

    private val EditRepeatingQuestViewState.formattedRepeatType: String
        get() = stringsRes(R.array.repeating_quest_frequencies)[repeatType.ordinal]

    private val EditRepeatingQuestViewState.formattedReminder: String
        get() {
            if (reminder == null) {
                return stringRes(R.string.do_not_remind)
            } else {
                return reminder.remindTime.toString()
            }
        }

    private val EditRepeatingQuestViewState.iconDrawable: Drawable
        get() =
            if (icon == null) {
                ContextCompat.getDrawable(view!!.context, R.drawable.ic_icon_black_24dp)!!
            } else {
                val androidIcon = icon.androidIcon
                IconicsDrawable(view!!.context)
                    .icon(androidIcon.icon)
                    .colorRes(androidIcon.color)
                    .sizeDp(24)
            }

    private val EditRepeatingQuestViewState.color500: Int
        get() = color.androidColor.color500

    private val EditRepeatingQuestViewState.color700: Int
        get() = color.androidColor.color700

    private val EditRepeatingQuestViewState.noteText: String
        get() = note ?: stringRes(R.string.tap_to_add_note)
}

