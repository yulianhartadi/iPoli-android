package io.ipoli.android.reward.edit

import io.ipoli.android.common.BaseRxUseCase
import io.reactivex.Observable

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/29/17.
 */
class EditRewardUseCase : BaseRxUseCase<Parameters, EditRewardViewState>() {
    override fun createObservable(parameters: Parameters): Observable<EditRewardViewState> {
        val reward = parameters.reward
        if (reward.name.isEmpty()) {
            return Observable.just(EditRewardViewState.Invalid(listOf(EditRewardViewState.ValidationError.EMPTY_NAME)))
        }
        if (reward.name.length > 50) {
            return Observable.just(EditRewardViewState.Invalid(listOf(EditRewardViewState.ValidationError.NAME_TOO_LONG)))
        }
        return Observable.just(EditRewardViewState.Created)
    }
}

sealed class EditRewardViewState {

    enum class ValidationError {
        EMPTY_NAME,
        NAME_TOO_LONG
    }

    object Created : EditRewardViewState()

    data class Invalid(val validationErrors: List<ValidationError>) : EditRewardViewState()
}

data class Parameters(val reward: EditRewardViewModel)