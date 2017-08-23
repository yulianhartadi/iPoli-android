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
class BuyAvatarUseCase(private val playerRepository: PlayerRepository) : BaseRxUseCase<AvatarViewModel, AvatarListViewState>() {

    override fun createObservable(avatarViewModel: AvatarViewModel): Observable<AvatarListViewState> =
        playerRepository.find()
            .flatMap { player ->
                Log.d("AAA find", player.toString())
                player.inventory.addAvatar(avatarViewModel.code, LocalDate.now())
                playerRepository.save(player)
            }
            .map { player ->
                Log.d("AAA buy", player.toString())
                AvatarListViewState.AvatarBought(avatarViewModel)
            }.toObservable()
            .cast(AvatarListViewState::class.java)
            .startWith(AvatarListViewState.Loading())
            .onErrorReturn { AvatarListViewState.Error(it) }

}