package io.ipoli.android.store

import io.ipoli.android.SimpleRxUseCase
import io.ipoli.android.player.PlayerRepository
import io.reactivex.Observable

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/18/17.
 */
class DisplayCoinsUseCase(private val playerRepository: PlayerRepository) : SimpleRxUseCase<StoreStatePartialChange>() {

    override fun createObservable(params: Unit): Observable<StoreStatePartialChange> =
        playerRepository.get()
            .map { player ->
                StoreLoadedPartialChange(player.coins) as StoreStatePartialChange
            }
            .startWith(StoreLoadingPartialChange())

}