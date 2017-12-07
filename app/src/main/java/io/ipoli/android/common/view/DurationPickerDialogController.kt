package io.ipoli.android.common.view

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.text.DurationFormatter.formatShort
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.LoadPetIntent
import io.ipoli.android.pet.PetDialogPresenter
import io.ipoli.android.pet.PetDialogViewState
import kotlinx.android.synthetic.main.dialog_duration_picker.view.*
import space.traversal.kapsule.Injects
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */
class DurationPickerDialogController :
    MviDialogController<PetDialogViewState, DurationPickerDialogController, PetDialogPresenter, LoadPetIntent>
    , ViewStateRenderer<PetDialogViewState>, Injects<ControllerModule> {

    interface DurationPickedListener {
        fun onDurationPicked(minutes: Int)
    }

    private var listener: DurationPickedListener? = null
    private var selectedDuration: Int? = null
    private var currentMinutes = Constants.QUEST_MIN_DURATION

    private val presenter by required { petDialogPresenter }

    constructor(listener: DurationPickedListener, selectedDuration: Int? = null) : this() {
        this.listener = listener
        this.selectedDuration = selectedDuration
    }

    constructor(args: Bundle? = null) : super(args)

    override fun createPresenter() = presenter

    override fun onCreateDialog(savedViewState: Bundle?): DialogView {

        val inflater = LayoutInflater.from(activity!!)

        val contentView = inflater.inflate(R.layout.dialog_duration_picker, null)

        val durationPicker = contentView.durationPicker
        durationPicker.labelSize = ViewUtils.spToPx(14, contentView.context)
        durationPicker.max = 25
        durationPicker.setOnProgressChangedListener { progress ->
            currentMinutes = updateMinutes(progress)
            durationPicker.label = formatShort(currentMinutes)
        }

        val dialog = AlertDialog.Builder(activity!!)
            .setView(contentView)
            .setTitle(R.string.quest_duration_question)
            .setPositiveButton(R.string.dialog_ok, { _, _ ->
                listener!!.onDurationPicked(currentMinutes)
            })
            .setNegativeButton(R.string.cancel, null)
            .create()

        return DialogView(dialog, contentView)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        send(LoadPetIntent)
    }

    private fun updateMinutes(progress: Int) =
        when {
            progress <= 11 -> progress * 5 + 5
            progress <= 17 -> 60 + (progress % 11) * 10
            else -> 120 + (progress % 17) * 15
        }

    override fun render(state: PetDialogViewState, view: View) {
        if (state.type == PetDialogViewState.Type.PET_LOADED) {
            changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
        }
    }
}