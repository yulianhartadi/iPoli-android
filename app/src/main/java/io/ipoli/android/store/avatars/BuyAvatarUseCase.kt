package io.ipoli.android.store.avatars

import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.common.SimpleRxUseCase
import io.ipoli.android.player.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.persistence.RealmPlayerRepository
import io.ipoli.android.store.avatars.data.Avatar
import io.reactivex.Observable
import timber.log.Timber

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/21/17.
 */
class BuyAvatarUseCase(private val playerRepository: PlayerRepository) : BaseRxUseCase<AvatarViewModel, AvatarListPartialStateChange>() {

    override fun createObservable(avatarViewModel: AvatarViewModel): Observable<AvatarListPartialStateChange> =
        playerRepository.findFirst()
            .map { player ->
                Timber.d(avatarViewModel.name.toString())
                //player.addAvatar(avatarViewModel.code)
                //save player
                AvatarBoughtPartialStateChange(Avatar.values()
                    .map { AvatarViewModel(it.code, it.avatarName, it.price, it.picture, false) },
                    0) as AvatarListPartialStateChange
            }.startWith(AvatarListLoadingPartialStateChange())

}