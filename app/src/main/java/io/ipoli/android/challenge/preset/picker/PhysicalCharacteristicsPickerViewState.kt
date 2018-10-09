package io.ipoli.android.challenge.preset.picker

import io.ipoli.android.challenge.preset.picker.PhysicalCharacteristicsPickerViewState.StateType.*
import io.ipoli.android.challenge.preset.picker.PhysicalCharacteristicsPickerViewState.ValidationError
import io.ipoli.android.challenge.usecase.CreateChallengeFromPresetUseCase.PhysicalCharacteristics
import io.ipoli.android.challenge.usecase.CreateChallengeFromPresetUseCase.PhysicalCharacteristics.Gender
import io.ipoli.android.challenge.usecase.CreateChallengeFromPresetUseCase.PhysicalCharacteristics.Units
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.Validator
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.pet.PetAvatar

sealed class PhysicalCharacteristicsPickerAction : Action {
    object Load : PhysicalCharacteristicsPickerAction()

    object ToggleUnits : PhysicalCharacteristicsPickerAction()
    data class Done(
        val currentWeight: String,
        val targetWeight: String,
        val heightCm: Int,
        val heightFeet: Int,
        val heightInches: Int
    ) : PhysicalCharacteristicsPickerAction()

    data class ChangeGender(val gender: PhysicalCharacteristics.Gender) :
        PhysicalCharacteristicsPickerAction()
}

object PhysicalCharacteristicsPickerReducer :
    BaseViewStateReducer<PhysicalCharacteristicsPickerViewState>() {

    override val stateKey = key<PhysicalCharacteristicsPickerViewState>()

    override fun reduce(
        state: AppState,
        subState: PhysicalCharacteristicsPickerViewState,
        action: Action
    ) =
        when (action) {

            is PhysicalCharacteristicsPickerAction.Load ->
                state.dataState.player?.let {
                    subState.copy(
                        type = DATA_LOADED,
                        petAvatar = it.pet.avatar
                    )
                } ?: subState.copy(type = LOADING)

            is DataLoadedAction.PlayerChanged ->
                subState.copy(
                    type = DATA_LOADED,
                    petAvatar = action.player.pet.avatar
                )

            is PhysicalCharacteristicsPickerAction.ToggleUnits ->
                subState.copy(
                    type = UNITS_CHANGED,
                    units = if (subState.units == Units.IMPERIAL) Units.METRIC else Units.IMPERIAL
                )

            is PhysicalCharacteristicsPickerAction.Done -> {

                val errors = Validator.validate(action).check<ValidationError> {
                    "gender" {
                        given { subState.gender == null } addError ValidationError.NO_GENDER_SELECTED
                    }
                    "currentWeight" {
                        given {
                            action.currentWeight.isBlank() || action.currentWeight.toIntOrNull() == null
                        } addError ValidationError.EMPTY_CURRENT_WEIGHT
                    }
                    "targetWeight" {
                        given {
                            action.targetWeight.isBlank() || action.targetWeight.toIntOrNull() == null
                        } addError ValidationError.EMPTY_TARGET_WEIGHT
                    }
                }

                if (errors.isEmpty()) {
                    subState.copy(
                        type = PhysicalCharacteristicsPickerViewState.StateType.CHARACTERISTICS_PICKED,
                        weight = action.currentWeight.toInt(),
                        targetWeight = action.targetWeight.toInt()
                    )
                } else {
                    subState.copy(
                        type = VALIDATION_ERROR,
                        errors = errors.toSet()
                    )
                }
            }

            is PhysicalCharacteristicsPickerAction.ChangeGender ->
                subState.copy(
                    type = GENDER_CHANGED,
                    gender = action.gender
                )

            else -> subState
        }

    override fun defaultState() =
        PhysicalCharacteristicsPickerViewState(
            type = LOADING,
            petAvatar = PetAvatar.BEAR,
            units = Units.IMPERIAL,
            gender = null,
            weight = null,
            targetWeight = null,
            errors = emptySet()
        )
}

data class PhysicalCharacteristicsPickerViewState(
    val type: StateType,
    val petAvatar: PetAvatar,
    val units: Units,
    val gender: Gender?,
    val weight: Int?,
    val targetWeight: Int?,
    val errors: Set<ValidationError>
) : BaseViewState() {
    enum class StateType {
        LOADING,
        DATA_LOADED,
        GENDER_CHANGED,
        UNITS_CHANGED,
        VALIDATION_ERROR,
        CHARACTERISTICS_PICKED
    }

    enum class ValidationError {
        EMPTY_CURRENT_WEIGHT, EMPTY_TARGET_WEIGHT, NO_GENDER_SELECTED
    }
}