package io.ipoli.android.challenge.add

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.challenge.add.TargetValuePickerViewState.StateType.*
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
import kotlinx.android.synthetic.main.dialog_target_value_picker.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*
import org.threeten.bp.LocalDate
import java.util.*

sealed class TargetValuePickerAction : Action {
    data class Load(val trackedValue: Challenge.TrackedValue.Target?) : TargetValuePickerAction()
    data class Select(
        val name: String,
        val targetValue: String,
        val units: String,
        val startValue: String,
        val accumulateValues: Boolean
    ) : TargetValuePickerAction()
}

object TargetValuePickerReducer : BaseViewStateReducer<TargetValuePickerViewState>() {

    override fun reduce(
        state: AppState,
        subState: TargetValuePickerViewState,
        action: Action
    ) = when (action) {

        is TargetValuePickerAction.Load -> {
            val trackedValueState = action.trackedValue?.let {
                subState.copy(
                    id = it.id,
                    name = it.name,
                    targetValue = it.targetValue,
                    units = it.units,
                    startValue = it.startValue,
                    isCumulative = it.isCumulative,
                    allowChangeCumulative = false
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

        is TargetValuePickerAction.Select -> {
            val errors = Validator.validate(action).check<ValidationError> {
                "name" {
                    given {
                        name.isBlank()
                    } addError ValidationError.EMPTY_NAME
                }
                "value format" {
                    given {
                        targetValue.toDoubleOrNull() == null || targetValue.toDouble() < 0
                    } addError ValidationError.INCORRECT_VALUE_FORMAT
                }
                "units" {
                    given {
                        units.isBlank()
                    } addError ValidationError.EMPTY_UNITS
                }
                "starting value format" {
                    given {
                        startValue.toDoubleOrNull() == null || startValue.toDouble() < 0
                    } addError ValidationError.INCORRECT_START_VALUE_FORMAT
                }
                "accumulate value" {
                    given {
                        val startVal = startValue.toDoubleOrNull()
                        val targetVal = targetValue.toDoubleOrNull()
                        if (!accumulateValues || startVal == null || targetVal == null) {
                            false
                        } else {
                            startVal >= targetVal
                        }
                    } addError ValidationError.ACCUMULATE_TARGET_BEFORE_START
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
                    trackedValue = Challenge.TrackedValue.Target(
                        id = subState.id ?: UUID.randomUUID().toString(),
                        name = action.name,
                        units = action.units,
                        startValue = action.startValue.toDouble(),
                        targetValue = action.targetValue.toDouble(),
                        currentValue = 0.0,
                        remainingValue = 0.0,
                        isCumulative = if (subState.allowChangeCumulative) action.accumulateValues else subState.isCumulative,
                        history = emptyMap<LocalDate, Challenge.TrackedValue.Log>().toSortedMap()
                    )
                )
            }
        }


        else -> subState
    }

    override fun defaultState() =
        TargetValuePickerViewState(
            type = LOADING,
            petAvatar = null,
            id = null,
            name = null,
            targetValue = null,
            units = null,
            startValue = null,
            isCumulative = false,
            allowChangeCumulative = true,
            errors = emptySet(),
            trackedValue = null
        )

    override val stateKey = key<TargetValuePickerViewState>()

    enum class ValidationError {
        EMPTY_NAME, INCORRECT_VALUE_FORMAT, EMPTY_UNITS, INCORRECT_START_VALUE_FORMAT, ACCUMULATE_TARGET_BEFORE_START
    }
}

data class TargetValuePickerViewState(
    val type: StateType,
    val petAvatar: PetAvatar?,
    val id: String?,
    val name: String?,
    val targetValue: Double?,
    val units: String?,
    val startValue: Double?,
    val isCumulative: Boolean,
    val allowChangeCumulative: Boolean,
    val errors: Set<TargetValuePickerReducer.ValidationError>,
    val trackedValue: Challenge.TrackedValue.Target?
) : BaseViewState() {
    enum class StateType {
        LOADING, DATA_LOADED, PET_CHANGED, VALIDATION_ERROR, TRACKED_VALUE_CHOSEN
    }
}

open class TextWatcherAdapter(private val afterTextChangedCallback: (Editable) -> Unit = {}) :
    TextWatcher {

    override fun afterTextChanged(s: Editable) {
        afterTextChangedCallback(s)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }

}

class TargetValuePickerDialogController(args: Bundle? = null) :
    ReduxDialogController<TargetValuePickerAction, TargetValuePickerViewState, TargetValuePickerReducer>(
        args
    ) {

    private var trackedValueSelectedListener: (Challenge.TrackedValue.Target) -> Unit = {}

    private var cancelListener: () -> Unit = {}

    private var editTrackedValue: Challenge.TrackedValue.Target? = null

    override val reducer = TargetValuePickerReducer

    constructor(
        trackedValueSelectedListener: (Challenge.TrackedValue.Target) -> Unit,
        cancelListener: () -> Unit = {},
        trackedValue: Challenge.TrackedValue.Target? = null
    ) : this() {
        this.trackedValueSelectedListener = trackedValueSelectedListener
        this.cancelListener = cancelListener
        this.editTrackedValue = trackedValue
    }

    @SuppressLint("InflateParams")
    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View =
        inflater.inflate(R.layout.dialog_target_value_picker, null)

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
        headerView.dialogHeaderTitle.setText(R.string.dialog_target_value_picker_title)
    }

    override fun onCreateLoadAction() = TargetValuePickerAction.Load(editTrackedValue)

    override fun onDialogCreated(dialog: AlertDialog, contentView: View) {
        dialog.setOnShowListener {
            setPositiveButtonListener {
                dispatch(
                    TargetValuePickerAction.Select(
                        name = contentView.valueName.text.toString(),
                        targetValue = contentView.valueReach.text.toString(),
                        units = contentView.valueUnit.text.toString(),
                        startValue = contentView.valueStart.text.toString(),
                        accumulateValues = contentView.valueCumulativeSwitch.isChecked
                    )
                )
            }

            setNeutralButtonListener {
                cancelListener()
                dismiss()
            }
        }
    }

    override fun render(state: TargetValuePickerViewState, view: View) {
        when (state.type) {

            DATA_LOADED -> {
                view.dialogContainer.requestFocus()
                state.petAvatar?.let {
                    changeIcon(AndroidPetAvatar.valueOf(it.name).headImage)
                }
                state.name?.let {
                    view.valueName.setText(it)
                    view.valueReach.setText(
                        Constants.DECIMAL_FORMATTER.format(state.targetValue!!)
                    )
                    view.valueUnit.setText(state.units!!)
                    view.valueStart.setText(Constants.DECIMAL_FORMATTER.format(state.startValue!!))
                    view.valueCumulativeSwitch.isChecked = state.isCumulative
                }

                view.valueCumulativeSwitch.isClickable = state.allowChangeCumulative
            }

            PET_CHANGED ->
                changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)

            VALIDATION_ERROR ->
                state.errors.forEach {
                    when (it) {
                        TargetValuePickerReducer.ValidationError.EMPTY_NAME ->
                            view.valueName.error = "Name is required"

                        TargetValuePickerReducer.ValidationError.INCORRECT_VALUE_FORMAT ->
                            view.valueReach.error = "Set target number"

                        TargetValuePickerReducer.ValidationError.EMPTY_UNITS ->
                            view.valueUnit.error = "Units are required"

                        TargetValuePickerReducer.ValidationError.INCORRECT_START_VALUE_FORMAT ->
                            view.valueStart.error = "Set start number"

                        TargetValuePickerReducer.ValidationError.ACCUMULATE_TARGET_BEFORE_START ->
                            view.valueReach.error = "Target value can't be smaller than start value"
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