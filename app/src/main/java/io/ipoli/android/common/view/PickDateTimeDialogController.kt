package io.ipoli.android.common.view

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.R
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.LoadPetDialogAction
import io.ipoli.android.pet.PetDialogReducer
import io.ipoli.android.pet.PetDialogViewState
import kotlinx.android.synthetic.main.dialog_date_time_picker.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*

class PickDateTimeDialogController(args: Bundle? = null) :
    ReduxDialogController<LoadPetDialogAction, PetDialogViewState, PetDialogReducer>(args) {
    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog {
        return dialogBuilder.create()
    }

    override val reducer = PetDialogReducer

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val contentView = inflater.inflate(R.layout.dialog_date_time_picker, null)
        contentView.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                if(tab.position == 0) {
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

    override fun render(state: PetDialogViewState, view: View) {
        if (state.type == PetDialogViewState.Type.PET_LOADED) {
            changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
        }
    }
}