package mypoli.android.repeatingquest.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.controller_edit_repeating_quest.view.*
import mypoli.android.R
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.text.DurationFormatter
import mypoli.android.common.view.stringRes
import mypoli.android.common.view.stringsRes
import mypoli.android.reminder.view.formatter.ReminderTimeFormatter
import mypoli.android.repeatingquest.edit.EditRepeatingQuestViewState.StateType.CHANGED
import mypoli.android.timer.view.formatter.TimerFormatter

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
        val view = inflater.inflate(
            R.layout.controller_edit_repeating_quest, container, false
        )
        return view
    }

    override fun onCreateLoadAction() =
        EditRepeatingQuestAction.Load(repeatingQuestId)

    override fun render(state: EditRepeatingQuestViewState, view: View) {
        when (state.type) {
            CHANGED -> {
                view.questName.setText(state.name)
                view.questRepeatPatternValue.text = state.formattedFrequencyType
                view.questStartTimeValue.text = state.formattedStartTime
                view.questDurationValue.text = state.formattedDuration
            }
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
            if (startTime != null) TimerFormatter.format(startTime.toMillisOfDay())
            else stringRes(R.string.do_not_know)

    private val EditRepeatingQuestViewState.formattedFrequencyType: String
        get() = stringsRes(R.array.repeating_quest_frequencies)[frequencyType.ordinal]

    private val EditRepeatingQuestViewState.formattedReminder: String
        get() {
            if (reminder == null) {
                return stringRes(R.string.do_not_remind)
            } else {
                return ReminderTimeFormatter(view!!.context).format(reminder.remindTime.toMillisOfDay())
            }
        }
}