package io.ipoli.android.reward.list.usecase

import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.reward.list.RewardListPartialChange
import io.ipoli.android.reward.list.RewardViewModel
import io.reactivex.Observable

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/27/17.
 */
class RemoveRewardFromListUseCase : BaseRxUseCase<RemoveRewardFromListUseCase.Parameters, RewardListPartialChange>() {
    override fun createObservable(parameters: Parameters): Observable<RewardListPartialChange> {
        return Observable.defer {
            val newList = parameters.rewards.filter { it != parameters.rewardToDelete }
            if (newList.isEmpty()) {
                Observable.just(RewardListPartialChange.Empty())
            } else {
                Observable.just(RewardListPartialChange.RewardRemoved(newList))
            }
        }.onErrorReturn { RewardListPartialChange.Error() }
    }

    data class Parameters(
        val rewards: List<RewardViewModel>,
        val rewardToDelete: RewardViewModel
    )
}