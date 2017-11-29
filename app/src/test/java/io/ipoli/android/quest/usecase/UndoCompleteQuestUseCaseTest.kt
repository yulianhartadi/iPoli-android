package io.ipoli.android.quest.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.player.AuthProvider
import io.ipoli.android.player.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.quest.Category
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.Reminder
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
class UndoCompleteQuestUseCaseSpek : Spek({

    describe("UndoCompletedQuestUseCase") {
        val questId = "sampleid"

        val quest = Quest(
            name = "",
            color = Color.BLUE,
            category = Category("Wellness", Color.BLUE),
            scheduledDate = LocalDate.now(),
            duration = 30,
            reminder = Reminder("", Time.now(), LocalDate.now()),
            experience = 20,
            coins = 10
        )

        var questRepo: QuestRepository = mock<QuestRepository>()

        var reminderScheduler = mock<ReminderScheduler>()

        val player = Player(
            authProvider = AuthProvider()
        )

        var playerRepo: PlayerRepository = mock<PlayerRepository>()

        var useCase = UndoCompletedQuestUseCase(
            questRepo,
            playerRepo,
            reminderScheduler
        )

        beforeEachTest {
            questRepo = mock<QuestRepository> {

                on { findById(any()) } doReturn
                    quest

                on { findNextQuestsToRemind(any()) } doReturn
                    listOf(quest)
            }

            playerRepo = mock<PlayerRepository> {
                on { find() } doReturn player
            }

            reminderScheduler = mock<ReminderScheduler>()

            useCase = UndoCompletedQuestUseCase(
                questRepo,
                playerRepo,
                reminderScheduler
            )
        }

        it("should fail when questId is empty") {
            val call = { useCase.execute("") }
            call `should throw` IllegalArgumentException::class
        }

        it("should mark quest as not completed") {
            val newQuest = useCase.execute(questId)
            newQuest.completedAtTime.shouldBeNull()
            newQuest.completedAtDate.shouldBeNull()
        }

        it("should schedule next reminder") {
            useCase.execute(questId)
            Verify on reminderScheduler that reminderScheduler.schedule(any()) was called
        }

        it("should not remove XP") {
            val newQuest = useCase.execute(questId)
            newQuest.experience.shouldNotBeNull()
        }

        it("should not remove coins") {
            val newQuest = useCase.execute(questId)
            newQuest.coins.shouldNotBeNull()
        }

        it("should level down & remove coins from Player") {
            val player = Player(
                level = 2,
                experience = 50,
                coins = 100,
                authProvider = AuthProvider()
            )

            val playerRepo = mock<PlayerRepository> {
                on { find() } doReturn player
            }
            val useCase = UndoCompletedQuestUseCase(
                questRepo,
                playerRepo,
                reminderScheduler
            )
            val newQuest = useCase.execute(questId)
            Verify on playerRepo that playerRepo.save(
                player.copy(
                    level = 1,
                    experience = player.experience - newQuest.experience!!,
                    coins = player.coins - newQuest.coins!!
                )
            ) was called
        }
    }

})




