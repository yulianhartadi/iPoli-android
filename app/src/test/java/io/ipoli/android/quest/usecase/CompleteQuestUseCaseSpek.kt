package io.ipoli.android.quest.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.player.AuthProvider
import io.ipoli.android.player.LevelUpScheduler
import io.ipoli.android.player.Player
import io.ipoli.android.player.persistence.PlayerRepository
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

        val player = Player(
            authProvider = AuthProvider()
        )

        var playerRepo: PlayerRepository = createPlayerRepository(player)

        val quest = Quest(
            name = "",
            color = Color.BLUE,
            category = Category("Wellness", Color.BLUE),
            scheduledDate = LocalDate.now(),
            duration = 30,
            reminder = Reminder("", Time.now(), LocalDate.now())
        )

        var questCompleteScheduler = mock<QuestCompleteScheduler>()

        var reminderScheduler = mock<ReminderScheduler>()

        var levelUpScheduler = mock<LevelUpScheduler>()

        var questRepo: QuestRepository = createQuestRepository(quest)

        var useCase = CompleteQuestUseCase(
            questRepo,
            playerRepo,
            reminderScheduler,
            questCompleteScheduler,
            levelUpScheduler,
            42
        )

        beforeEachTest {
            playerRepo = createPlayerRepository(player)

            questCompleteScheduler = mock<QuestCompleteScheduler>()

            reminderScheduler = mock<ReminderScheduler>()

            levelUpScheduler = mock<LevelUpScheduler>()

            questRepo = createQuestRepository(quest)

            useCase = CompleteQuestUseCase(
                questRepo,
                playerRepo,
                reminderScheduler,
                questCompleteScheduler,
                levelUpScheduler,
                42
            )
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

        it("should save XP to the Quest") {
            val newQuest = useCase.execute(questId)
            newQuest.experience.shouldNotBeNull()
            newQuest.experience!! `should be greater than` 0
        }

        it("should save coins to the Quest") {
            val newQuest = useCase.execute(questId)
            newQuest.coins.shouldNotBeNull()
            newQuest.coins!! `should be greater than` 0
        }

        it("should level up & give coins to Player") {
            val player = Player(
                coins = 10,
                experience = 49,
                authProvider = AuthProvider()
            )

            val playerRepo = mock<PlayerRepository> {
                on { find() } doReturn player
            }
            val useCase = CompleteQuestUseCase(
                questRepo,
                playerRepo,
                reminderScheduler,
                questCompleteScheduler,
                levelUpScheduler
            )
            val newQuest = useCase.execute(questId)
            Verify on playerRepo that playerRepo.save(
                player.copy(
                    level = 2,
                    experience = player.experience + newQuest.experience!!,
                    coins = player.coins + newQuest.coins!!
                )
            ) was called

        }

        it("should schedule level up message") {
            val player = Player(
                experience = 49,
                authProvider = AuthProvider()
            )

            val playerRepo = mock<PlayerRepository> {
                on { find() } doReturn player
            }
            val useCase = CompleteQuestUseCase(
                questRepo,
                playerRepo,
                reminderScheduler,
                questCompleteScheduler,
                levelUpScheduler
            )
            useCase.execute(questId)
            Verify on levelUpScheduler that levelUpScheduler.schedule() was called
        }

    }
})

private fun createQuestRepository(quest: Quest): QuestRepository = mock {
    on { findById(any()) } doReturn
        quest

    on { findNextQuestsToRemind(any()) } doReturn
        listOf(quest)
}

private fun createPlayerRepository(player: Player): PlayerRepository = mock {
    on { find() } doReturn player
}

