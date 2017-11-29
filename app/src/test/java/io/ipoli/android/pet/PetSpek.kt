package io.ipoli.android.pet

import io.ipoli.android.quest.Category
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Quest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 29.11.17.
 */
class PetSpek : Spek({
    describe("Pet") {

        val pet = Pet(
            name = "",
            avatar = PetAvatar.ELEPHANT,
            healthPoints = 0,
            moodPoints = 0
        )

        val quest = Quest(
            name = "",
            color = Color.BLUE,
            scheduledDate = LocalDate.now(),
            category = Category("", Color.BLUE_GREY),
            duration = 30,
            experience = 100,
            coins = 20
        )

        it("should not give more than max HP & MP") {
            val p = pet.copy(
                healthPoints = Pet.MAX_HP,
                moodPoints = Pet.MAX_MP,
                mood = PetMood.AWESOME
            )
            val newPet = p.rewardFor(quest)
            newPet.healthPoints.`should be equal to`(Pet.MAX_HP)
            newPet.moodPoints.`should be equal to`(Pet.MAX_MP)
        }

        it("should give 2x mood bonus when HP is high") {
            val sickPet = pet.copy(
                healthPoints = 30,
                moodPoints = 60,
                mood = PetMood.GOOD
            )

            val normalIncrease = sickPet.rewardFor(quest).moodPoints - sickPet.moodPoints
            val healthyPet = sickPet.copy(healthPoints = 90)
            val bonusIncrease = healthyPet.rewardFor(quest).moodPoints - sickPet.moodPoints
            bonusIncrease.`should be equal to`(normalIncrease * 2)
        }

        it("should have maximum bonus when mood is Awesome") {
            val newPet = pet.copy(
                healthPoints = 89,
                moodPoints = 89,
                mood = PetMood.HAPPY
            ).rewardFor(quest)
            newPet.mood.`should be`(PetMood.AWESOME)
            newPet.experienceBonus.`should be equal to`(Pet.MAX_XP_BONUS)
            newPet.coinBonus.`should be equal to`(Pet.MAX_COIN_BONUS)
            newPet.unlockChanceBonus.`should be equal to`(Pet.MAX_UNLOCK_CHANCE_BONUS)
        }

        it("should change mood to Happy reward from Quest is removed") {
            val newPet = pet.copy(
                healthPoints = Pet.MAX_HP,
                moodPoints = Pet.AWESOME_MIN_MOOD_POINTS - 1,
                mood = PetMood.AWESOME
            ).removeRewardFor(quest)
            newPet.mood.`should be`(PetMood.HAPPY)
        }
    }
})