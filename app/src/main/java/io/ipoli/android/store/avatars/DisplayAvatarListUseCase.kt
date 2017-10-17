package io.ipoli.android.store.avatars

import io.ipoli.android.common.SimpleRxUseCase
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.store.avatars.data.Avatar
import io.reactivex.Observable

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/21/17.
 */
class DisplayAvatarListUseCase(private val playerRepository: PlayerRepository) : SimpleRxUseCase<AvatarListPartialChange>() {

    override fun createObservable(parameters: Unit): Observable<AvatarListPartialChange> =
        playerRepository.listen()
            .map { player ->
                AvatarListPartialChange.DataLoaded(Avatar.values().map {
                    AvatarViewModel(it.code, it.avatarName,
                        it.price, it.picture, false)
                })
            }.cast(AvatarListPartialChange::class.java)
            .startWith(AvatarListPartialChange.Loading())
            .onErrorReturn { AvatarListPartialChange.Error(it) }
}