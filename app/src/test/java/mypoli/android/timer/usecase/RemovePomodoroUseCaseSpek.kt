package mypoli.android.timer.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import mypoli.android.common.datetime.Time
import mypoli.android.common.datetime.plusMinutes
import mypoli.android.quest.*
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.quest.usecase.RemovePomodoroUseCase
import mypoli.android.quest.usecase.SplitDurationForPomodoroTimerUseCase
import mypoli.android.timer.longBreaks
import mypoli.android.timer.pomodoros
import mypoli.android.timer.shortBreaks
import org.amshove.kluent.`should be equal to`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/19/18.
 */
class RemovePomodoroUseCaseSpek : Spek({

    describe("RemovePomodoroUseCase") {

        fun executeUseCase(
            quest: Quest
        ): Quest {
            val questRepoMock = mock<QuestRepository> {
                on { findById(any()) } doReturn quest
                on { save(any()) } doAnswer { invocation ->
                    invocation.getArgument(0)
                }
            }
            return RemovePomodoroUseCase(
                questRepoMock,
                SplitDurationForPomodoroTimerUseCase()
            )
                .execute(RemovePomodoroUseCase.Params(quest.id))
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

        it("should not remove the last pomodoro") {
            val result = executeUseCase(
                simpleQuest.copy(
                    duration = 1.pomodoros() + 1.shortBreaks()
                )
            )
            result.duration.`should be equal to`(1.pomodoros() + 1.shortBreaks())
        }

        it("should not change quest with short duration") {
            val result = executeUseCase(
                simpleQuest.copy(
                    duration = 10
                )
            )
            result.duration.`should be equal to`(10)
        }

        it("should remove pomodoro duration with short break") {
            val timeRanges = mutableListOf<TimeRange>()

            for (i: Int in 1..2) {
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
                    duration = 2.pomodoros() + 2.shortBreaks(),
                    pomodoroTimeRanges = timeRanges
                )
            )
            result.duration.`should be equal to`(1.pomodoros() + 1.shortBreaks())
        }

        it("should remove pomodoro duration with long break") {
            val timeRanges = mutableListOf<TimeRange>()

            for (i: Int in 1..4) {
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
                    duration = 4.pomodoros() + 3.shortBreaks() + 1.longBreaks(),
                    pomodoroTimeRanges = timeRanges
                )
            )
            result.duration.`should be equal to`(3.pomodoros() + 3.shortBreaks())
        }

        it("should not remove started pomodoro") {

            val duration = 2.pomodoros() + 15 + 3.shortBreaks()
            val result = executeUseCase(
                simpleQuest.copy(
                    duration = duration,
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
                        ),
                        TimeRange(
                            TimeRange.Type.WORK,
                            1.pomodoros(),
                            now
                        )
                    )
                )
            )
            result.duration.`should be equal to`(duration)
        }

        it("should remove pomodoro from not started quest") {
            val result = executeUseCase(
                simpleQuest.copy(
                    duration = 3.pomodoros() + 3.shortBreaks(),
                    pomodoroTimeRanges = listOf()
                )
            )
            result.duration.`should be equal to`(2.pomodoros() + 2.shortBreaks())
        }

    }
})