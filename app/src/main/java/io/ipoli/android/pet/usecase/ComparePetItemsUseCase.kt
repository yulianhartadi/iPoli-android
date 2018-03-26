package io.ipoli.android.pet.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.pet.PetItem

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 23.12.17.
 */
class ComparePetItemsUseCase :
    UseCase<ComparePetItemsUseCase.Params, ComparePetItemsUseCase.Result> {
    override fun execute(parameters: Params): Result {

        val (currentItem, newItem) = parameters

        if (currentItem == null) {
            return ComparePetItemsUseCase.Result(
                newItem.experienceBonus,
                newItem.coinBonus,
                newItem.bountyBonus
            )
        }

        require(currentItem.type == newItem.type)

        return ComparePetItemsUseCase.Result(
            newItem.experienceBonus - currentItem.experienceBonus,
            newItem.coinBonus - currentItem.coinBonus,
            newItem.bountyBonus - currentItem.bountyBonus
        )
    }

    data class Params(val currentItem: PetItem?, val newItem: PetItem)

    data class Result(val experienceBonus: Int, val coinBonus: Int, val bountyBonus: Int)
}