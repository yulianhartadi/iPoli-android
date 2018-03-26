package io.ipoli.android.pet.usecase

import io.ipoli.android.pet.PetItem
import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@io.ipoli.io>
 * on 23.12.17.
 */
class ComparePetItemsUseCaseSpek : Spek({
    describe("ComparePetItemsUseCase") {

        fun executeUseCase(currentItem: PetItem?, newItem: PetItem) =
            ComparePetItemsUseCase().execute(ComparePetItemsUseCase.Params(currentItem, newItem))

        val newItem = PetItem.GLASSES

        it("should not compare different type of items") {
            val exec = { executeUseCase(PetItem.RED_HAT, newItem) }
            exec shouldThrow IllegalArgumentException::class
        }

        it("should give no bonus difference between same items") {
            val result = executeUseCase(newItem, newItem)
            val expectedResult = ComparePetItemsUseCase.Result(0, 0, 0)
            result.`should equal`(expectedResult)
        }

        it("should give bonus from new item when no current item") {
            val result = executeUseCase(null, newItem)
            val expectedResult = ComparePetItemsUseCase.Result(
                newItem.experienceBonus,
                newItem.coinBonus,
                newItem.bountyBonus
            )
            result.`should equal`(expectedResult)
        }

        it("should give correct bonuses for new item") {
            val currentItem = PetItem.BEARD
            val result = executeUseCase(currentItem, newItem)
            val expectedResult = ComparePetItemsUseCase.Result(
                newItem.experienceBonus - currentItem.experienceBonus,
                newItem.coinBonus - currentItem.coinBonus,
                newItem.bountyBonus - currentItem.bountyBonus
            )
            result.`should equal`(expectedResult)
        }
    }
})