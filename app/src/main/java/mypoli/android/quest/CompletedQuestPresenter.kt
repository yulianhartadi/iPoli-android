package mypoli.android.quest

import mypoli.android.common.datetime.minutes
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.common.view.AndroidColor
import mypoli.android.common.view.AndroidIcon
import mypoli.android.quest.CompletedQuestViewState.StateType.DATA_LOADED
import mypoli.android.quest.CompletedQuestViewState.StateType.LOADING
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.timer.usecase.SplitDurationForPomodoroTimerUseCase
import mypoli.android.timer.usecase.SplitDurationForPomodoroTimerUseCase.Result.DurationNotSplit
import mypoli.android.timer.usecase.SplitDurationForPomodoroTimerUseCase.Result.DurationSplit
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/24/18.
 */
class CompletedQuestPresenter(
    private val questRepository: QuestRepository,
    private val splitDurationForPomodoroTimerUseCase: SplitDurationForPomodoroTimerUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<CompletedQuestViewState>, CompletedQuestViewState, CompletedQuestIntent>(
    CompletedQuestViewState(LOADING),
    coroutineContext
) {
    override fun reduceState(
        intent: CompletedQuestIntent,
        state: CompletedQuestViewState
    ): CompletedQuestViewState =
        when (intent) {
            is CompletedQuestIntent.LoadData -> {

                val quest = questRepository.findById(intent.questId)!!

                val timer = if (!quest.hasTimer) {
                    CompletedQuestViewState.Timer.Untracked(quest.duration.minutes)
                } else if (quest.hasPomodoroTimer) {
                    val timeRanges = quest.timeRanges

                    val completedCnt = timeRanges.filter { it.end != null }.size / 2

                    val splitResult = splitDurationForPomodoroTimerUseCase.execute(
                        SplitDurationForPomodoroTimerUseCase.Params(quest)
                    )
                    val totalCnt =
                        if (splitResult == DurationNotSplit) {
                            timeRanges.size / 2
                        } else {
                            (splitResult as DurationSplit).timeRanges.size / 2
                        }

                    val work = timeRanges.filter { it.type == TimeRange.Type.POMODORO_WORK }
                    val workDuration = work.map { it.duration }.sum()
                    val workActualDuration =
                        work.map { it.actualDuration() }.sumBy { it.asMinutes.intValue }

                    val breaks =
                        timeRanges.filter { it.type == TimeRange.Type.POMODORO_LONG_BREAK || it.type == TimeRange.Type.POMODORO_SHORT_BREAK }

                    val breakDuration = breaks.map { it.duration }.sum()

                    val breakActualDuration =
                        breaks.map { it.actualDuration() }.sumBy { it.asMinutes.intValue }

                    CompletedQuestViewState.Timer.Pomodoro(
                        completedPomodoros = completedCnt,
                        totalPomodoros = totalCnt,
                        workDuration = workActualDuration.minutes,
                        overdueWorkDuration = workActualDuration.minutes - workDuration.minutes,
                        breakDuration = breakActualDuration.minutes,
                        overdueBreakDuration = breakActualDuration.minutes - breakDuration.minutes
                    )
                } else {
                    CompletedQuestViewState.Timer.Countdown(
                        quest.duration.minutes,
                        quest.actualDuration.asMinutes - quest.duration.minutes
                    )
                }

                state.copy(
                    type = DATA_LOADED,
                    name = quest.name,
                    icon = quest.icon?.let {
                        AndroidIcon.valueOf(it.name)
                    },
                    color = AndroidColor.valueOf(quest.color.name),
                    completeAt = quest.completedAtDate!!,
                    startedAt = quest.startTime,
                    finishedAt = quest.completedAtTime,
                    timer = timer,
                    experience = quest.experience,
                    coins = quest.coins,
                    bounty = quest.bounty.let {
                        if (it is Quest.Bounty.Food) {
                            it.food
                        } else {
                            null
                        }
                    }
                )
            }
        }

}