package io.ipoli.android.player.usecase

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.pet.Pet
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.player.AuthProvider
import io.ipoli.android.player.ExperienceForLevelGenerator
import io.ipoli.android.player.LevelDownScheduler
import io.ipoli.android.player.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.quest.Category
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.Reminder
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 29.11.17.
 */
class RemoveRewardFromPlayerUseCaseSpek : Spek({
    describe("RemoveRewardFromPlayerUseCase") {
        val pet = Pet(
            "",
            avatar = PetAvatar.ELEPHANT,
            healthPoints = 10,
            moodPoints = Pet.AWESOME_MIN_MOOD_POINTS - 1
        )

        val player = Player(
            level = 2,
            coins = 10,
            experience = ExperienceForLevelGenerator.forLevel(2),
            authProvider = AuthProvider(),
            pet = pet
        )

        val quest = Quest(
            name = "",
            color = Color.BLUE,
            category = Category("Wellness", Color.BLUE),
            scheduledDate = LocalDate.now(),
            duration = 30,
            reminder = Reminder("", Time.now(), LocalDate.now())
        )

        val levelDownScheduler = mock<LevelDownScheduler>()

        val playerRepo = mock<PlayerRepository> {
            on { find() } doReturn player
        }

        val useCase = RemoveRewardFromPlayerUseCase(playerRepo, levelDownScheduler)

        beforeEachTest {
            reset(levelDownScheduler)
        }

        it("should level down") {
            val newPlayer = useCase.execute(quest.copy(experience = 1, coins = 1))
            newPlayer.level.`should be equal to`(1)
            Verify on levelDownScheduler that levelDownScheduler.schedule() was called
        }

        it("should remove XP & coins") {
            val xp = 10
            val coins = 5
            val newPlayer = useCase.execute(quest.copy(experience = xp, coins = coins))
            newPlayer.coins.`should be`(player.coins - coins)
            newPlayer.experience.`should be`(player.experience - xp)
        }

        it("should remove reward from the Pet") {
            val xp = 10
            val coins = 5
            val newQuest = quest.copy(experience = xp, coins = coins)
            val newPet = useCase.execute(newQuest).pet
            val petReward = pet.removeRewardFor(newQuest)
            newPet.healthPoints.`should be equal to`(petReward.healthPoints)
            newPet.moodPoints.`should be equal to`(petReward.moodPoints)
            newPet.mood.`should be`(petReward.mood)
            newPet.experienceBonus.`should be equal to`(petReward.experienceBonus)
            newPet.coinBonus.`should be equal to`(petReward.coinBonus)
            newPet.unlockChanceBonus.`should be equal to`(petReward.unlockChanceBonus)
        }
    }
})