package io.ipoli.android.reward.edit

import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.reward.list.RewardViewModel
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
        return Observable.just(EditRewardViewState.Created())
    }
}

interface EditRewardViewState {

    enum class ValidationError {
        EMPTY_NAME
    }

    class Created : EditRewardViewState

    data class Invalid(val validationErrors: List<ValidationError>) : EditRewardViewState
}

data class Parameters(val reward: EditRewardViewModel)