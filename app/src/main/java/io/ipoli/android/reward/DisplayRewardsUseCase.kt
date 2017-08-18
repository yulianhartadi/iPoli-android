package io.ipoli.android.reward

import io.ipoli.android.SimpleRxUseCase
import io.ipoli.android.player.PlayerRepository
import io.reactivex.Observable

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/1/17.
 */
class DisplayRewardsUseCase(private val rewardRepository: RewardRepository, private val playerRepository: PlayerRepository) : SimpleRxUseCase<RewardStatePartialChange>() {

    override fun createObservable(params: Unit): Observable<RewardStatePartialChange> {
//        playerRepository.save(Player(coins = 22, experience = 345)).subscribe()
        return playerRepository.get()
//                .doOnNext { player -> Timber.d(player.coins.toString()) }
            .switchMap { player ->
                rewardRepository.findAll()
                    .map { data ->
                        val rewardModels = data.map { RewardModel(it.id, it.name, it.description, it.price, player.coins >= it.price) }
                        RewardsLoadedPartialChange(rewardModels) as RewardStatePartialChange
                    }
            }
            .startWith(RewardsLoadingPartialChange())

//        return rewardRepository.loadRewards()
    }

}