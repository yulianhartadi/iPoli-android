package mypoli.android.pet.store

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import mypoli.android.R
import mypoli.android.common.AppState
import mypoli.android.common.redux.android.AndroidStatePresenter
import mypoli.android.pet.AndroidPetAvatar
import mypoli.android.pet.PetAvatar
import mypoli.android.pet.PetMood

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/24/2018.
 */
class PetStorePresenter : AndroidStatePresenter<AppState, PetStoreViewState> {

    override fun present(state: AppState, context: Context): PetStoreViewState {

        val petStoreState = state.petStoreState

        val playerGems = state.appDataState.player?.gems ?: 0

        return PetStoreViewState(
            type = PetStoreViewState.StateType.valueOf(petStoreState.type.name),
            playerGems = playerGems,
            petViewModels = petStoreState.pets.map {

                val avatar = AndroidPetAvatar.valueOf(it.avatar.name)

                when {
                    it.isCurrent -> {
                        PetViewModel(
                            it.avatar,
                            avatar.petName,
                            avatar.image,
                            it.avatar.gemPrice.toString(),
                            avatar.description,
                            null,
                            avatar.moodImage[PetMood.HAPPY]!!,
                            false,
                            true,
                            null
                        )
                    }

                    it.isBought -> {
                        PetViewModel(
                            it.avatar,
                            avatar.petName,
                            avatar.image,
                            it.avatar.gemPrice.toString(),
                            avatar.description,
                            R.string.store_pet_in_inventory,
                            avatar.moodImage[PetMood.GOOD]!!,
                            true,
                            false,
                            PetViewModel.Action.CHANGE
                        )
                    }

                    it.isLocked -> {
                        PetViewModel(
                            it.avatar,
                            avatar.petName,
                            avatar.image,
                            it.avatar.gemPrice.toString(),
                            avatar.description,
                            R.string.unlock,
                            avatar.moodImage[PetMood.GOOD]!!,
                            true,
                            false,
                            PetViewModel.Action.UNLOCK
                        )
                    }

                    else -> {
                        PetViewModel(
                            it.avatar,
                            avatar.petName,
                            avatar.image,
                            it.avatar.gemPrice.toString(),
                            avatar.description,
                            R.string.store_buy_pet,
                            avatar.moodImage[PetMood.GOOD]!!,
                            true,
                            false,
                            PetViewModel.Action.BUY
                        )
                    }
                }


            }
        )
    }

    data class PetViewModel(
        val avatar: PetAvatar,
        @StringRes val name: Int,
        @DrawableRes val image: Int,
        val price: String,
        @StringRes val description: Int,
        @StringRes val actionText: Int?,
        @DrawableRes val moodImage: Int,
        val showAction: Boolean,
        val showIsCurrent: Boolean,
        val action: Action?
    ) {
        enum class Action {
            CHANGE, UNLOCK, BUY
        }
    }

}