package io.ipoli.android.event.calendar.picker

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.R
import io.ipoli.android.common.view.ReduxDialogController
import io.ipoli.android.common.view.recyclerview.BaseRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.RecyclerViewViewModel
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.player.data.Player.Preferences.SyncCalendar
import kotlinx.android.synthetic.main.dialog_calendar_picker.view.*
import kotlinx.android.synthetic.main.item_calendar_picker.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/11/2018.
 */
class CalendarPickerDialogController :
    ReduxDialogController<CalendarPickerAction, CalendarPickerViewState, CalendarPickerReducer> {

    override val reducer = CalendarPickerReducer

    private var pickedCalendarsListener: (Set<SyncCalendar>) -> Unit = {}

    constructor(args: Bundle? = null) : super(args)

    constructor(
        pickedCalendarsListener: (Set<SyncCalendar>) -> Unit
    ) : this() {
        this.pickedCalendarsListener = pickedCalendarsListener
    }

    override fun onCreateLoadAction() = CalendarPickerAction.Load

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setPositiveButton(R.string.sync_selected, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.calendar_picker_title)
    }


    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        @SuppressLint("InflateParams")
        val view = inflater.inflate(R.layout.dialog_calendar_picker, null)

        view.calendarList.layoutManager = LinearLayoutManager(activity!!)
        view.calendarList.adapter = CalendarAdapter()

        return view
    }

    override fun onDialogCreated(dialog: AlertDialog, contentView: View) {
        dialog.setOnShowListener {
            setPositiveButtonListener {
                dispatch(CalendarPickerAction.SyncSelectedCalendars)
            }
        }
    }

    override fun render(state: CalendarPickerViewState, view: View) {
        when (state.type) {
            CalendarPickerViewState.StateType.CALENDAR_DATA_CHANGED -> {
                changeIcon(state.petHeadImage)
                (view.calendarList.adapter as CalendarAdapter).updateAll(state.calendarViewModels)
            }

            CalendarPickerViewState.StateType.CALENDARS_SELECTED -> {
                dismiss()
                pickedCalendarsListener(state.syncCalendars)
            }

            else -> {
            }
        }
    }

    data class CalendarViewModel(
        override val id: String,
        val name: String,
        @ColorInt val color: Int,
        val isSelected: Boolean
    ) : RecyclerViewViewModel

    inner class CalendarAdapter :
        BaseRecyclerViewAdapter<CalendarViewModel>(R.layout.item_calendar_picker) {

        override fun onBindViewModel(vm: CalendarViewModel, view: View, holder: SimpleViewHolder) {

            view.calendarName.text = vm.name

            view.calendarColor.backgroundTintList = ColorStateList.valueOf(vm.color)

            view.calendarCheckBox.setOnCheckedChangeListener(null)

            view.setOnClickListener {
                view.calendarCheckBox.toggle()
            }

            view.calendarCheckBox.isChecked = vm.isSelected

            view.calendarCheckBox.setOnCheckedChangeListener { _, isChecked ->
                dispatch(CalendarPickerAction.ToggleSelectedCalendar(isChecked, vm.id, vm.name))
            }
        }

    }

    private val CalendarPickerViewState.calendarViewModels
        get() = calendars.map {
            CalendarViewModel(
                id = it.id,
                name = it.name,
                color = it.color,
                isSelected = syncCalendars.map { it.id }.contains(it.id)
            )
        }

    private val CalendarPickerViewState.petHeadImage
        get() = AndroidPetAvatar.valueOf(petAvatar.name).headImage
}