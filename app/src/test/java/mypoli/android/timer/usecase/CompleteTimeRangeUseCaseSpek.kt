package mypoli.android.timer.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import mypoli.android.Constants
import mypoli.android.common.datetime.Time
import mypoli.android.quest.*
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.quest.usecase.CompleteQuestUseCase
import mypoli.android.timer.pomodoros
import mypoli.android.timer.shortBreaks
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/18/18.
 */
class CompleteTimeRangeUseCaseSpek : Spek({

    describe("CompleteTimeRangeUseCase") {

        fun executeUseCase(
            quest: Quest,
            time: Instant = Instant.now(),
            completeQuestUseCase: CompleteQuestUseCase = mock() {
                on { execute(any()) } doAnswer { invocation ->
                    (invocation.getArgument(0) as CompleteQuestUseCase.Params.WithQuest).quest
                }
            }
        ): Quest {
            val questRepoMock = mock<QuestRepository> {
                on { findById(any()) } doReturn quest
                on { save(any()) } doAnswer { invocation ->
                    invocation.getArgument(0)
                }
            }

            return CompleteTimeRangeUseCase(
                questRepoMock,
                SplitDurationForPomodoroTimerUseCase(),
                completeQuestUseCase,
                mock()
            ).execute(CompleteTimeRangeUseCase.Params(quest.id, time))
        }

        val simpleQuest = Quest(
            name = "",
            color = Color.BLUE,
            category = Category("Wellness", Color.BLUE),
            scheduledDate = LocalDate.now(),
            duration = 30,
            reminder = Reminder("", Time.now(), LocalDate.now())
        )

        val now = Instant.now()

        it("should end the last time range") {
            val quest = simpleQuest.copy(
                duration = 1.pomodoros() + 1.shortBreaks(),
                pomodoroTimeRanges = listOf(
                    TimeRange(
                        TimeRange.Type.WORK,
                        Constants.DEFAULT_POMODORO_WORK_DURATION,
                        now,
                        now
                    ),
                    TimeRange(
                        TimeRange.Type.BREAK,
                        Constants.DEFAULT_POMODORO_BREAK_DURATION,
                        now,
                        null
                    )
                )
            )
            val result = executeUseCase(quest)
            result.pomodoroTimeRanges.size `should be equal to` (2)
            result.pomodoroTimeRanges.last().end.`should not be null`()
        }

        it("should end the last time range with smaller duration") {
            val quest = simpleQuest.copy(
                duration = 10,
                pomodoroTimeRanges = listOf(
                    TimeRange(
                        TimeRange.Type.WORK,
                        Constants.DEFAULT_POMODORO_WORK_DURATION,
                        now,
                        now
                    ),
                    TimeRange(
                        TimeRange.Type.BREAK,
                        Constants.DEFAULT_POMODORO_BREAK_DURATION,
                        now,
                        null
                    )
                )
            )
            val result = executeUseCase(quest)
            result.pomodoroTimeRanges.size `should be equal to` (2)
            result.pomodoroTimeRanges.last().end.`should not be null`()
        }

        it("should end the last and add new time range") {
            val quest = simpleQuest.copy(
                duration = 2.pomodoros() + 1.shortBreaks(),
                pomodoroTimeRanges = listOf(
                    TimeRange(
                        TimeRange.Type.WORK,
                        Constants.DEFAULT_POMODORO_WORK_DURATION,
                        now,
                        now
                    ),
                    TimeRange(
                        TimeRange.Type.BREAK,
                        Constants.DEFAULT_POMODORO_BREAK_DURATION,
                        now,
                        null
                    )
                )
            )
            val result = executeUseCase(quest)
            result.pomodoroTimeRanges.size `should be equal to` (3)
            result.pomodoroTimeRanges[1].end.`should not be null`()
            val range = result.pomodoroTimeRanges.last()
            range.start.`should not be null`()
            range.end.`should be null`()
        }

        it("should complete quest with countdown timer") {

            val completeQuestUseCaseMock = mock<CompleteQuestUseCase>()

            executeUseCase(
                simpleQuest.copy(
                    actualStart = now
                ),
                completeQuestUseCase = completeQuestUseCaseMock
            )

            Verify on completeQuestUseCaseMock that completeQuestUseCaseMock.execute(any()) was called
        }

        it("should complete quest when pomodoros are complete") {

            val completeQuestUseCaseMock = mock<CompleteQuestUseCase>()

            executeUseCase(
                simpleQuest.copy(
                    duration = 1.pomodoros() + 1.shortBreaks(),
                    pomodoroTimeRanges = listOf(
                        TimeRange(TimeRange.Type.WORK, 1.pomodoros(), now, now),
                        TimeRange(TimeRange.Type.BREAK, 1.shortBreaks(), now, null)
                    )
                ),
                completeQuestUseCase = completeQuestUseCaseMock
            )

            Verify on completeQuestUseCaseMock that completeQuestUseCaseMock.execute(any()) was called
        }

        it("should complete quest with short duration when pomodoros are complete") {

            val completeQuestUseCaseMock = mock<CompleteQuestUseCase>()

            executeUseCase(
                simpleQuest.copy(
                    duration = 1.pomodoros(),
                    pomodoroTimeRanges = listOf(
                        TimeRange(TimeRange.Type.WORK, 1.pomodoros(), now, now),
                        TimeRange(TimeRange.Type.BREAK, 1.shortBreaks(), now, null)
                    )
                ),
                completeQuestUseCase = completeQuestUseCaseMock
            )

            Verify on completeQuestUseCaseMock that completeQuestUseCaseMock.execute(any()) was called
        }

    }
})