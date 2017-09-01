package io.ipoli.android.reward.edit

import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.common.jobservice.JobQueue
import io.ipoli.android.common.validate
import io.ipoli.android.reward.Reward
import io.ipoli.android.reward.edit.EditRewardViewState.ValidationError.*
import io.reactivex.Observable

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/29/17.
 */
class EditRewardUseCase(private val jobQueue: JobQueue, private val rewardId: String = "") : BaseRxUseCase<Parameters, EditRewardViewState>() {

    override fun createObservable(parameters: Parameters): Observable<EditRewardViewState> {
        val reward = parameters.reward

        val validationErrors = reward.validate<EditRewardViewModel, EditRewardViewState.ValidationError> {
            "name" {
                When { name.isEmpty() } error EMPTY_NAME
                When { name.length > 50 } error NAME_TOO_LONG
            }
            "description" {
                When { description.length > 100 } error DESCRIPTION_TOO_LONG
            }
            "price" {
                When { price < 0 } error NEGATIVE_PRICE
            }
        }
        if (validationErrors.isEmpty()) {
            jobQueue.save(
                Reward(rewardId,
                    reward.name,
                    reward.description,
                    reward.price)
            )
        }
        return when {
            validationErrors.isNotEmpty() -> Observable.just(EditRewardViewState.Invalid(validationErrors))
            rewardId.isNotEmpty() -> Observable.just<EditRewardViewState>(EditRewardViewState.Updated)
            else -> Observable.just<EditRewardViewState>(EditRewardViewState.Added)
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