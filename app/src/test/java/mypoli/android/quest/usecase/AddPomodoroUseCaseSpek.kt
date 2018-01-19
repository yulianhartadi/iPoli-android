package mypoli.android.quest.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import mypoli.android.common.datetime.Time
import mypoli.android.quest.*
import mypoli.android.quest.data.persistence.QuestRepository
import org.amshove.kluent.`should be equal to`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/19/18.
 */
class AddPomodoroUseCaseSpek : Spek({
    describe("AddPomodoroUseCase") {
        fun executeUseCase(
            quest: Quest
        ): Quest {
            val questRepoMock = mock<QuestRepository> {
                on { findById(any()) } doReturn quest
                on { save(any()) } doAnswer { invocation ->
                    invocation.getArgument(0)
                }
            }
            return AddPomodoroUseCase(questRepoMock)
                .execute(AddPomodoroUseCase.Params(quest.id))
        }

        val simpleQuest = Quest(
            name = "",
            color = Color.BLUE,
            category = Category("Wellness", Color.BLUE),
            scheduledDate = LocalDate.now(),
            duration = 30,
            reminder = Reminder("", Time.now(), LocalDate.now())
        )

        it("should add duration of 1 pomodoro with short break") {
            val result = executeUseCase(
                simpleQuest.copy(
                    duration = 1.pomodoros() + 1.shortBreaks()
                )
            )
            result.duration.`should be equal to`(2.pomodoros() + 2.shortBreaks())
        }

        it("should add pomodoro duration to actual duration") {
            val now = LocalDateTime.now()
            val result = executeUseCase(
                simpleQuest.copy(
                    duration = 10,
                    pomodoroTimeRanges = listOf(
                        TimeRange(
                            TimeRange.Type.WORK,
                            1.pomodoros(),
                            now,
                            now.plusMinutes(1.pomodoros().toLong())
                        ),
                        TimeRange(
                            TimeRange.Type.BREAK,
                            1.shortBreaks(),
                            now,
                            now.plusMinutes(1.shortBreaks().toLong())
                        )
                    )
                )
            )
            result.duration.`should be equal to`(2.pomodoros() + 2.shortBreaks())
        }

        it("should add pomodoro duration with long break") {
            val now = LocalDateTime.now()
            val timeRanges = mutableListOf<TimeRange>()

            for (i: Int in 1..7) {
                if (i % 2 == 1) {
                    timeRanges.add(
                        TimeRange(
                            TimeRange.Type.WORK,
                            1.pomodoros(),
                            now,
                            now.plusMinutes(1.pomodoros().toLong())
                        )
                    )
                } else {
                    timeRanges.add(
                        TimeRange(
                            TimeRange.Type.BREAK,
                            1.shortBreaks(),
                            now,
                            now.plusMinutes(1.shortBreaks().toLong())
                        )
                    )
                }
            }

            val result = executeUseCase(
                simpleQuest.copy(
                    duration = 4.pomodoros() + 3.shortBreaks(),
                    pomodoroTimeRanges = timeRanges
                )
            )
            result.duration.`should be equal to`(5.pomodoros() + 3.shortBreaks() + 1.longBreaks())
        }
    }
})