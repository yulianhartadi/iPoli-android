package io.ipoli.android.store.avatars

import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.player.persistence.PlayerRepository
import io.reactivex.Observable
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/21/17.
 */
class BuyAvatarUseCase(private val playerRepository: PlayerRepository) : BaseRxUseCase<AvatarViewModel, AvatarListPartialChange>() {

    override fun createObservable(parameters: AvatarViewModel): Observable<AvatarListPartialChange> =
        playerRepository.find()
            .flatMap { player ->
                player.inventory.addAvatar(parameters.code, LocalDate.now())
                playerRepository.save(player)
            }
            .map {
                AvatarListPartialChange.AvatarBought(parameters)
            }.toObservable()
            .cast(AvatarListPartialChange::class.java)
            .startWith(AvatarListPartialChange.Loading())
            .onErrorReturn { AvatarListPartialChange.Error(it) }

}