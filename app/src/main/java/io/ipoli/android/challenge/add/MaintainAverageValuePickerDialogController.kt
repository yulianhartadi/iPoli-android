package io.ipoli.android.challenge.add

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.challenge.add.MaintainAverageValueViewState.StateType.*
import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.Validator
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.common.view.ReduxDialogController
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.PetAvatar
import kotlinx.android.synthetic.main.dialog_maintain_average_value_picker.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*
import org.threeten.bp.LocalDate
import java.util.*

sealed class MaintainAverageValueAction : Action {
    data class Select(
        val name: String,
        val targetValue: String,
        val units: String,
        val targetLow: String,
        val targetHigh: String
    ) : MaintainAverageValueAction()

    data class Load(val trackedValue: Challenge.TrackedValue.Average?) :
        MaintainAverageValueAction()
}

object MaintainAverageValueReducer :
    BaseViewStateReducer<MaintainAverageValueViewState>() {

    override val stateKey = key<MaintainAverageValueViewState>()

    override fun reduce(
        state: AppState,
        subState: MaintainAverageValueViewState,
        action: Action
    ) = when (action) {

        is MaintainAverageValueAction.Load -> {

            val trackedValueState = action.trackedValue?.let {
                subState.copy(
                    id = it.id,
                    name = it.name,
                    targetValue = it.targetValue,
                    units = it.units,
                    targetLow = it.lowerBound,
                    targetHigh = it.upperBound
                )
            } ?: subState

            state.dataState.player?.let {
                trackedValueState.copy(
                    type = DATA_LOADED,
                    petAvatar = it.pet.avatar
                )
            } ?: trackedValueState.copy(
                type = LOADING
            )
        }

        is DataLoadedAction.PlayerChanged ->
            subState.copy(
                type = if (subState.type == LOADING) DATA_LOADED else PET_CHANGED,
                petAvatar = action.player.pet.avatar
            )

        is MaintainAverageValueAction.Select -> {
            val errors = Validator.validate(action).check<ValidationError> {
                "name" {
                    given {
                        name.isBlank()
                    } addError ValidationError.EMPTY_NAME
                }
                "target value format" {
                    given {
                        targetValue.toDoubleOrNull() == null || targetValue.toDouble() < 0
                    } addError ValidationError.INCORRECT_VALUE_FORMAT
                }
                "units" {
                    given {
                        units.isBlank()
                    } addError ValidationError.EMPTY_UNITS
                }
                "low value format" {
                    given {
                        targetLow.toDoubleOrNull() == null || targetLow.toDouble() < 0
                    } addError ValidationError.INCORRECT_LOW_VALUE_FORMAT
                }
                "high value format" {
                    given {
                        targetHigh.toDoubleOrNull() == null || targetHigh.toDouble() < 0
                    } addError ValidationError.INCORRECT_HIGH_VALUE_FORMAT
                }
                "low value too high" {
                    given {
                        val lowVal = targetLow.toDoubleOrNull()
                        val targetVal = targetValue.toDoubleOrNull()
                        if (lowVal == null || targetVal == null) {
                            false
                        } else {
                            lowVal > targetVal
                        }
                    } addError ValidationError.LOW_VALUE_TOO_HIGH
                }

                "high value too low" {
                    given {
                        val highVal = targetHigh.toDoubleOrNull()
                        val targetVal = targetValue.toDoubleOrNull()
                        if (highVal == null || targetVal == null) {
                            false
                        } else {
                            highVal < targetVal
                        }
                    } addError ValidationError.HIGH_VALUE_TOO_LOW
                }
            }

            if (errors.isNotEmpty()) {
                subState.copy(
                    type = VALIDATION_ERROR,
                    errors = errors.toSet()
                )
            } else {
                subState.copy(
                    type = TRACKED_VALUE_CHOSEN,
                    trackedValue = Challenge.TrackedValue.Average(
                        id = subState.id ?: UUID.randomUUID().toString(),
                        name = action.name,
                        targetValue = action.targetValue.toDouble(),
                        units = action.units,
                        lowerBound = action.targetLow.toDouble(),
                        upperBound = action.targetHigh.toDouble(),
                        history = emptyMap<LocalDate, Challenge.TrackedValue.Log>().toSortedMap()
                    )
                )
            }
        }


        else -> subState
    }

    override fun defaultState() =
        MaintainAverageValueViewState(
            type = LOADING,
            petAvatar = null,
            id = null,
            name = null,
            targetValue = null,
            units = null,
            targetLow = null,
            targetHigh = null,
            errors = emptySet(),
            trackedValue = null
        )

    enum class ValidationError {
        EMPTY_NAME, INCORRECT_VALUE_FORMAT, EMPTY_UNITS, INCORRECT_LOW_VALUE_FORMAT, INCORRECT_HIGH_VALUE_FORMAT, LOW_VALUE_TOO_HIGH, HIGH_VALUE_TOO_LOW
    }
}

