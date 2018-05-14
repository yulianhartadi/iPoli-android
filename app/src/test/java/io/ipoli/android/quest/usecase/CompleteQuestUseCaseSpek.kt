package io.ipoli.android.quest.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import io.ipoli.android.TestUtil
import io.ipoli.android.common.SimpleReward
import io.ipoli.android.common.rate.RatePopupScheduler
import io.ipoli.android.pet.Food
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.usecase.RewardPlayerUseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.job.QuestCompleteScheduler
import io.ipoli.android.quest.job.ReminderScheduler
import io.ipoli.android.quest.usecase.CompleteQuestUseCase.Params.WithQuestId
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDateTime

/**
 * Created by Venelin Valkov <venelin@io.ipoli.io>
 * on 11/15/17.
 */
class CompleteQuestUseCaseSpek : Spek({

    describe("CompleteQuestUseCase") {

        fun createQuestRepository(quest: Quest): QuestRepository = mock {
            on { findById(any()) } doReturn
                    quest

            on { findNextReminderTime(any()) } doReturn LocalDateTime.now()
        }

        val player = TestUtil.player()

        val quest = TestUtil.quest

        val questRepo = createQuestRepository(quest)

        val questCompleteScheduler = mock<QuestCompleteScheduler>()
        val reminderScheduler = mock<ReminderScheduler>()
        val ratePopupScheduler = mock<RatePopupScheduler>()
        val rewardPlayerUseCase = mock<RewardPlayerUseCase>()

        val playerRepo = mock<PlayerRepository> {
            on { find() } doReturn player
        }

        val useCase = CompleteQuestUseCase(
            questRepo,
            playerRepo,
            reminderScheduler,
            questCompleteScheduler,
            ratePopupScheduler,
            rewardPlayerUseCase,
            42
        )

        beforeEachTest {
            reset(rewardPlayerUseCase, questCompleteScheduler, reminderScheduler)
        }

        val questId = "sampleid"

        it("should fail when questId is empty") {

            val call = { useCase.execute(WithQuestId("")) }
            call `should throw` IllegalArgumentException::class
        }

        it("should mark quest as completed") {

            val newQuest = useCase.execute(WithQuestId(questId))
            newQuest.completedAtTime.shouldNotBeNull()
            newQuest.completedAtDate.shouldNotBeNull()
        }

        it("should schedule next reminder") {
            useCase.execute(WithQuestId(questId))
            Verify on reminderScheduler that reminderScheduler.schedule() was called
        }

        it("should schedule show quest complete message") {
            useCase.execute(WithQuestId(questId))
            Verify on questCompleteScheduler that questCompleteScheduler.schedule(any()) was called
        }

        it("should have XP") {
            val newQuest = useCase.execute(WithQuestId(questId))
            newQuest.experience.shouldNotBeNull()
            newQuest.experience!! `should be greater than` 0
        }

        it("should have coins") {
            val newQuest = useCase.execute(WithQuestId(questId))
            newQuest.coins.shouldNotBeNull()
            newQuest.coins!! `should be greater than` 0
        }

        it("should have None bounty") {
            val newQuest = useCase.execute(WithQuestId(questId))
            newQuest.bounty.`should be`(Quest.Bounty.None)
        }

        it("should have Food bounty") {

            val foodUseCase = CompleteQuestUseCase(
                questRepo,
                playerRepo,
                reminderScheduler,
                questCompleteScheduler,
                ratePopupScheduler,
                rewardPlayerUseCase,
                4096
            )

            val newQuest = foodUseCase.execute(WithQuestId(questId))
            newQuest.bounty.`should be instance of`(Quest.Bounty.Food::class)
        }

        it("should not change bounty") {
            val noChangeUseCase = CompleteQuestUseCase(
                createQuestRepository(
                    quest.copy(
                        bounty = Quest.Bounty.None
                    )
                ),
                playerRepo,
                reminderScheduler,
                questCompleteScheduler,
                ratePopupScheduler,
                rewardPlayerUseCase,
                4096
            )

            val newQuest = noChangeUseCase.execute(WithQuestId(questId))
            newQuest.bounty.`should be`(Quest.Bounty.None)
        }

        it("should not give bounty when was already completed") {

            val rewardPlayerUseCaseMock = mock<RewardPlayerUseCase>()

            val xp = 10
            val coins = 20
            val noNewBountyUseCase = CompleteQuestUseCase(
                createQuestRepository(
                    quest.copy(
                        experience = xp,
                        coins = coins,
                        bounty = Quest.Bounty.Food(Food.BANANA)
                    )
                ),
                playerRepo,
                reminderScheduler,
                questCompleteScheduler,
                ratePopupScheduler,
                rewardPlayerUseCaseMock
            )

            val newQuest = noNewBountyUseCase.execute(WithQuestId(questId))
            newQuest.bounty.`should be instance of`(Quest.Bounty.Food::class)
            val expectedReward = SimpleReward(xp, coins, Quest.Bounty.None)
            Verify on rewardPlayerUseCaseMock that rewardPlayerUseCaseMock.execute(expectedReward) was called
        }
    }
})