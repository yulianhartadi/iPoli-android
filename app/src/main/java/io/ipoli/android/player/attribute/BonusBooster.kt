package io.ipoli.android.player.attribute

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.challenge.entity.SharingPreference
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.pet.Pet
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.data.Player.AttributeType.*
import io.ipoli.android.player.data.Player.Rank.*
import io.ipoli.android.quest.Quest

data class Booster(
    val healthPointsPercentage: Int = 0,
    val experiencePercentage: Int = 0,
    val coinsPercentage: Int = 0,
    val itemDropPercentage: Int = 0,
    val strengthPercentage: Int = 0,
    val intelligencePercentage: Int = 0,
    val charismaPercentage: Int = 0,
    val expertisePercentage: Int = 0,
    val wellBeingPercentage: Int = 0,
    val willpowerPercentage: Int = 0
) {

    companion object {
        fun ofAllAttributeBonus(bonus: Int) =
            Booster(
                strengthPercentage = bonus,
                intelligencePercentage = bonus,
                charismaPercentage = bonus,
                expertisePercentage = bonus,
                wellBeingPercentage = bonus,
                willpowerPercentage = bonus
            )
    }

    fun combineWith(other: Booster) =
        Booster(
            healthPointsPercentage = healthPointsPercentage + other.healthPointsPercentage,
            experiencePercentage = experiencePercentage + other.experiencePercentage,
            coinsPercentage = coinsPercentage + other.coinsPercentage,
            itemDropPercentage = itemDropPercentage + other.itemDropPercentage,
            strengthPercentage = strengthPercentage + other.strengthPercentage,
            intelligencePercentage = intelligencePercentage + other.intelligencePercentage,
            charismaPercentage = charismaPercentage + other.charismaPercentage,
            expertisePercentage = expertisePercentage + other.expertisePercentage,
            wellBeingPercentage = wellBeingPercentage + other.wellBeingPercentage,
            willpowerPercentage = willpowerPercentage + other.willpowerPercentage
        )

    fun attributeBonus(attributeType: Player.AttributeType) =
        when (attributeType) {
            STRENGTH -> strengthPercentage
            INTELLIGENCE -> intelligencePercentage
            CHARISMA -> charismaPercentage
            EXPERTISE -> expertisePercentage
            WELL_BEING -> wellBeingPercentage
            WILLPOWER -> willpowerPercentage
        }
}

object TotalBonusBooster : BonusBooster {

    private val bonusBoosters = listOf(
        PetBooster(),
        PetItemBooster(),
        StrengthBooster(),
        IntelligenceBooster(),
        CharismaBooster(),
        ExpertiseBooster(),
        WellBeingBooster(),
        WillpowerBooster()
    )

    override fun forQuest(quest: Quest, player: Player, rank: Player.Rank): Booster {
        var booster = Booster()
        bonusBoosters.forEach {
            booster = booster.combineWith(it.forQuest(quest, player, rank))
        }
        return booster
    }

    override fun forHabit(habit: Habit, player: Player, rank: Player.Rank): Booster {
        var booster = Booster()
        bonusBoosters.forEach {
            booster = booster.combineWith(it.forHabit(habit, player, rank))
        }
        return booster
    }

    override fun forChallenge(
        challenge: Challenge,
        player: Player,
        rank: Player.Rank
    ): Booster {
        var booster = Booster()
        bonusBoosters.forEach {
            booster = booster.combineWith(it.forChallenge(challenge, player, rank))
        }
        return booster
    }

    override fun forDailyChallenge(player: Player, rank: Player.Rank): Booster {
        var booster = Booster()
        bonusBoosters.forEach {
            booster = booster.combineWith(it.forDailyChallenge(player, rank))
        }
        return booster
    }

}

interface BonusBooster {
    fun forQuest(quest: Quest, player: Player, rank: Player.Rank): Booster
    fun forHabit(habit: Habit, player: Player, rank: Player.Rank): Booster
    fun forChallenge(challenge: Challenge, player: Player, rank: Player.Rank): Booster
    fun forDailyChallenge(player: Player, rank: Player.Rank): Booster
}

