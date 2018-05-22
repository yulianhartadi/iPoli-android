package io.ipoli.android.planday

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.BaseViewState
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.view.ReduxDialogController
import io.ipoli.android.common.view.recyclerview.BaseRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.RecyclerViewViewModel
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.common.view.stringRes
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.planday.RescheduleDialogViewState.StateType.DATA_LOADED
import io.ipoli.android.planday.RescheduleDialogViewState.StateType.LOADING
import kotlinx.android.synthetic.main.dialog_reschedule.view.*
import kotlinx.android.synthetic.main.item_reschedule_date.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/17/18.
 */
sealed class RescheduleDialogAction : Action {
    data class Load(val includeToday: Boolean) : RescheduleDialogAction()
}

object RescheduleDialogReducer : BaseViewStateReducer<RescheduleDialogViewState>() {
    override val stateKey = key<RescheduleDialogViewState>()

    override fun reduce(
        state: AppState,
        subState: RescheduleDialogViewState,
        action: Action
    ): RescheduleDialogViewState {
        return when (action) {
            is RescheduleDialogAction.Load -> {
                subState.copy(
                    type = DATA_LOADED,
                    petAvatar = state.dataState.player!!.pet.avatar
                )
            }
            else -> subState
        }
    }

    override fun defaultState() = RescheduleDialogViewState(
        type = LOADING,
        petAvatar = Constants.DEFAULT_PET_AVATAR
    )


}

data class RescheduleDialogViewState(
    val type: StateType,
    val petAvatar: PetAvatar
) : BaseViewState() {
    enum class StateType {
        LOADING,
        DATA_LOADED
    }
}

class RescheduleDialogController(args: Bundle? = null) :
    ReduxDialogController<RescheduleDialogAction, RescheduleDialogViewState, RescheduleDialogReducer>(
        args
    ) {

    override val reducer = RescheduleDialogReducer

    private var includeToday: Boolean = true

    private lateinit var listener: (LocalDate?) -> Unit

    private lateinit var cancelListener: () -> Unit

    constructor(
        includeToday: Boolean,
        listener: (LocalDate?) -> Unit,
        cancelListener: () -> Unit = {}
    ) : this() {
        this.includeToday = includeToday
        this.listener = listener
        this.cancelListener = cancelListener
    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_reschedule, null)
        view.dateList.layoutManager = GridLayoutManager(view.context, 2)
        view.dateList.adapter = DateAdapter()
        return view
    }

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.choose_date)
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog = dialogBuilder
        .setNegativeButton(R.string.cancel, null)
        .create()

    override fun onCreateLoadAction() = RescheduleDialogAction.Load(includeToday)

    override fun render(state: RescheduleDialogViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                changeIcon(AndroidPetAvatar.valueOf(state.petAvatar.name).headImage)
                (view.dateList.adapter as DateAdapter).updateAll(state.viewModels)
            }
        }
    }

    override fun onDialogCreated(dialog: AlertDialog, contentView: View) {
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
                cancelListener.invoke()
                dismiss()
            }
        }
    }

    data class DateViewModel(
        val icon: Int,
        val text: String,
        val date: LocalDate?,
        val showPicker: Boolean = false
    ) : RecyclerViewViewModel {
        override val id: String
            get() = text
    }

    inner class DateAdapter :
        BaseRecyclerViewAdapter<DateViewModel>(R.layout.item_reschedule_date) {

        override fun onBindViewModel(vm: DateViewModel, view: View, holder: SimpleViewHolder) {
            view.rescheduleDate.text = vm.text
            view.rescheduleIcon.setImageResource(vm.icon)
            view.onDebounceClick {
                if (!vm.showPicker) {
                    listener(vm.date)
                    dismiss()
                } else {
                    val date = LocalDate.now()
                    DatePickerDialog(
                        view.context, R.style.Theme_myPoli_AlertDialog,
                        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                            listener(LocalDate.of(year, month + 1, dayOfMonth))
                            dismiss()
                        }, date.year, date.month.value - 1, date.dayOfMonth
                    ).show()
                }
            }

        }

    }

    private val RescheduleDialogViewState.viewModels: List<DateViewModel>
        get() {
            val vms = mutableListOf<DateViewModel>()
            val today = LocalDate.now()
            vms.addAll(
                listOf(
                    DateViewModel(
                        R.drawable.ic_tomorrow_black_24dp,
                        stringRes(R.string.tomorrow),
                        today.plusDays(1)
                    ),

                    DateViewModel(
                        R.drawable.ic_bucket_black_24dp,
                        stringRes(R.string.bucket),
                        null
                    ),

                    DateViewModel(
                        R.drawable.ic_more_circle_black_24dp,
                        stringRes(R.string.pick_date),
                        null,
                        true
                    )
                )
            )
            if (includeToday) {
                vms.add(
                    0,
                    DateViewModel(
                        R.drawable.ic_today_black_24dp,
                        stringRes(R.string.today),
                        today
                    )
                )
            } else {
                vms.add(
                    1,
                    DateViewModel(
                        R.drawable.ic_next_week,
                        stringRes(R.string.next_week),
                        today.plusDays(7)
                    )
                )
            }

            return vms
        }
}