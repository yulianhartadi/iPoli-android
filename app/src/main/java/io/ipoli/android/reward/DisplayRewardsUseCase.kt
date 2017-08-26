package io.ipoli.android.reward

import io.ipoli.android.common.SimpleRxUseCase
import io.ipoli.android.player.persistence.PlayerRepository
import io.reactivex.Observable

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/1/17.
 */
class DisplayRewardsUseCase(private val rewardRepository: RewardRepository, private val playerRepository: PlayerRepository) : SimpleRxUseCase<RewardListPartialChange>() {

    override fun createObservable(params: Unit): Observable<RewardListPartialChange> {
//        playerRepository.save(Player(coins = 22, experience = 345)).subscribe()
        return playerRepository.listen()
//                .doOnNext { player -> Timber.d(player.coins.toString()) }
            .switchMap { player ->
                rewardRepository.listenForAll()
                    .map { data ->
                        val rewardModels = data.map { RewardViewModel(it.id, it.name, it.description, it.price, player.coins >= it.price) }
                        RewardListPartialChange.DataLoaded(rewardModels)
                    }.cast(RewardListPartialChange::class.java)
            }
            .startWith(RewardListPartialChange.Loading())

//        return rewardRepository.loadRewards()
    }

}