interface BoostStrategy {
    fun specialistBooster(): Booster
    fun adeptBooster(): Booster
    fun apprenticeBooster(): Booster
}

open class NoBoostStrategy : BoostStrategy {
    override fun specialistBooster() = Booster()

    override fun adeptBooster() = Booster()

    override fun apprenticeBooster() = Booster()
}

abstract class BaseAttributeBonusBooster(private val attributeType: Player.AttributeType) :
    BonusBooster {

    override fun forQuest(quest: Quest, player: Player, rank: Player.Rank) =
        applyBoostStrategy(createQuestBoostStrategy(quest), player, rank)

    override fun forHabit(habit: Habit, player: Player, rank: Player.Rank) =
        applyBoostStrategy(createHabitBoostStrategy(habit), player, rank)


    override fun forChallenge(
        challenge: Challenge,
        player: Player,
        rank: Player.Rank
    ) =
        applyBoostStrategy(createChallengeBoostStrategy(challenge), player, rank)

    override fun forDailyChallenge(player: Player, rank: Player.Rank) =
        applyBoostStrategy(createDailyChallengeBoostStrategy(), player, rank)

    private fun applyBoostStrategy(strategy: BoostStrategy, player: Player, rank: Player.Rank) =
        when (AttributeRank.of(player.attributeLevel(attributeType), rank)) {
            SPECIALIST ->
                strategy.apprenticeBooster()
                    .combineWith(strategy.adeptBooster())
                    .combineWith(strategy.specialistBooster())

            ADEPT ->
                strategy.apprenticeBooster()
                    .combineWith(strategy.adeptBooster())

            APPRENTICE ->
                strategy.apprenticeBooster()

            else -> Booster()
        }

    protected open fun createQuestBoostStrategy(quest: Quest): BoostStrategy = NoBoostStrategy()
    protected open fun createHabitBoostStrategy(habit: Habit): BoostStrategy = NoBoostStrategy()
    protected open fun createChallengeBoostStrategy(challenge: Challenge): BoostStrategy =
        NoBoostStrategy()

    protected open fun createDailyChallengeBoostStrategy(): BoostStrategy = NoBoostStrategy()
}

class PetBooster : BonusBooster {

    override fun forQuest(quest: Quest, player: Player, rank: Player.Rank) =
        applyPetBonus(player)

    override fun forHabit(habit: Habit, player: Player, rank: Player.Rank) =
        applyPetBonus(player)

    override fun forChallenge(
        challenge: Challenge,
        player: Player,
        rank: Player.Rank
    ) = applyPetBonus(player)

    private fun applyPetBonus(player: Player) =
        player.pet.let {
            Booster(
                experiencePercentage = it.experienceBonus.toInt(),
                coinsPercentage = it.coinBonus.toInt(),
                itemDropPercentage = it.itemDropBonus.toInt()
            )
        }

    override fun forDailyChallenge(player: Player, rank: Player.Rank) = Booster()
}

class PetItemBooster : BonusBooster {

    override fun forQuest(quest: Quest, player: Player, rank: Player.Rank) =
        applyPetItemBonus(player.pet)

    override fun forHabit(habit: Habit, player: Player, rank: Player.Rank) =
        applyPetItemBonus(player.pet)

    override fun forChallenge(
        challenge: Challenge,
        player: Player,
        rank: Player.Rank
    ) = applyPetItemBonus(player.pet)

    private fun applyPetItemBonus(pet: Pet): Booster {
        val eq = pet.equipment
        var xp = 0
        var coins = 0
        var itemDrop = 0
        eq.hat?.let {
            xp += it.experienceBonus
            coins += it.coinBonus
            itemDrop += it.bountyBonus
        }

        eq.mask?.let {
            xp += it.experienceBonus
            coins += it.coinBonus
            itemDrop += it.bountyBonus
        }

        eq.bodyArmor?.let {
            xp += it.experienceBonus
            coins += it.coinBonus
            itemDrop += it.bountyBonus
        }

        return Booster(
            experiencePercentage = xp,
            coinsPercentage = coins,
            itemDropPercentage = itemDrop
        )
    }

