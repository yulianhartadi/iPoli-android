package mypoli.android.repeatingquest.usecase

import mypoli.android.TestUtil
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.entity.RepeatingQuest
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/14/2018.
 */
class ScheduleRepeatingQuestUseCaseSpek : Spek({

    describe("ScheduleRepeatingQuestUseCase") {
        fun executeUseCase(
            quest: RepeatingQuest,
            start: LocalDate,
            end: LocalDate,
            questRepository: QuestRepository = TestUtil.questRepoMock()
        ) =
            ScheduleRepeatingQuestUseCase(questRepository).execute(
                ScheduleRepeatingQuestUseCase.Params(
                    repeatingQuest = quest,
                    start = start,
                    end = end
                )
            )

        it("should require end after start") {
            val exec =
                { executeUseCase(RepeatingQuest(name = "Test"), LocalDate.now(), LocalDate.now()) }
            exec shouldThrow IllegalArgumentException::class
        }

        describe("repeating daily") {}

        describe("repeating weekly") {

        }

        describe("repeating monthly") {

        }

        describe("repeating yearly") {

        }
    }
})