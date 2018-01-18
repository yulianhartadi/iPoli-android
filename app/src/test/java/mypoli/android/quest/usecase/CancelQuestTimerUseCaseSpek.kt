package mypoli.android.quest.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import mypoli.android.common.datetime.Time
import mypoli.android.quest.*
import mypoli.android.quest.data.persistence.QuestRepository
import org.amshove.kluent.`should be empty`
import org.amshove.kluent.`should be null`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/18/18.
 */
class CancelQuestTimerUseCaseSpek : Spek({

    describe("CancelQuestTimerUseCase") {

        fun executeUseCase(
            quest: Quest
        ): Quest {
            val questRepoMock = mock<QuestRepository> {
                on { findById(any()) } doReturn quest
                on { save(any()) } doAnswer { invocation ->
                    invocation.getArgument(0)
                }
            }
            return CancelQuestTimerUseCase(questRepoMock)
                .execute(CancelQuestTimerUseCase.Params(quest.id))
        }

        val simpleQuest = Quest(
            name = "",
            color = Color.BLUE,
            category = Category("Wellness", Color.BLUE),
            scheduledDate = LocalDate.now(),
            duration = 30,
            reminder = Reminder("", Time.now(), LocalDate.now())
        )

        it("should cancel count down") {
            val result = executeUseCase(
                simpleQuest.copy(
                    actualStart = LocalDateTime.now()
                )
            )
            result.actualStart.`should be null`()
        }

        it("should cancel current pomodoro") {
            val result = executeUseCase(
                simpleQuest.copy(
                    pomodoroTimeRanges = listOf(
                        TimeRange(
                            TimeRange.Type.WORK,
                            1.pomodoros(),
                            start = LocalDateTime.now(),
                            end = null
                        )
                    )
                )
            )
            result.pomodoroTimeRanges.`should be empty`()
        }
    }
})