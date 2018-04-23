package io.ipoli.android.common.view

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.text.DurationFormatter
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.LoadPetDialogAction
import io.ipoli.android.pet.PetDialogReducer
import io.ipoli.android.pet.PetDialogViewState
import kotlinx.android.synthetic.main.dialog_duration_picker.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 9/2/17.
 */
class PickDurationDialogController :
    ReduxDialogController<LoadPetDialogAction, PetDialogViewState, PetDialogReducer> {

    override val reducer = PetDialogReducer

    private var listener: (Int) -> Unit = {}
    private var selectedDuration: Int? = null

    constructor(selectedDuration: Int? = null, listener: (Int) -> Unit) : this() {
        this.listener = listener
        this.selectedDuration = selectedDuration
    }

    constructor(args: Bundle? = null) : super(args)


    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val contentView = inflater.inflate(R.layout.dialog_duration_picker, null)

        val durationPicker = contentView.durationPicker
        durationPicker.setItems(
            Constants.DURATIONS
                .map {
                    DurationFormatter.formatReadable(
                        contentView.context,
                        it
                    )
                })

        val selectedIndex =
            Constants.DURATIONS.indexOfFirst { it == selectedDuration ?: Constants.DEFAULT_QUEST_DURATION }
        durationPicker.setSelectedItem(selectedIndex)

        return contentView
    }

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.quest_duration_question)
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder.setPositiveButton(R.string.dialog_ok, { _, _ ->
            listener(Constants.DURATIONS[contentView.durationPicker.selectedItemIndex])
        })
            .setNegativeButton(R.string.cancel, null)
            .create()

    override fun onCreateLoadAction() = LoadPetDialogAction

    override fun render(state: PetDialogViewState, view: View) {
        if (state.type == PetDialogViewState.Type.PET_LOADED) {
            changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
        }
    }
}