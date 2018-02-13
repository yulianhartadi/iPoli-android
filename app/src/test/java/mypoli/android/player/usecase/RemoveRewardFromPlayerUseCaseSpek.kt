package mypoli.android.player.usecase

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import mypoli.android.TestUtil
import mypoli.android.common.SimpleReward
import mypoli.android.pet.Pet
import mypoli.android.pet.PetAvatar
import mypoli.android.player.ExperienceForLevelGenerator
import mypoli.android.player.LevelDownScheduler
import mypoli.android.player.persistence.PlayerRepository
import mypoli.android.quest.Quest
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

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

        val player = TestUtil.player().copy(
            level = 2,
            coins = 10,
            experience = ExperienceForLevelGenerator.forLevel(2),
            pet = pet
        )

        val reward = SimpleReward(0, 0, Quest.Bounty.None)

        val levelDownScheduler = mock<LevelDownScheduler>()

        val playerRepo = mock<PlayerRepository> {
            on { find() } doReturn player
        }

        val useCase = RemoveRewardFromPlayerUseCase(playerRepo, levelDownScheduler)

        beforeEachTest {
            reset(levelDownScheduler)
        }

        it("should level down") {
            val newPlayer = useCase.execute(reward.copy(experience = 1, coins = 1))
            newPlayer.level.`should be equal to`(1)
            Verify on levelDownScheduler that levelDownScheduler.schedule() was called
        }

        it("should remove XP & coins") {
            val xp = 10
            val coins = 5
            val newPlayer = useCase.execute(reward.copy(experience = xp, coins = coins))
            newPlayer.coins.`should be`(player.coins - coins)
            newPlayer.experience.`should be`(player.experience - xp)
        }

        it("should remove reward from the Pet") {
            val xp = 10
            val coins = 5
            val newQuest = reward.copy(experience = xp, coins = coins)
            val newPet = useCase.execute(newQuest).pet
            val petReward = pet.removeReward(newQuest)
            newPet.healthPoints.`should be equal to`(petReward.healthPoints)
            newPet.moodPoints.`should be equal to`(petReward.moodPoints)
            newPet.mood.`should be`(petReward.mood)
            newPet.experienceBonus.`should be equal to`(petReward.experienceBonus)
            newPet.coinBonus.`should be equal to`(petReward.coinBonus)
            newPet.bountyBonus.`should be equal to`(petReward.bountyBonus)
        }
    }
})