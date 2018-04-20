package io.ipoli.android.quest.show.usecase

import io.ipoli.android.TestUtil
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.TimeRange
import io.ipoli.android.quest.show.longBreaks
import io.ipoli.android.quest.show.pomodoros
import io.ipoli.android.quest.show.shortBreaks
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldNotBeNull
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.Instant

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 1/16/18.
 */
class SplitDurationForPomodoroTimerUseCaseSpek : Spek({

    describe("SplitDurationForPomodoroTimerUseCase") {

        fun executeUseCase(quest: Quest) =
            SplitDurationForPomodoroTimerUseCase().execute(
                SplitDurationForPomodoroTimerUseCase.Params(
                    quest
                )
            )

        val simpleQuest = TestUtil.quest

        it("should be split into 1 work and 1 break") {
            val quest = simpleQuest.copy(
                timeRanges = listOf(),
                duration = 30
            )
            val result = executeUseCase(quest)
            val ranges =
                (result as SplitDurationForPomodoroTimerUseCase.Result.DurationSplit).timeRanges
            ranges.size.`should be equal to`(2)
            ranges.first().type.`should be`(TimeRange.Type.POMODORO_WORK)
            ranges.last().`should be short break`()
        }

        it("should not be split") {
            val quest = simpleQuest.copy(
                timeRanges = listOf(),
                duration = 10
            )
            val result = executeUseCase(quest)
            result.`should be`(SplitDurationForPomodoroTimerUseCase.Result.DurationNotSplit)
        }

        it("should be split into 2 work and 1 break ranges with 1 work shorter") {
            val quest = simpleQuest.copy(
                timeRanges = listOf(),
                duration = 40
            )
            val result = executeUseCase(quest)
            val ranges =
                (result as SplitDurationForPomodoroTimerUseCase.Result.DurationSplit).timeRanges
            ranges.size.`should be equal to`(3)
            val shorterWork = ranges.last()
            shorterWork.type.`should be`(TimeRange.Type.POMODORO_WORK)
            shorterWork.duration.`should be equal to`(quest.duration - 1.shortBreaks() - 1.pomodoros())
        }

        it("should be split into 2 work and 2 break ranges") {
            val quest = simpleQuest.copy(
                timeRanges = listOf(),
                duration = 60
            )
            val result = executeUseCase(quest)
            val ranges =
                (result as SplitDurationForPomodoroTimerUseCase.Result.DurationSplit).timeRanges
            ranges.size.`should be equal to`(4)
            ranges.last().`should be short break`()
        }

        it("should be split into 4 work and 4 break ranges with 1 long break") {
            val quest = simpleQuest.copy(
                timeRanges = listOf(),
                duration = 2 * 60 + 10
            )
            val result = executeUseCase(quest)
            val ranges =
                (result as SplitDurationForPomodoroTimerUseCase.Result.DurationSplit).timeRanges
            ranges.size.`should be equal to`(8)
            ranges.last().`should be long break`()
        }

        it("should be split into 4 work and 4 break ranges with 1 long incomplete break") {
            val quest = simpleQuest.copy(
                timeRanges = listOf(),
                duration = 2 * 60
            )
            val result = executeUseCase(quest)
            val ranges =
                (result as SplitDurationForPomodoroTimerUseCase.Result.DurationSplit).timeRanges
            ranges.size.`should be equal to`(8)
            ranges.last().type.`should be`(TimeRange.Type.POMODORO_LONG_BREAK)
            ranges.last()
                .duration.`should be equal to`(quest.duration - (4.pomodoros() + 3.shortBreaks()))
        }

        it("should be split into 8 work and 8 break ranges with 2 long breaks") {
            val quest = simpleQuest.copy(
                timeRanges = listOf(),
                duration = 8.pomodoros() + 6.shortBreaks() + 2.longBreaks()
            )
            val result = executeUseCase(quest)
            val ranges =
                (result as SplitDurationForPomodoroTimerUseCase.Result.DurationSplit).timeRanges
            ranges.size.`should be equal to`(16)
            ranges[7].`should be long break`()
            ranges.last().`should be long break`()
        }

        it("should not add work or breaks") {
            val quest = simpleQuest.copy(
                timeRanges = listOf(
                    TimeRange(
                        TimeRange.Type.POMODORO_WORK,
                        1.pomodoros(),
                        Instant.now(),
                        Instant.now()
                    ),
                    TimeRange(
                        TimeRange.Type.POMODORO_SHORT_BREAK,
                        1.shortBreaks(),
                        Instant.now(),
                        Instant.now()
                    )
                ),
                duration = 1.pomodoros() + 1.shortBreaks()
            )

            val result = executeUseCase(quest)
            val ranges =
                (result as SplitDurationForPomodoroTimerUseCase.Result.DurationSplit).timeRanges
            ranges.size.`should be equal to`(2)
            ranges.first().end.shouldNotBeNull()
            ranges.last().start.shouldNotBeNull()
        }

        it("should add completed pomodoros when not enough duration") {
            val quest = simpleQuest.copy(
                timeRanges = listOf(
                    TimeRange(
                        TimeRange.Type.POMODORO_WORK,
                        1.pomodoros(),
                        Instant.now(),
                        Instant.now()
                    ),
                    TimeRange(
                        TimeRange.Type.POMODORO_SHORT_BREAK,
                        1.shortBreaks(),
                        Instant.now(),
                        Instant.now()
                    )
                ),
                duration = 1.shortBreaks()
            )
            val result = executeUseCase(quest)
            val ranges =
                (result as SplitDurationForPomodoroTimerUseCase.Result.DurationSplit).timeRanges
            ranges.size.`should be equal to`(2)
            ranges.first().end.shouldNotBeNull()
            ranges.last().start.shouldNotBeNull()
        }
    }
})

private fun TimeRange.`should be short break`() {
    type.`should be`(TimeRange.Type.POMODORO_SHORT_BREAK)
}

private fun TimeRange.`should be long break`() {
    type.`should be`(TimeRange.Type.POMODORO_LONG_BREAK)
}
