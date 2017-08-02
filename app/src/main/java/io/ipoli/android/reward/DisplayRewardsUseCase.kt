package io.ipoli.android.reward

import io.ipoli.android.SimpleRxUseCase
import io.ipoli.android.player.PlayerRepository
import io.reactivex.Observable
import timber.log.Timber

/**
 * Created by vini on 8/1/17.
 */
class DisplayRewardsUseCase(private val rewardRepository: RewardRepository, private val playerRepository: PlayerRepository) : SimpleRxUseCase<List<Reward>>() {

    override fun createObservable(params: Unit): Observable<List<Reward>> {
//        playerRepository.save(Player(coins = 22, experience = 345)).subscribe()
//        return playerRepository.get()
//                .doOnNext { player -> Timber.d(player.coins.toString()) }
//                .switchMap { player -> rewardRepository.loadRewards() }

        return rewardRepository.loadRewards()
    }

}