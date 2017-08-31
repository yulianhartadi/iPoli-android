package io.ipoli.android.reward.edit

import io.ipoli.android.common.BaseRxUseCase
import io.reactivex.Observable

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/29/17.
 */
class EditRewardUseCase(private val rewardId: String = "") : BaseRxUseCase<Parameters, EditRewardViewState>() {
    override fun createObservable(parameters: Parameters): Observable<EditRewardViewState> {
        val reward = parameters.reward

        val validationErrors = mutableListOf<EditRewardViewState.ValidationError>()

        validationErrors.addIfNotNull(validateNotEmpty(reward.name,
            EditRewardViewState.ValidationError.EMPTY_NAME))

        validationErrors.addIfNotNull(validateLength(reward.name,
            50,
            EditRewardViewState.ValidationError.NAME_TOO_LONG))

        validationErrors.addIfNotNull(validateLength(reward.description,
            100,
            EditRewardViewState.ValidationError.DESCRIPTION_TOO_LONG))

        validationErrors.addIfNotNull(validateNonNegative(reward.price,
            EditRewardViewState.ValidationError.NEGATIVE_PRICE))

        if (validationErrors.isNotEmpty()) {
            return Observable.just(EditRewardViewState.Invalid(validationErrors))
        }

        return if (rewardId.isNotEmpty()) {
            Observable.just(EditRewardViewState.Updated)
        } else {
            Observable.just(EditRewardViewState.Added)
        }
    }

    private fun validateNonNegative(number: Int, error: EditRewardViewState.ValidationError): EditRewardViewState.ValidationError? =
        if (number < 0) error else null

    private fun validateLength(text: String, maxLength: Int, error: EditRewardViewState.ValidationError): EditRewardViewState.ValidationError? =
        if (text.length > maxLength) error else null

    private fun validateNotEmpty(text: String, error: EditRewardViewState.ValidationError): EditRewardViewState.ValidationError? =
        if (text.isEmpty()) error else null
}

private fun <E> MutableList<E>.addIfNotNull(element: E?) {
    if (element != null) {
        add(element)
    }
}

sealed class EditRewardViewState {

    enum class ValidationError {
        EMPTY_NAME,
        NAME_TOO_LONG,
        DESCRIPTION_TOO_LONG,
        NEGATIVE_PRICE
    }

    object Added : EditRewardViewState()
    object Updated : EditRewardViewState()

    data class Invalid(val validationErrors: List<ValidationError>) : EditRewardViewState()
}

data class Parameters(val reward: EditRewardViewModel)