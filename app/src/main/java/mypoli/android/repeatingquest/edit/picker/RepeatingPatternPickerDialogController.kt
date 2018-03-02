package mypoli.android.repeatingquest.edit.picker

import android.app.DatePickerDialog
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.transition.TransitionManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_repeating_picker.view.*
import kotlinx.android.synthetic.main.popup_rate.view.*
import mypoli.android.R
import mypoli.android.common.ViewUtils
import mypoli.android.common.datetime.DateUtils
import mypoli.android.common.text.DateFormatter
import mypoli.android.common.view.*
import mypoli.android.repeatingquest.entity.RepeatType
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.edit.picker.RepeatingPatternViewState.StateType.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle


/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/16/18.
 */
class RepeatingPatternPickerDialogController :
    ReduxDialogController<RepeatingPatternAction, RepeatingPatternViewState, RepeatingPatternReducer> {

    override val reducer = RepeatingPatternReducer

    private var repeatingPattern: RepeatingPattern? = null
    private lateinit var resultListener: (RepeatingPattern) -> Unit
    private var cancelListener: (() -> Unit)? = null

    constructor(args: Bundle? = null) : super(args)

    constructor(
        repeatingPattern: RepeatingPattern? = null,
        resultListener: (RepeatingPattern) -> Unit,
        cancelListener: (() -> Unit)? = null
    ) : this() {
        this.repeatingPattern = repeatingPattern
        this.resultListener = resultListener
        this.cancelListener = cancelListener
    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_repeating_picker, null)

        view.rpWeekDayList.layoutManager =
            LinearLayoutManager(activity!!, LinearLayoutManager.HORIZONTAL, false)
        view.rpWeekDayList.adapter = WeekDayAdapter()

        view.rpMonthDayList.layoutManager = GridLayoutManager(activity, 7)
        view.rpMonthDayList.setHasFixedSize(true)
        view.rpMonthDayList.adapter = MonthDayAdapter()

        return view
    }

    override fun onCreateLoadAction() =
        RepeatingPatternAction.LoadData(repeatingPattern)

    override fun render(state: RepeatingPatternViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                renderStartDate(view, state)
                initStartDateListener(view, state)

                renderEndDate(view, state)
                initEndDateListener(view, state)

                renderForRepeatType(state, view)

                view.rpMessage.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(view.context, state.petAvatar!!),
                    null,
                    null,
                    null
                )
            }

            REPEAT_TYPE_CHANGED -> {
                TransitionManager.beginDelayedTransition(view as ViewGroup)
                renderForRepeatType(state, view)
            }

            WEEK_DAYS_CHANGED -> {
                renderWeekDays(view, state)
                renderMessage(view, state)
            }

            COUNT_CHANGED -> {
                renderMessage(view, state)
            }

            MONTH_DAYS_CHANGED -> {
                renderMonthDays(view, state)
                renderMessage(view, state)
            }

            YEAR_DAY_CHANGED -> {
                renderDayOfYear(view, state)
            }

            START_DATE_CHANGED -> {
                renderStartDate(view, state)
            }

            END_DATE_CHANGED -> {
                renderEndDate(view, state)
            }

            PATTERN_CREATED -> {
                resultListener(state.resultPattern!!)
                dismissDialog()
            }
        }
    }

    private fun renderDayOfYear(
        view: View,
        state: RepeatingPatternViewState
    ) {
        view.rpDayOfYear.text = state.formattedDayOfYear
    }

    private fun initEndDateListener(
        view: View,
        state: RepeatingPatternViewState
    ) {
        view.rpEnd.setOnClickListener {
            val date = state.pickerEndDate
            val datePickerDialog = DatePickerDialog(
                view.context, R.style.Theme_myPoli_AlertDialog,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    dispatch(
                        RepeatingPatternAction.ChangeEndDate(
                            LocalDate.of(year, month + 1, dayOfMonth)
                        )
                    )
                }, date.year, date.month.value - 1, date.dayOfMonth
            )
            datePickerDialog.datePicker.minDate = DateUtils.toMillis(state.startDate.plusDays(1))
            datePickerDialog.show()
        }
    }

    private fun renderEndDate(
        view: View,
        state: RepeatingPatternViewState
    ) {
        view.rpEnd.text = state.formattedEndDate
    }

    private fun initStartDateListener(
        view: View,
        state: RepeatingPatternViewState
    ) {
        view.rpStart.setOnClickListener {
            val date = state.startDate
            val datePickerDialog = DatePickerDialog(
                view.context, R.style.Theme_myPoli_AlertDialog,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    dispatch(
                        RepeatingPatternAction.ChangeStartDate(
                            LocalDate.of(year, month + 1, dayOfMonth)
                        )
                    )
                }, date.year, date.month.value - 1, date.dayOfMonth
            )
            datePickerDialog.datePicker.minDate = DateUtils.toMillis(LocalDate.now())
            if (state.endDate != null) {
                datePickerDialog.datePicker.maxDate = DateUtils.toMillis(state.endDate.minusDays(1))
            }
            datePickerDialog.show()
        }
    }

    private fun renderStartDate(
        view: View,
        state: RepeatingPatternViewState
    ) {
        view.rpStart.text = state.formattedStartDate
    }

    private fun renderForRepeatType(
        state: RepeatingPatternViewState,
        view: View
    ) {
        when (state.repeatType) {
            RepeatType.DAILY -> renderDaily(view, state)
            RepeatType.WEEKLY -> renderWeekly(view, state)
            RepeatType.MONTHLY -> renderMonthly(view, state)
            RepeatType.YEARLY -> renderYearly(view, state)
        }
    }

    private fun renderYearly(
        view: View,
        state: RepeatingPatternViewState
    ) {
        ViewUtils.goneViews(
            view.rpWeekDayList,
            view.rpMonthDayList,
            view.countGroup
        )
        ViewUtils.showViews(
            view.yearlyPatternGroup
        )

        renderFrequencies(view, state)
        renderMessage(view, state)

        renderDayOfYear(view, state)

        view.rpDayOfYear.setOnClickListener {
            val date = state.dayOfYear
            val datePickerDialog = DatePickerDialog(
                view.context, R.style.Theme_myPoli_AlertDialog,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    dispatch(
                        RepeatingPatternAction.ChangeDayOfYear(
                            LocalDate.of(year, month + 1, dayOfMonth)
                        )
                    )
                }, date.year, date.month.value - 1, date.dayOfMonth
            )
            datePickerDialog.show()
        }
    }

    private fun renderMonthly(
        view: View,
        state: RepeatingPatternViewState
    ) {
        ViewUtils.goneViews(
            view.rpWeekDayList,
            view.yearlyPatternGroup
        )
        ViewUtils.showViews(
            view.rpMonthDayList,
            view.countGroup
        )

        renderFrequencies(view, state)
        renderMonthDaysCount(view, state)
        renderMonthDays(view, state)
        renderMessage(view, state)
    }

    private fun renderWeekly(
        view: View,
        state: RepeatingPatternViewState
    ) {
        ViewUtils.goneViews(
            view.rpMonthDayList,
            view.yearlyPatternGroup
        )
        ViewUtils.showViews(
            view.rpWeekDayList,
            view.countGroup
        )

        renderFrequencies(view, state)
        renderWeekDaysCount(view, state)
        renderWeekDays(view, state)
        renderMessage(view, state)
    }

    private fun renderDaily(
        view: View,
        state: RepeatingPatternViewState
    ) {
        ViewUtils.goneViews(
            view.rpWeekDayList,
            view.rpMonthDayList,
            view.yearlyPatternGroup,
            view.countGroup
        )
        renderFrequencies(view, state)
        renderMessage(view, state)
    }

    private fun renderMonthDays(
        view: View,
        state: RepeatingPatternViewState
    ) {
        (view.rpMonthDayList.adapter as MonthDayAdapter).updateAll(
            state.monthDaysViewModels()
        )
    }

    private fun renderMessage(
        view: View,
        state: RepeatingPatternViewState
    ) {
        if (state.isFlexible) {
            ViewUtils.showViews(view.rpMessage)
        } else {
            ViewUtils.goneViews(view.rpMessage)
        }
    }

    private fun renderWeekDaysCount(
        view: View,
        state: RepeatingPatternViewState
    ) {
        val count = view.rpCount
        count.adapter = ArrayAdapter(
            view.context,
            R.layout.item_dropdown_number_spinner,
            state.weekCountValues
        )
        count.onItemSelectedListener = null
        count.setSelection(state.weekDaysCountIndex)
        count.post {
            count.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    dispatch(RepeatingPatternAction.ChangeWeekDayCount(position))
                }

            }
        }
    }

    private fun renderMonthDaysCount(
        view: View,
        state: RepeatingPatternViewState
    ) {
        val count = view.rpCount
        count.adapter = ArrayAdapter(
            view.context,
            R.layout.item_dropdown_number_spinner,
            state.monthCountValues
        )
        count.onItemSelectedListener = null
        count.setSelection(state.monthDaysCountIndex)
        count.post {
            count.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    dispatch(RepeatingPatternAction.ChangeMonthDayCount(position))
                }
            }
        }
    }

    private fun renderWeekDays(
        view: View,
        state: RepeatingPatternViewState
    ) {
        (view.rpWeekDayList.adapter as WeekDayAdapter).updateAll(
            state.weekDaysViewModels()
        )
    }

    private fun renderFrequencies(
        view: View,
        state: RepeatingPatternViewState
    ) {
        val repeatType = view.rpRepeatType
        repeatType.onItemSelectedListener = null
        repeatType.setSelection(state.repeatTypeIndex)
        repeatType.post {
            repeatType.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        dispatch(RepeatingPatternAction.ChangeFrequency(position))
                    }

                }
            }

    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setPositiveButton("OK", null)
            .setNegativeButton(R.string.cancel, null)
            .create()


    override fun onDialogCreated(dialog: AlertDialog, contentView: View) {
        dialog.setOnShowListener {
            setPositiveButtonListener {
                dispatch(RepeatingPatternAction.CreatePattern)
            }
            setNegativeButtonListener {
                cancelListener?.invoke()
                dismissDialog()
            }
        }
    }

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.text = "Pick repeating pattern"
        headerView.dialogHeaderIcon.setImageResource(R.drawable.logo)
    }

    data class WeekDayViewModel(
        val text: String,
        @DrawableRes val background: Int,
        @ColorRes val textColor: Int,
        val isSelected: Boolean,
        val weekDay: DayOfWeek
    )

    inner class WeekDayAdapter(private var viewModels: List<WeekDayViewModel> = listOf()) :
        RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount() = viewModels.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = viewModels[position]
            val button = holder.itemView as Button
            button.text = vm.text
            val (background, textColor) = if (vm.isSelected)
                Pair(R.drawable.circle_accent, colorRes(R.color.md_white))
            else
                Pair(R.drawable.circle_normal, attrData(R.attr.colorAccent))

            button.setBackgroundResource(background)
            button.setTextColor(textColor)
            button.dispatchOnClick(RepeatingPatternAction.ToggleWeekDay(vm.weekDay))
        }

        fun updateAll(viewModels: List<WeekDayViewModel>) {
            this.viewModels = viewModels
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_repeating_pattern_week_day,
                    parent,
                    false
                )
            )

    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    data class MonthDayViewModel(
        val text: String,
        @DrawableRes val background: Int,
        val isSelected: Boolean, val day: Int
    )

    inner class MonthDayAdapter(private var viewModels: List<MonthDayViewModel> = listOf()) :
        RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount() = viewModels.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = viewModels[position]
            val view = holder.itemView as TextView
            view.text = vm.text
            view.setBackgroundResource(vm.background)
            view.dispatchOnClick(RepeatingPatternAction.ToggleMonthDay(vm.day))
        }

        fun updateAll(viewModels: List<MonthDayViewModel>) {
            this.viewModels = viewModels
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_repeating_pattern_month_day,
                    parent,
                    false
                )
            )

    }

    private fun RepeatingPatternViewState.weekDaysViewModels() =
        DateUtils.localeDaysOfWeek.map {
            val isSelected = selectedWeekDays.contains(it)
            val (background, textColor) = if (isSelected)
                Pair(R.drawable.circle_accent, colorRes(R.color.md_white))
            else
                Pair(R.drawable.circle_normal, attrData(R.attr.colorAccent))
            DateUtils.daysOfWeekText(TextStyle.SHORT_STANDALONE)

            RepeatingPatternPickerDialogController.WeekDayViewModel(
                text = DateUtils.dayOfWeekText(
                    it,
                    TextStyle.SHORT_STANDALONE
                ).first().toUpperCase().toString(),
                background = background,
                textColor = textColor,
                isSelected = isSelected,
                weekDay = it
            )
        }

    private fun RepeatingPatternViewState.monthDaysViewModels() =
        (1..31).map {
            val isSelected = selectedMonthDays.contains(it)
            val background = if (isSelected)
                R.drawable.bordered_circle_accent_background
            else
                attrResourceId(android.R.attr.selectableItemBackgroundBorderless)

            RepeatingPatternPickerDialogController.MonthDayViewModel(
                text = it.toString(),
                background = background,
                isSelected = isSelected,
                day = it
            )
        }

    private val RepeatingPatternViewState.formattedDayOfYear
        get() = DateFormatter.formatDayWithWeek(dayOfYear)

    private val RepeatingPatternViewState.formattedStartDate
        get() = DateFormatter.format(view!!.context, startDate)

    private val RepeatingPatternViewState.formattedEndDate
        get() =
            if (endDate == null)
                stringRes(R.string.end_of_time)
            else
                DateFormatter.format(view!!.context, endDate)



}