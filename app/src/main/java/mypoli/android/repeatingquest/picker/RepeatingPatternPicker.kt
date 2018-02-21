package mypoli.android.repeatingquest.picker

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_repeating_picker.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*
import mypoli.android.R
import mypoli.android.common.view.ReduxDialogController
import mypoli.android.common.view.attr
import mypoli.android.common.view.attrResource
import mypoli.android.common.view.colorRes
import mypoli.android.repeatingquest.entity.RepeatingPattern
import org.threeten.bp.DayOfWeek
import java.util.*


/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/16/18.
 */
class RepeatingPatternPicker :
    ReduxDialogController<RepeatingPatternAction, RepeatingPatternViewState, RepeatingPatternReducer> {

    override val reducer = RepeatingPatternReducer

    private lateinit var repeatingPattern: RepeatingPattern
    private lateinit var resultListener: (RepeatingPattern) -> Unit

    constructor(args: Bundle? = null) : super(args)

    constructor(
        repeatingPattern: RepeatingPattern? = null,
        resultListener: (RepeatingPattern) -> Unit
    ) : this() {
        this.repeatingPattern = repeatingPattern ?: RepeatingPattern.Daily
        this.resultListener = resultListener
    }

    override fun render(state: RepeatingPatternViewState, view: View) {

    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_repeating_picker, null)

//        val spinnerArrayAdapter =
//            ArrayAdapter<String>(view.context, android.R.layout.simple_spinner_dropdown_item,
//                (1..31).map { it.toString() })
//        view.rqCount.adapter = spinnerArrayAdapter

        view.rpWeekDayList.layoutManager =
            LinearLayoutManager(activity!!, LinearLayoutManager.HORIZONTAL, false)

        view.rpWeekDayList.adapter = WeekDayAdapter(
            DayOfWeek.values().map {
                WeekDayViewModel(
                    it.name.first().toString().toUpperCase(),
                    Random().nextBoolean(),
                    it
                )
            }
        )

        view.rpMonthDayList.layoutManager = GridLayoutManager(activity, 7)
        view.rpMonthDayList.setHasFixedSize(true)
        view.rpMonthDayList.adapter = MonthDayAdapter(
            (1..31).map {
                MonthDayViewModel(
                    it.toString(),
                    Random().nextBoolean(),
                    it
                )
            }
        )

        return view
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setPositiveButton("OK", { _, _ ->
                resultListener(repeatingPattern)
            })
            .setNegativeButton(R.string.cancel, null)
            .create()

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.text = "Pick repeating pattern"
        headerView.dialogHeaderIcon.setImageResource(R.drawable.logo)
    }

    data class WeekDayViewModel(val text: String, val isSelected: Boolean, val weekDay: DayOfWeek)

    inner class WeekDayAdapter(private var viewModels: List<WeekDayViewModel>) :
        RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount() = viewModels.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = viewModels[position]
            val button = holder.itemView as Button
            button.text = vm.text
            val (background, textColor) = if (vm.isSelected)
                Pair(R.drawable.circle_accent, colorRes(R.color.md_white))
            else
                Pair(R.drawable.circle_normal, attr(R.attr.colorAccent))

            button.setBackgroundResource(background)
            button.setTextColor(textColor)

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

    data class MonthDayViewModel(val text: String, val isSelected: Boolean, val day: Int)

    inner class MonthDayAdapter(private var viewModels: List<MonthDayViewModel>) :
        RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount() = viewModels.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = viewModels[position]
            val view = holder.itemView as TextView
            view.text = vm.text
            if (vm.isSelected) {
                view.setBackgroundResource(R.drawable.bordered_circle_accent_background)
            } else {
                view.setBackgroundResource(attrResource(android.R.attr.selectableItemBackgroundBorderless))
            }
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

}