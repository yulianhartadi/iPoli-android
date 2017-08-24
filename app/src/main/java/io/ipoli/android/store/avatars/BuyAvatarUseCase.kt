package io.ipoli.android.store.avatars

import android.util.Log
import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.store.avatars.data.Avatar
import io.reactivex.Observable
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/21/17.
 */
class BuyAvatarUseCase(private val playerRepository: PlayerRepository) : BaseRxUseCase<AvatarViewModel, AvatarListPartialChange>() {

    override fun createObservable(avatarViewModel: AvatarViewModel): Observable<AvatarListPartialChange> =
        playerRepository.find()
            .flatMap { player ->
                player.inventory.addAvatar(avatarViewModel.code, LocalDate.now())
                playerRepository.save(player)
            }
            .map { player ->
                AvatarListPartialChange.AvatarBought(avatarViewModel)
            }.toObservable()
            .cast(AvatarListPartialChange::class.java)
            .startWith(AvatarListPartialChange.Loading())
            .onErrorReturn { AvatarListPartialChange.Error(it) }

}