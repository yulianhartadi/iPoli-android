package io.ipoli.android.quest.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import io.ipoli.android.TestUtil
import io.ipoli.android.common.Reward
import io.ipoli.android.common.rate.RatePopupScheduler
import io.ipoli.android.pet.Food
import io.ipoli.android.player.attribute.usecase.CheckForOneTimeBoostUseCase
import io.ipoli.android.player.usecase.RewardPlayerUseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.job.ReminderScheduler
import io.ipoli.android.quest.job.RewardScheduler
import io.ipoli.android.quest.usecase.CompleteQuestUseCase.Params.WithQuestId
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@io.ipoli.io>
 * on 11/15/17.
 */
class CompleteQuestUseCaseSpek : Spek({

    describe("CompleteQuestUseCase") {

        fun createQuestRepository(quest: Quest): QuestRepository = mock {
            on { findById(any()) } doReturn
                quest
        }

        val quest = TestUtil.quest

        val questRepo = createQuestRepository(quest)

        val rewardScheduler = mock<RewardScheduler>()
        val reminderScheduler = mock<ReminderScheduler>()
        val ratePopupScheduler = mock<RatePopupScheduler>()
        val rewardPlayerUseCase = RewardPlayerUseCase(
            TestUtil.playerRepoMock(TestUtil.player),
            mock(),
            mock(),
            CheckForOneTimeBoostUseCase(mock()),
            mock()
        )

        val useCase = CompleteQuestUseCase(
            questRepo,
            TestUtil.playerRepoMock(TestUtil.player),
            mock(),
            reminderScheduler,
            rewardScheduler,
            ratePopupScheduler,
            rewardPlayerUseCase,
            mock(),
            mock()
        )

        beforeEachTest {
            reset(rewardScheduler, reminderScheduler)
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
            Verify on rewardScheduler that rewardScheduler.schedule(
                any(),
                any(),
                any(),
                any()
            ) was called
        }

        it("should have XP") {
            val newQuest = useCase.execute(WithQuestId(questId))

            newQuest.reward!!.experience `should be greater than` 0
        }

        it("should have coins") {
            val newQuest = useCase.execute(WithQuestId(questId))
            newQuest.reward!!.coins `should be greater than` 0
        }

        it("should have Food bounty") {

            val p = TestUtil.player.copy(
                pet = TestUtil.player.pet.copy(
                    itemDropBonus = 10000f
                )
            )

            val foodUseCase = CompleteQuestUseCase(
                questRepo,
                TestUtil.playerRepoMock(p),
                mock(),
                reminderScheduler,
                rewardScheduler,
                ratePopupScheduler,
                rewardPlayerUseCase,
                mock(),
                mock()
            )

            val newQuest = foodUseCase.execute(WithQuestId(questId))
            newQuest.reward!!.bounty.`should be instance of`(Quest.Bounty.Food::class)
        }


        it("should not give reward when was already completed") {

            val expectedReward = Reward(emptyMap(), 0, 10, 20, Quest.Bounty.Food(Food.BANANA))
            val noNewBountyUseCase = CompleteQuestUseCase(
                createQuestRepository(
                    quest.copy(
                        reward = expectedReward
                    )
                ),
                TestUtil.playerRepoMock(TestUtil.player),
                mock(),
                reminderScheduler,
                rewardScheduler,
                ratePopupScheduler,
                rewardPlayerUseCase,
                mock(),
                mock()
            )

            val newQuest = noNewBountyUseCase.execute(WithQuestId(questId))
            newQuest.reward!!.bounty.`should be instance of`(Quest.Bounty.Food::class)
        }
    }
})