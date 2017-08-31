package io.ipoli.android.reward.edit

import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.common.Validator
import io.ipoli.android.common.jobservice.JobQueue
import io.ipoli.android.reward.edit.EditRewardViewState.ValidationError.*
import io.reactivex.Observable

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/29/17.
 */
class EditRewardUseCase(private val jobQueue: JobQueue, private val rewardId: String = "") : BaseRxUseCase<Parameters, EditRewardViewState>() {

    override fun createObservable(parameters: Parameters): Observable<EditRewardViewState> {
        val reward = parameters.reward

        val validationErrors = Validator<EditRewardViewModel, EditRewardViewState.ValidationError> {
            "name"{
                not { name.isEmpty() } error EMPTY_NAME
                not { name.length > 50 } error NAME_TOO_LONG
            }
            "description" {
                not { description.length > 100 } error DESCRIPTION_TOO_LONG
            }
            "price" {
                not { price < 0 } error NEGATIVE_PRICE
            }
        }.validate(reward)

        return if (validationErrors.isNotEmpty()) {
            Observable.just(EditRewardViewState.Invalid(validationErrors))
        } else if (rewardId.isNotEmpty()) {
            Observable.just<EditRewardViewState>(EditRewardViewState.Updated)
        } else {
            Observable.just<EditRewardViewState>(EditRewardViewState.Added)
        }
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