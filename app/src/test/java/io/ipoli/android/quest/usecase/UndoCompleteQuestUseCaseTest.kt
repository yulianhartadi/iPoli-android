package io.ipoli.android.quest.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.player.AuthProvider
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
class UndoCompleteQuestUseCaseSpek : Spek({

    describe("UndoCompleteQuestUseCase") {
        val questId = "sampleid"

        beforeEachTest {
            reset(questRepo, reminderScheduler)
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

        it("should level down player") {
            val player = Player(
                level = 2,
                experience = 50,
                authProvider = AuthProvider()
            )

            val playerRepo = mock<PlayerRepository> {
                on { find() } doReturn player
            }
            val useCase = UndoCompleteQuestUseCase(
                questRepo,
                playerRepo,
                reminderScheduler
            )
            val newQuest = useCase.execute(questId)
            Verify on playerRepo that playerRepo.save(
                player.copy(
                    level = 1,
                    experience = player.experience - newQuest.experience!!
                )
            ) was called
        }
    }


})

val quest = Quest(
    name = "",
    color = Color.BLUE,
    category = Category("Wellness", Color.BLUE),
    scheduledDate = LocalDate.now(),
    duration = 30,
    reminder = Reminder("", Time.now(), LocalDate.now()),
    experience = 20
)

val questRepo: QuestRepository
    get() =
        mock<QuestRepository> {

            on { findById(any()) } doReturn
                quest

            on { findNextQuestsToRemind(any()) } doReturn
                listOf(quest)
        }

val reminderScheduler = mock<ReminderScheduler>()

val player = Player(
    authProvider = AuthProvider()
)

val playerRepo: PlayerRepository
    get() =
        mock<PlayerRepository> {
            on { find() } doReturn player
        }

val useCase = UndoCompleteQuestUseCase(
    questRepo,
    playerRepo,
    reminderScheduler
)



