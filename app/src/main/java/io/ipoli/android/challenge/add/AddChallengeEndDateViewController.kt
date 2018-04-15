package io.ipoli.android.challenge.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.ipoli.android.R
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.redux.android.BaseViewController
import io.ipoli.android.common.view.stringRes
import kotlinx.android.synthetic.main.controller_add_challenge_end_date.view.*
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/9/18.
 */
class AddChallengeEndDateViewController(args: Bundle? = null) :
    BaseViewController<EditChallengeAction, EditChallengeViewState>(
        args
    ) {
    override val stateKey = EditChallengeReducer.stateKey

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.controller_add_challenge_end_date, container, false)
        view.dateList.layoutManager =
            LinearLayoutManager(container.context, LinearLayoutManager.VERTICAL, false)
        view.dateList.adapter = DateAdapter()
        return view
    }

    override fun colorLayoutBars() {}

    override fun render(state: EditChallengeViewState, view: View) {
        (view.dateList.adapter as DateAdapter).updateAll(state.viewModels)
    }

    data class DateViewModel(
        val text: String,
        val date: LocalDate?
    )

    inner class DateAdapter(private var viewModels: List<DateViewModel> = listOf()) :
        RecyclerView.Adapter<ViewHolder>() {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = viewModels[position]
            val view = holder.itemView as TextView
            view.text = vm.text
            if (vm.date != null) {
                view.dispatchOnClick(EditChallengeAction.SelectDate(vm.date))
            } else {
                view.setOnClickListener {
                    val date = LocalDate.now()
                    val datePickerDialog = DatePickerDialog(
                        view.context, R.style.Theme_myPoli_AlertDialog,
                        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                            dispatch(
                                EditChallengeAction.SelectDate(
                                    LocalDate.of(
                                        year,
                                        month + 1,
                                        dayOfMonth
                                    )
                                )
                            )
                        }, date.year, date.month.value - 1, date.dayOfMonth
                    )
                    datePickerDialog.datePicker.minDate = DateUtils.toMillis(date)
                    datePickerDialog.show()
                }
            }
        }

        fun updateAll(viewModels: List<DateViewModel>) {
            this.viewModels = viewModels
            notifyDataSetChanged()
        }

        override fun getItemCount() = viewModels.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_add_challenge_end_date,
                    parent,
                    false
                )
            )

    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private val EditChallengeViewState.viewModels: List<DateViewModel>
        get() {
            val today = LocalDate.now().minusDays(1)
            val viewModels = mutableListOf<DateViewModel>()
            viewModels.add(DateViewModel(stringRes(R.string.one_month), today.plusMonths(1)))
            viewModels.add(DateViewModel(stringRes(R.string.one_week), today.plusWeeks(1)))
            viewModels.add(DateViewModel(stringRes(R.string.ten_days), today.plusDays(10)))
            viewModels.add(DateViewModel(stringRes(R.string.two_weeks), today.plusWeeks(2)))
            viewModels.add(DateViewModel(stringRes(R.string.three_months), today.plusMonths(3)))
            viewModels.add(DateViewModel(stringRes(R.string.fifteen_days), today.plusDays(15)))
            viewModels.add(DateViewModel(stringRes(R.string.exact_date), null))
            return viewModels
        }
}