data class MaintainAverageValueViewState(
    val type: StateType,
    val petAvatar: PetAvatar?,
    val id: String?,
    val name: String?,
    val targetValue: Double?,
    val units: String?,
    val targetLow: Double?,
    val targetHigh: Double?,
    val errors: Set<MaintainAverageValueReducer.ValidationError>,
    val trackedValue: Challenge.TrackedValue.Average?
) : BaseViewState() {
    enum class StateType {
        LOADING, DATA_LOADED, PET_CHANGED, VALIDATION_ERROR, TRACKED_VALUE_CHOSEN
    }
}

class MaintainAverageValuePickerDialogController(args: Bundle? = null) :
    ReduxDialogController<MaintainAverageValueAction, MaintainAverageValueViewState, MaintainAverageValueReducer>(
        args
    ) {

    private var trackedValueSelectedListener: (Challenge.TrackedValue.Average) -> Unit = {}

    private var cancelListener: () -> Unit = {}

    private var editTrackedValue: Challenge.TrackedValue.Average? = null

    override val reducer = MaintainAverageValueReducer

    constructor(
        trackedValueSelectedListener: (Challenge.TrackedValue.Average) -> Unit,
        cancelListener: () -> Unit = {},
        trackedValue: Challenge.TrackedValue.Average? = null
    ) : this() {
        this.trackedValueSelectedListener = trackedValueSelectedListener
        this.cancelListener = cancelListener
        this.editTrackedValue = trackedValue
    }

    @SuppressLint("InflateParams")
    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View =
        inflater.inflate(R.layout.dialog_maintain_average_value_picker, null)

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setPositiveButton(R.string.track_value, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.dialog_average_value_picker_title)
    }

    override fun onCreateLoadAction() = MaintainAverageValueAction.Load(editTrackedValue)

    override fun onDialogCreated(dialog: AlertDialog, contentView: View) {
        dialog.setOnShowListener {
            setPositiveButtonListener {
                dispatch(
                    MaintainAverageValueAction.Select(
                        name = contentView.valueName.text.toString(),
                        targetValue = contentView.valueReach.text.toString(),
                        units = contentView.valueUnit.text.toString(),
                        targetLow = contentView.valueLow.text.toString(),
                        targetHigh = contentView.valueHigh.text.toString()
                    )
                )
            }

            setNeutralButtonListener {
                cancelListener()
                dismiss()
            }
        }
    }

    override fun render(state: MaintainAverageValueViewState, view: View) {
        when (state.type) {

            DATA_LOADED -> {
                view.dialogContainer.requestFocus()
                state.petAvatar?.let {
                    changeIcon(AndroidPetAvatar.valueOf(it.name).headImage)
                }

                state.name?.let {
                    view.valueName.setText(it)
                    view.valueReach.setText(Constants.DECIMAL_FORMATTER.format(state.targetValue!!))
                    view.valueUnit.setText(state.units!!)
                    view.valueLow.setText(Constants.DECIMAL_FORMATTER.format(state.targetLow!!))
                    view.valueHigh.setText(Constants.DECIMAL_FORMATTER.format(state.targetHigh!!))
                }
            }

            PET_CHANGED ->
                changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)

            VALIDATION_ERROR ->
                state.errors.forEach {
                    when (it) {
                        MaintainAverageValueReducer.ValidationError.EMPTY_NAME ->
                            view.valueName.error = "Name is required"

                        MaintainAverageValueReducer.ValidationError.INCORRECT_VALUE_FORMAT ->
                            view.valueReach.error = "Set average number"

                        MaintainAverageValueReducer.ValidationError.EMPTY_UNITS ->
                            view.valueUnit.error = "Units are required"

                        MaintainAverageValueReducer.ValidationError.INCORRECT_LOW_VALUE_FORMAT ->
                            view.valueLow.error = "Set minimum good number"

                        MaintainAverageValueReducer.ValidationError.INCORRECT_HIGH_VALUE_FORMAT ->
                            view.valueHigh.error = "Set maximum good number"

                        MaintainAverageValueReducer.ValidationError.LOW_VALUE_TOO_HIGH ->
                            view.valueLow.error = "Can't be higher than average value"

                        MaintainAverageValueReducer.ValidationError.HIGH_VALUE_TOO_LOW ->
                            view.valueHigh.error = "Can't be lower than average value"
                    }
                }

            TRACKED_VALUE_CHOSEN -> {
                trackedValueSelectedListener(state.trackedValue!!)
                dismiss()
            }


            else -> {
            }
        }
    }
}