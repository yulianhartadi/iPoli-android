package mypoli.android.quest.usecase

import mypoli.android.Constants
import mypoli.android.common.datetime.Time
import mypoli.android.quest.*
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/16/18.
 */
class SplitQuestToPomodorosUseCaseSpek : Spek({

    describe("SplitQuestToPomodorosUseCase") {

        fun executeUseCase(quest: Quest) =
            SplitQuestToPomodorosUseCase().execute(SplitQuestToPomodorosUseCase.Params(quest))

        val simpleQuest = Quest(
            name = "",
            color = Color.BLUE,
            category = Category("Wellness", Color.BLUE),
            scheduledDate = LocalDate.now(),
            duration = 30,
            reminder = Reminder("", Time.now(), LocalDate.now())
        )

        it("should be split into 1 work and 1 break") {
            val quest = simpleQuest.copy(
                pomodoroTimeRanges = listOf(),
                duration = 30
            )
            val result = executeUseCase(quest)
            val ranges = (result as SplitQuestToPomodorosUseCase.Result.DurationSplit).timeRanges
            ranges.size.`should be equal to`(2)
            ranges[0].type.`should be`(TimeRange.Type.WORK)
            ranges[1].type.`should be`(TimeRange.Type.BREAK)
        }

        it("should not be split") {
            val quest = simpleQuest.copy(
                pomodoroTimeRanges = listOf(),
                duration = 10
            )
            val result = executeUseCase(quest)
            result.`should be`(SplitQuestToPomodorosUseCase.Result.DurationNotSplit)
        }

        it("should be split into 2 work and 1 break ranges with 1 work shorter") {
            val quest = simpleQuest.copy(
                pomodoroTimeRanges = listOf(),
                duration = 40
            )
            val result = executeUseCase(quest)
            val ranges = (result as SplitQuestToPomodorosUseCase.Result.DurationSplit).timeRanges
            ranges.size.`should be equal to`(3)
            val shorterWork = ranges[2]
            shorterWork.type.`should be`(TimeRange.Type.WORK)
            shorterWork.duration.`should be equal to`(quest.duration - Constants.DEFAULT_POMODORO_BREAK_DURATION - Constants.DEFAULT_POMODORO_WORK_DURATION)
        }

        it("should be split into 2 work and 2 break ranges") {
            val quest = simpleQuest.copy(
                pomodoroTimeRanges = listOf(),
                duration = 60
            )
            val result = executeUseCase(quest)
            val ranges = (result as SplitQuestToPomodorosUseCase.Result.DurationSplit).timeRanges
            ranges.size.`should be equal to`(4)
            ranges[3].type.`should be`(TimeRange.Type.BREAK)
        }

        it("should be split into 4 work and 4 break ranges with 1 long break") {
            val quest = simpleQuest.copy(
                pomodoroTimeRanges = listOf(),
                duration = 2 * 60 + 10
            )
            val result = executeUseCase(quest)
            val ranges = (result as SplitQuestToPomodorosUseCase.Result.DurationSplit).timeRanges
            ranges.size.`should be equal to`(8)
            ranges[7].type.`should be`(TimeRange.Type.BREAK)
            ranges[7].duration.`should be equal to`(Constants.DEFAULT_POMODORO_LONG_BREAK_DURATION)
        }

        it("should be split into 4 work and 4 break ranges with 1 long incomplete break") {
            val quest = simpleQuest.copy(
                pomodoroTimeRanges = listOf(),
                duration = 2 * 60
            )
            val result = executeUseCase(quest)
            val ranges = (result as SplitQuestToPomodorosUseCase.Result.DurationSplit).timeRanges
            ranges.size.`should be equal to`(8)
            ranges[7].type.`should be`(TimeRange.Type.BREAK)
            ranges[7].duration.`should be equal to`(quest.duration - (Constants.DEFAULT_POMODORO_WORK_DURATION * 4 + Constants.DEFAULT_POMODORO_BREAK_DURATION * 3))
        }

        it("should be split into 8 work and 8 break ranges with 2 long breaks") {
            val quest = simpleQuest.copy(
                pomodoroTimeRanges = listOf(),
                duration = 8 * Constants.DEFAULT_POMODORO_WORK_DURATION + 6 * Constants.DEFAULT_POMODORO_BREAK_DURATION + 2 * Constants.DEFAULT_POMODORO_LONG_BREAK_DURATION
            )
            val result = executeUseCase(quest)
            val ranges = (result as SplitQuestToPomodorosUseCase.Result.DurationSplit).timeRanges
            ranges.size.`should be equal to`(16)
            ranges[7].type.`should be`(TimeRange.Type.BREAK)
            ranges[7].duration.`should be equal to`(Constants.DEFAULT_POMODORO_LONG_BREAK_DURATION)
            ranges.last().type.`should be`(TimeRange.Type.BREAK)
            ranges.last().duration.`should be equal to`(Constants.DEFAULT_POMODORO_LONG_BREAK_DURATION)
        }

    }
})