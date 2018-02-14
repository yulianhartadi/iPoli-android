package mypoli.android.repeatingquest.usecase

import mypoli.android.TestUtil
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.entity.RepeatingQuest
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
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

        describe("repeating daily") {}

        describe("repeating weekly") {

        }

        describe("repeating monthly") {

        }

        describe("repeating yearly") {

        }
    }
})