    override fun forDailyChallenge(player: Player, rank: Player.Rank) = Booster()
}

class StrengthBooster : BaseAttributeBonusBooster(STRENGTH) {

    override fun createQuestBoostStrategy(quest: Quest) =
        object : NoBoostStrategy() {
            override fun specialistBooster() =
                Booster(healthPointsPercentage = 20)
        }
}

class IntelligenceBooster : BaseAttributeBonusBooster(INTELLIGENCE) {

    override fun createQuestBoostStrategy(quest: Quest) =
        object : NoBoostStrategy() {

            override fun adeptBooster() =
                if (quest.hasTimer)
                    Booster(experiencePercentage = 100)
                else
                    Booster()

            override fun apprenticeBooster() =
                if (quest.completedAtTime == null || quest.startTime == null || quest.endTime == null) {
                    Booster()
                } else if (!quest.isScheduled || !quest.completedAtTime.isBetween(
                        quest.startTime,
                        quest.endTime!!.plus(120)
                    )
                ) {
                    Booster()
                } else {
                    Booster(experiencePercentage = 20, coinsPercentage = 20)
                }
        }
}

class CharismaBooster : BaseAttributeBonusBooster(CHARISMA) {
    override fun createChallengeBoostStrategy(challenge: Challenge) =
        object : NoBoostStrategy() {
            override fun specialistBooster() =
                if (challenge.sharingPreference == SharingPreference.PRIVATE) {
                    Booster()
                } else {
                    Booster.ofAllAttributeBonus(50)
                }
        }
}

class ExpertiseBooster : BaseAttributeBonusBooster(EXPERTISE) {

    override fun createQuestBoostStrategy(quest: Quest) =
        object : NoBoostStrategy() {
            override fun adeptBooster() = Booster(intelligencePercentage = 5)
        }

    override fun createHabitBoostStrategy(habit: Habit) =
        object : NoBoostStrategy() {
            override fun adeptBooster() = Booster(intelligencePercentage = 5)
        }

    override fun createDailyChallengeBoostStrategy() =
        object : NoBoostStrategy() {

            override fun adeptBooster() = Booster(experiencePercentage = 20, coinsPercentage = 20)

            override fun specialistBooster() =
                Booster(experiencePercentage = 30, coinsPercentage = 30)
        }
}

class WellBeingBooster : BaseAttributeBonusBooster(WELL_BEING) {

    override fun createQuestBoostStrategy(quest: Quest) =
        object : NoBoostStrategy() {

            override fun adeptBooster() =
                Booster.ofAllAttributeBonus(10)

            override fun apprenticeBooster() =
                Booster.ofAllAttributeBonus(5)
        }

    override fun createHabitBoostStrategy(habit: Habit) =
        object : NoBoostStrategy() {

            override fun adeptBooster() =
                Booster.ofAllAttributeBonus(10)

            override fun apprenticeBooster() =
                Booster.ofAllAttributeBonus(5)
        }
}

class WillpowerBooster : BaseAttributeBonusBooster(WILLPOWER) {

    override fun createQuestBoostStrategy(quest: Quest) =
        object : NoBoostStrategy() {

            override fun specialistBooster() =
                if (quest.isFromRepeatingQuest)
                    Booster(experiencePercentage = 5, coinsPercentage = 5)
                else
                    Booster()

            override fun apprenticeBooster() =
                when {
                    quest.isFromChallenge -> Booster(
                        experiencePercentage = 10,
                        coinsPercentage = 10
                    )
                    quest.isFromRepeatingQuest -> Booster(
                        experiencePercentage = 5,
                        coinsPercentage = 5
                    )
                    else -> Booster()
                }
        }

    override fun createHabitBoostStrategy(habit: Habit) =
        object : NoBoostStrategy() {
            override fun specialistBooster() =
                Booster(Math.min(habit.currentStreak, 20), Math.min(habit.currentStreak, 20))

            override fun apprenticeBooster() =
                if (habit.isFromChallenge)
                    Booster(experiencePercentage = 10, coinsPercentage = 10)
                else
                    Booster()
        }
}