package io.ipoli.android.store.avatars

import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.store.avatars.data.Avatar
import io.reactivex.Observable

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/21/17.
 */
class UseAvatarUseCase(private val playerRepository: PlayerRepository) : BaseRxUseCase<AvatarViewModel, AvatarListViewState>() {

    override fun createObservable(avatarViewModel: AvatarViewModel): Observable<AvatarListViewState> =
        playerRepository.find()
            .flatMap { player ->
                player.avatarCode = avatarViewModel.code
                playerRepository.save(player)
            }.map { player ->
            AvatarListViewState.AvatarUsed(avatarViewModel)
        }.toObservable()
            .cast(AvatarListViewState::class.java)
            .startWith(AvatarListViewState.Loading())
            .onErrorReturn { AvatarListViewState.Error(it) }

}