package io.ipoli.android.repeatingquest.list.usecase

import io.ipoli.android.common.SimpleRxUseCase
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.isBetween
import io.ipoli.android.quest.data.Quest
import io.ipoli.android.repeatingquest.data.Recurrence
import io.ipoli.android.repeatingquest.data.RepeatingQuest
import io.ipoli.android.repeatingquest.list.ui.RepeatingQuestViewModel
import io.ipoli.android.repeatingquest.list.usecase.data.PeriodHistory
import io.ipoli.android.repeatingquest.persistence.RepeatingQuestRepository
import io.reactivex.Observable
import org.threeten.bp.LocalDate


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/22/17.
 */
class DisplayRepeatingQuestListUseCase(private val repeatingQuestRepository: RepeatingQuestRepository) : SimpleRxUseCase<RepeatingQuestListViewState>() {

    override fun createObservable(params: Unit): Observable<RepeatingQuestListViewState> {
        return repeatingQuestRepository.listenForAll()
            .map { repeatingQuests ->
                val viewModels = repeatingQuests.map {
                    createViewModel(it)
                }
                if (viewModels.isEmpty()) {
                    RepeatingQuestListViewState.Empty()
                } else {
                    RepeatingQuestListViewState.DataLoaded(viewModels)
                }
            }
            .cast(RepeatingQuestListViewState::class.java)
            .startWith(RepeatingQuestListViewState.Loading())
            .onErrorReturn { RepeatingQuestListViewState.Error(it) }
    }

    private fun createViewModel(repeatingQuest: RepeatingQuest): RepeatingQuestViewModel {
        val history = getPeriodHistories(repeatingQuest)
        val currentPeriod = history.last()

        return RepeatingQuestViewModel(repeatingQuest.name!!,
            repeatingQuest.categoryType.colorfulImage,
            repeatingQuest.categoryType.color500,
            getNextScheduledDate(repeatingQuest.quests),
            repeatingQuest.getDuration(),
            repeatingQuest.startTime,
            currentPeriod.scheduledCount,
            currentPeriod.completedCount,
            currentPeriod.remainingCount,
            repeatingQuest.recurrence.recurrenceType)
    }

    private fun getNextScheduledDate(quests: List<Quest>): LocalDate? {
        var nextDate: LocalDate? = null
        val currentDate = LocalDate.now()
        for (quest in quests) {
            if (!quest.isCompleted && quest.scheduledDate != null && !currentDate.isAfter(quest.scheduledDate)) {
                if (nextDate == null || nextDate.isAfter(quest.scheduledDate)) {
                    nextDate = quest.scheduledDate
                }
            }
        }
        return nextDate
    }

    private fun getPeriodHistories(repeatingQuest: RepeatingQuest): List<PeriodHistory> {

        val currentDate = LocalDate.now()

        val result = ArrayList<PeriodHistory>()
        val frequency = repeatingQuest.frequency
        val pairs = if (repeatingQuest.recurrence.recurrenceType === Recurrence.RepeatType.MONTHLY)
            DateUtils.getBoundsFor4MonthsInThePast(currentDate)
        else
            DateUtils.getBoundsFor4WeeksInThePast(currentDate)

        for (p in pairs) {
            result.add(PeriodHistory(p.first, p.second, frequency))
        }

        for (qd in repeatingQuest.quests) {
            for (p in result) {
                val scheduledDate = qd.scheduledDate ?: continue
                if (scheduledDate.isBetween(p.startDate, p.endDate)) {
                    if (qd.isCompleted) {
                        p.increaseCompletedCount()
                    }
                    p.increaseScheduledCount()
                    break
                }
            }
        }

        return result
    }
}