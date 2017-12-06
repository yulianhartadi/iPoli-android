package io.ipoli.android.quest.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import io.ipoli.android.TestUtil
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.usecase.RewardPlayerUseCase
import io.ipoli.android.quest.*
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.reminder.ReminderScheduler
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/15/17.
 */
class CompleteQuestUseCaseSpek : Spek({

    describe("CompleteQuestUseCase") {

        fun createQuestRepository(quest: Quest): QuestRepository = mock {
            on { findById(any()) } doReturn
                quest

            on { findNextQuestsToRemind(any()) } doReturn
                listOf(quest)
        }

        val player = TestUtil.player()

        val quest = Quest(
            name = "",
            color = Color.BLUE,
            category = Category("Wellness", Color.BLUE),
            scheduledDate = LocalDate.now(),
            duration = 30,
            reminder = Reminder("", Time.now(), LocalDate.now())
        )

        val questRepo = createQuestRepository(quest)

        val questCompleteScheduler = mock<QuestCompleteScheduler>()
        val reminderScheduler = mock<ReminderScheduler>()
        val rewardPlayerUseCase = mock<RewardPlayerUseCase>()

        val playerRepo = mock<PlayerRepository> {
            on { find() } doReturn player
        }

        val useCase = CompleteQuestUseCase(
            questRepo,
            playerRepo,
            reminderScheduler,
            questCompleteScheduler,
            rewardPlayerUseCase,
            42
        )

        beforeEachTest {
            reset(rewardPlayerUseCase, questCompleteScheduler, reminderScheduler)
        }

        val questId = "sampleid"

        it("should fail when questId is empty") {

            val call = { useCase.execute("") }
            call `should throw` IllegalArgumentException::class
        }

        it("should mark quest as completed") {

            val newQuest = useCase.execute(questId)
            newQuest.completedAtTime.shouldNotBeNull()
            newQuest.completedAtDate.shouldNotBeNull()
        }

        it("should schedule next reminder") {
            useCase.execute(questId)
            Verify on reminderScheduler that reminderScheduler.schedule(any()) was called
        }

        it("should schedule show quest complete message") {
            useCase.execute(questId)
            Verify on questCompleteScheduler that questCompleteScheduler.schedule(any()) was called
        }

        it("should have XP") {
            val newQuest = useCase.execute(questId)
            newQuest.experience.shouldNotBeNull()
            newQuest.experience!! `should be greater than` 0
        }

        it("should have coins") {
            val newQuest = useCase.execute(questId)
            newQuest.coins.shouldNotBeNull()
            newQuest.coins!! `should be greater than` 0
        }

        it("should have None bounty") {
            val newQuest = useCase.execute(questId)
            newQuest.bounty.`should be`(Quest.Bounty.None)
        }

        it("should have Food bounty") {

            val foodUseCase = CompleteQuestUseCase(
                questRepo,
                playerRepo,
                reminderScheduler,
                questCompleteScheduler,
                rewardPlayerUseCase,
                4096
            )

            val newQuest = foodUseCase.execute(questId)
            newQuest.bounty.`should be instance of`(Quest.Bounty.Food::class)
        }

        it("should not change bounty") {
            val noChangeUseCase = CompleteQuestUseCase(
                createQuestRepository(quest.copy(
                    bounty = Quest.Bounty.None
                )),
                playerRepo,
                reminderScheduler,
                questCompleteScheduler,
                rewardPlayerUseCase,
                4096
            )

            val newQuest = noChangeUseCase.execute(questId)
            newQuest.bounty.`should be`(Quest.Bounty.None)
        }
    }
})