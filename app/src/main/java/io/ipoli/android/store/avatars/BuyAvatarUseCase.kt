package io.ipoli.android.store.avatars

import io.ipoli.android.common.SimpleRxUseCase
import io.ipoli.android.player.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.persistence.RealmPlayerRepository
import io.ipoli.android.store.avatars.data.Avatar
import io.reactivex.Observable

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/21/17.
 */
class BuyAvatarUseCase(private val playerRepository: PlayerRepository) : SimpleRxUseCase<AvatarListPartialStateChange>() {
    override fun createObservable(params: Unit): Observable<AvatarListPartialStateChange> =
        playerRepository.findFirst()
            .map { player ->
                AvatarBoughtPartialStateChange(Avatar.values().map { AvatarViewModel(it.code, it.avatarName, it.price, it.picture, false)}, 0) as AvatarListPartialStateChange
            }.startWith(AvatarListLoadingPartialStateChange())

}