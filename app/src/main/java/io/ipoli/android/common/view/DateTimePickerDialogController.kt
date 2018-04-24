package io.ipoli.android.common.view

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.R
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.LoadPetDialogAction
import io.ipoli.android.pet.PetDialogReducer
import io.ipoli.android.pet.PetDialogViewState
import kotlinx.android.synthetic.main.dialog_date_time_picker.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*
import org.threeten.bp.LocalDate

class DateTimePickerDialogController(args: Bundle? = null) :
    ReduxDialogController<LoadPetDialogAction, PetDialogViewState, PetDialogReducer>(args) {

    private var listener: (LocalDate?, Time?) -> Unit = { _, _ -> }

    override val reducer = PetDialogReducer

    constructor(listener: (LocalDate?, Time?) -> Unit) : this() {
        this.listener = listener
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setPositiveButton(R.string.dialog_ok, { _, _ ->
                val datePicker = contentView.datePicker
                val timePicker = contentView.timePicker
                val date =
                    LocalDate.of(datePicker.year, datePicker.month + 1, datePicker.dayOfMonth)
                val time = Time.at(timePicker.currentHour, timePicker.currentMinute)
                listener(date, time)
            })
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton(R.string.do_not_know, { _, _ ->
                listener(null, null)
            })
            .create()

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val contentView = inflater.inflate(R.layout.dialog_date_time_picker, null)
        contentView.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    contentView.datePicker.visible()
                    contentView.timePicker.gone()
                } else {
                    contentView.datePicker.gone()
                    contentView.timePicker.visible()
                }
            }

        })
        return contentView
    }

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.date_time_picker_title)
    }

    override fun onCreateLoadAction() = LoadPetDialogAction

    override fun render(state: PetDialogViewState, view: View) {
        if (state.type == PetDialogViewState.Type.PET_LOADED) {
            changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
        }
    }
}