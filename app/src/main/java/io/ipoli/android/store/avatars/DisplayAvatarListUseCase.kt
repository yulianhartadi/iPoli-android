package io.ipoli.android.store.avatars

import android.util.Log
import io.ipoli.android.common.SimpleRxUseCase
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.store.avatars.data.Avatar
import io.reactivex.Observable

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/21/17.
 */
class DisplayAvatarListUseCase(private val playerRepository: PlayerRepository) : SimpleRxUseCase<AvatarListViewState>() {

    override fun createObservable(params: Unit): Observable<AvatarListViewState> =
        playerRepository.listen()
            .map { player ->
                Log.d("AAA use case", "display avatar")
                AvatarListViewState.DataLoaded(Avatar.values().map {
                    AvatarViewModel(it.code, it.avatarName,
                        it.price, it.picture, player.inventory.hasAvatar(it.code))
                })
            }.cast(AvatarListViewState::class.java)
            .startWith(AvatarListViewState.Loading())
            .onErrorReturn { AvatarListViewState.Error(it) }
}