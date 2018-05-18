package io.ipoli.android.settings.view

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.R
import io.ipoli.android.common.view.ReduxDialogController
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.LoadPetDialogAction
import io.ipoli.android.pet.PetDialogReducer
import io.ipoli.android.pet.PetDialogViewState
import kotlinx.android.synthetic.main.view_dialog_header.view.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.format.TextStyle
import java.util.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/16/18.
 */
class DaysPickerDialogController(args: Bundle? = null) :
    ReduxDialogController<LoadPetDialogAction, PetDialogViewState, PetDialogReducer>(args) {

    override val reducer = PetDialogReducer

    private val days: List<DayOfWeek> = DayOfWeek.values().toList()

    private var listener: (Set<DayOfWeek>) -> Unit = {}

    private lateinit var selectedDays: MutableSet<DayOfWeek>

    constructor(
        selectedDays: Set<DayOfWeek>,
        listener: (Set<DayOfWeek>) -> Unit
    ) : this() {
        this.listener = listener
        this.selectedDays = selectedDays.toMutableSet()
    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_days_picker, null)
    }

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.choose_days_of_week)
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog {
        val daysOfWeekNames = days.map { it.getDisplayName(TextStyle.FULL, Locale.getDefault()) }
        val checked = days.map { selectedDays.contains(it) }

        return dialogBuilder
            .setMultiChoiceItems(
                daysOfWeekNames.toTypedArray(),
                checked.toBooleanArray(),
                { _, which, isChecked ->
                    if (isChecked) {
                        selectedDays.add(days[which])
                    } else {
                        selectedDays.remove(days[which])
                    }
                })
            .setPositiveButton(R.string.dialog_ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    override fun onDialogCreated(dialog: AlertDialog, contentView: View) {
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                listener(selectedDays)
                dismiss()
            }
        }
    }

    override fun onCreateLoadAction() = LoadPetDialogAction

    override fun render(state: PetDialogViewState, view: View) {
        if (state.type == PetDialogViewState.Type.PET_LOADED) {
            changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
        }
    }
}