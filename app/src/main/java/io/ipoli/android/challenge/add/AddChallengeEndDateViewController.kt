package io.ipoli.android.challenge.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.controller_add_challenge_end_date.view.*
import io.ipoli.android.R
import io.ipoli.android.challenge.add.AddChallengeEndDateViewState.StateType.INITIAL
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.stringRes
import io.ipoli.android.quest.Color
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/9/18.
 */

sealed class AddChallengeEndDateAction : Action {
    data class SelectDate(val date: LocalDate) : AddChallengeEndDateAction()
}

object AddChallengeEndDateReducer : BaseViewStateReducer<AddChallengeEndDateViewState>() {
    override val stateKey = key<AddChallengeEndDateViewState>()


    override fun reduce(
        state: AppState,
        subState: AddChallengeEndDateViewState,
        action: Action
    ): AddChallengeEndDateViewState {
        return subState
    }

    override fun defaultState() =
        AddChallengeEndDateViewState(
            type = INITIAL,
            color = Color.GREEN
        )
}

data class AddChallengeEndDateViewState(
    val type: AddChallengeEndDateViewState.StateType,
    val color: Color
) : ViewState {
    enum class StateType {
        INITIAL,
        DATA_LOADED
    }
}

class AddChallengeEndDateViewController(args: Bundle? = null) :
    ReduxViewController<AddChallengeEndDateAction, AddChallengeEndDateViewState, AddChallengeEndDateReducer>(
        args
    ) {

    override val reducer = AddChallengeEndDateReducer

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

    override fun render(state: AddChallengeEndDateViewState, view: View) {
        when (state.type) {
            INITIAL -> {
                (view.dateList.adapter as DateAdapter).updateAll(state.viewModels)
            }
        }
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
                view.dispatchOnClick(AddChallengeEndDateAction.SelectDate(vm.date))
            } else {
                view.setOnClickListener {
                    val date = LocalDate.now()
                    val datePickerDialog = DatePickerDialog(
                        view.context, R.style.Theme_myPoli_AlertDialog,
                        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                            dispatch(
                                AddChallengeEndDateAction.SelectDate(
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

    private val AddChallengeEndDateViewState.viewModels: List<DateViewModel>
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