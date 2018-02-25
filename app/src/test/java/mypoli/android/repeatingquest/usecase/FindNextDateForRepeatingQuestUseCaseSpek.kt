package mypoli.android.repeatingquest.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import mypoli.android.TestUtil
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.entity.RepeatingQuest
import org.amshove.kluent.`should be true`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/25/18.
 */
class FindNextDateForRepeatingQuestUseCaseSpek : Spek({
    describe("FindNextDateForRepeatingQuestUseCase") {

        fun executeUseCase(
            questRepo: QuestRepository,
            repeatingQuest: RepeatingQuest,
            fromDate: LocalDate = LocalDate.now()
        ) =
            FindNextDateForRepeatingQuestUseCase(questRepo).execute(
                FindNextDateForRepeatingQuestUseCase.Params(
                    repeatingQuest, fromDate
                )
            )

        fun shouldHaveNextDate(repeatingQuest: RepeatingQuest, date: LocalDate) {
            repeatingQuest.nextDate!!.isEqual(date).`should be true`()
        }

        it("should find scheduled for today") {
            val today = LocalDate.now()
            val quest = TestUtil.quest.copy(
                scheduledDate = today,
                originalScheduledDate = today
            )
            val questRepoMock = mock<QuestRepository> {
                on { findNextScheduledForRepeatingQuest(any()) } doReturn quest
            }

            val result = executeUseCase(questRepoMock, TestUtil.repeatingQuest)
            shouldHaveNextDate(result.repeatingQuest, today)
        }

        it("should find scheduled for tomorrow") {
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)
            val quest = TestUtil.quest.copy(
                originalScheduledDate = today,
                scheduledDate = tomorrow
            )
            val questRepoMock = mock<QuestRepository> {
                on { findNextScheduledForRepeatingQuest(any()) } doReturn quest
                on { findNextOriginalScheduledForRepeatingQuest(any()) } doReturn quest
            }

            val result = executeUseCase(questRepoMock, TestUtil.repeatingQuest)
            shouldHaveNextDate(result.repeatingQuest, tomorrow)
        }

        it("should find next from daily pattern") {

            val today = LocalDate.now()
            val questRepoMock = mock<QuestRepository> {
                //                on { findNextScheduledForRepeatingQuest(any()) } doReturn null
//                on { findNextOriginalScheduledForRepeatingQuest(any()) } doReturn null
            }

            val result = executeUseCase(questRepoMock, TestUtil.repeatingQuest)
            shouldHaveNextDate(result.repeatingQuest, today)
        }


    }
})