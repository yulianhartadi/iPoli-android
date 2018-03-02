package mypoli.android.repeatingquest.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import mypoli.android.TestUtil
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.entity.RepeatingQuest
import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/2/18.
 */
class RepeatingQuestHistoryUseCaseSpek : Spek({

    describe("RepeatingQuestHistoryUseCase") {

        fun executeUseCase(
            questRepo: QuestRepository,
            repeatingQuest: RepeatingQuest,
            start: LocalDate, end: LocalDate
        ) =
            RepeatingQuestHistoryUseCase(questRepo).execute(
                RepeatingQuestHistoryUseCase.Params(
                    repeatingQuest,
                    start,
                    end
                )
            )

        it("should require end to be after start") {
            val exec = {
                executeUseCase(
                    TestUtil.questRepoMock(),
                    TestUtil.repeatingQuest,
                    LocalDate.now(),
                    LocalDate.now().minusDays(1)
                )
            }
            exec shouldThrow IllegalArgumentException::class
        }

        it("should return completed") {
            val date = LocalDate.now()
            val questRepoMock = mock<QuestRepository> {
                on {
                    findCompletedForRepeatingQuestInPeriod(
                        any(),
                        any(),
                        any()
                    )
                } doReturn listOf<Quest>(
                    TestUtil.quest.copy(
                        completedAtDate = date
                    )
                )
            }
            val result = executeUseCase(
                questRepoMock, TestUtil.repeatingQuest.copy(
                    repeatingPattern = RepeatingPattern.Daily()
                ), date, date
            )
            result[date].`should equal`(RepeatingQuestHistoryUseCase.QuestState.COMPLETED)
        }

        it("should return not completed") {
            val date = LocalDate.now()
            val questRepoMock = mock<QuestRepository> {
                on {
                    findCompletedForRepeatingQuestInPeriod(
                        any(),
                        any(),
                        any()
                    )
                } doReturn listOf<Quest>()
            }
            val result = executeUseCase(
                questRepoMock, TestUtil.repeatingQuest.copy(
                    repeatingPattern = RepeatingPattern.Daily()
                ), date, date
            )
            result[date].`should equal`(RepeatingQuestHistoryUseCase.QuestState.NOT_COMPLETED)
        }
    }
})