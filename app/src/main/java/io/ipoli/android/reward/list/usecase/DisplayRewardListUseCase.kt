package io.ipoli.android.reward.list.usecase

import io.ipoli.android.common.SimpleRxUseCase
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.reward.Reward
import io.ipoli.android.reward.RewardRepository
import io.ipoli.android.reward.list.RewardListPartialChange
import io.ipoli.android.reward.list.RewardViewModel
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/1/17.
 */
class DisplayRewardListUseCase(private val rewardRepository: RewardRepository,
                               private val playerRepository: PlayerRepository)
    : SimpleRxUseCase<RewardListPartialChange>() {

    override fun createObservable(parameters: Unit): Observable<RewardListPartialChange> =
        Observable.combineLatest<Player, List<Reward>, RewardListPartialChange>(
            playerRepository.listen(),
            rewardRepository.listenForAll(),
            BiFunction { player, rewards ->
                if (rewards.isEmpty()) {
                    RewardListPartialChange.Empty()
                } else {
                    val rewardModels = rewards.map {
                        RewardViewModel(it.id,
                            it.name,
                            it.description,
                            it.price,
                            player.coins >= it.price)
                    }
                    RewardListPartialChange.DataLoaded(rewardModels)
                }
            })
            .startWith(RewardListPartialChange.Loading())
            .onErrorReturn { RewardListPartialChange.Error() }
}