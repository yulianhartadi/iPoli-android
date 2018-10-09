package io.ipoli.android.challenge.preset.picker

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.R
import io.ipoli.android.challenge.preset.picker.PhysicalCharacteristicsPickerViewState.StateType.*
import io.ipoli.android.challenge.preset.picker.PhysicalCharacteristicsPickerViewState.ValidationError
import io.ipoli.android.challenge.usecase.CreateChallengeFromPresetUseCase
import io.ipoli.android.challenge.usecase.CreateChallengeFromPresetUseCase.PhysicalCharacteristics.Gender
import io.ipoli.android.challenge.usecase.CreateChallengeFromPresetUseCase.PhysicalCharacteristics.Units.IMPERIAL
import io.ipoli.android.common.view.*
import io.ipoli.android.pet.AndroidPetAvatar
import kotlinx.android.synthetic.main.dialog_physical_characteristics_picker.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*


class PhysicalCharacteristicsPickerDialogController(args: Bundle? = null) :
    ReduxDialogController<PhysicalCharacteristicsPickerAction, PhysicalCharacteristicsPickerViewState, PhysicalCharacteristicsPickerReducer>(
        args
    ) {

    override val reducer = PhysicalCharacteristicsPickerReducer

    private var listener: (CreateChallengeFromPresetUseCase.PhysicalCharacteristics?) -> Unit = {}

    constructor(listener: (CreateChallengeFromPresetUseCase.PhysicalCharacteristics?) -> Unit) : this() {
        this.listener = listener
    }

    override fun onCreateLoadAction() = PhysicalCharacteristicsPickerAction.Load

    @SuppressLint("InflateParams")
    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val v = inflater.inflate(R.layout.dialog_physical_characteristics_picker, null)
        v.presetHeightFeet.setSelection(3)

        v.presetGender.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.presetGenderFemale) {
                dispatch(PhysicalCharacteristicsPickerAction.ChangeGender(Gender.FEMALE))
            } else {
                dispatch(PhysicalCharacteristicsPickerAction.ChangeGender(Gender.MALE))
            }
        }

        v.explanationIcon.setImageDrawable(
            IconicsDrawable(activity!!)
                .icon(GoogleMaterial.Icon.gmd_info_outline)
                .color(attrData(R.attr.colorAccent))
                .sizeDp(24)
        )

        v.presetHeightCm.adapter = ArrayAdapter<String>(
            v.context,
            android.R.layout.simple_spinner_item,
            (55..250).map { it.toString() }
        )
        v.presetHeightCm.setSelection(110)

        return v
    }

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.physical_characteristics_picker_title)
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setPositiveButton(R.string.done, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

    override fun onDialogCreated(dialog: AlertDialog, contentView: View) {
        dialog.setOnShowListener {
            setPositiveButtonListener {
                dispatch(
                    PhysicalCharacteristicsPickerAction.Done(
                        currentWeight = contentView.presetWeight.text.toString(),
                        targetWeight = contentView.presetTargetWeight.text.toString(),
                        heightCm = contentView.presetHeightCm.selectedItem.toString().toInt(),
                        heightFeet = contentView.presetHeightFeet.selectedItem.toString().toInt(),
                        heightInches = contentView.presetHeightInches.selectedItem.toString().toInt()
                    )
                )
            }

            setNeutralButtonListener {
                listener(null)
                dismiss()
            }
        }
    }

    override fun render(state: PhysicalCharacteristicsPickerViewState, view: View) {
        when (state.type) {

            DATA_LOADED -> {
                changeIcon(state.petHeadImage)
                renderUnits(view, state)
            }

            UNITS_CHANGED ->
                renderUnits(view, state)

            CHARACTERISTICS_PICKED -> {
                listener(
                    CreateChallengeFromPresetUseCase.PhysicalCharacteristics(
                        units = state.units,
                        gender = state.gender!!,
                        weight = state.weight!!,
                        targetWeight = state.targetWeight!!
                    )
                )
                dismiss()
            }

            VALIDATION_ERROR -> {
                val errors = state.errors
                if (errors.contains(ValidationError.EMPTY_CURRENT_WEIGHT)) {
                    view.presetWeight.error = stringRes(R.string.validation_empty_current_weight)
                }

                if (errors.contains(ValidationError.EMPTY_TARGET_WEIGHT)) {
                    view.presetTargetWeight.error =
                        stringRes(R.string.validation_empty_target_weight)
                }

                if (errors.contains(ValidationError.NO_GENDER_SELECTED)) {
                    showShortToast(R.string.validation_empty_gender)
                }
            }

            else -> {
            }
        }
    }

    private fun renderUnits(
        view: View,
        state: PhysicalCharacteristicsPickerViewState
    ) {
        view.presetMeasurementSystem.setOnCheckedChangeListener(null)
        val isImperial = state.units == IMPERIAL
        view.presetMeasurementSystem.isChecked = isImperial
        view.presetMeasurementSystem.setOnCheckedChangeListener { _, _ ->
            dispatch(PhysicalCharacteristicsPickerAction.ToggleUnits)
        }

        if (isImperial) {
            view.presetWeightInputLayout.hint = stringRes(R.string.current_weight_lbs)
            view.presetTargetWeightInputLayout.hint = stringRes(R.string.target_weight_lbs)
        } else {
            view.presetWeightInputLayout.hint = stringRes(R.string.current_weight_kgs)
            view.presetTargetWeightInputLayout.hint = stringRes(R.string.target_weight_kgs)
        }

        if (isImperial) {
            view.presetHeightFeet.visible()
            view.presetHeightFeetLabel.visible()
            view.presetHeightInches.visible()
            view.presetHeightInchesLabel.visible()
            view.presetHeightCm.invisible()
            view.presetHeightCmLabel.invisible()
        } else {
            view.presetHeightFeet.gone()
            view.presetHeightFeetLabel.gone()
            view.presetHeightInches.gone()
            view.presetHeightInchesLabel.gone()
            view.presetHeightCm.visible()
            view.presetHeightCmLabel.visible()
        }
    }

    private val PhysicalCharacteristicsPickerViewState.petHeadImage
        get() = AndroidPetAvatar.valueOf(petAvatar.name).headImage
}