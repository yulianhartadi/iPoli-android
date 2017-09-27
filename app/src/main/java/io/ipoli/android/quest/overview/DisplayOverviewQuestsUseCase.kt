package io.ipoli.android.quest.overview

import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.quest.data.Quest
import io.ipoli.android.quest.overview.ui.*
import io.ipoli.android.quest.persistence.QuestRepository
import io.reactivex.Observable
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/20/17.
 */
class DisplayOverviewQuestsUseCase(private val questRepository: QuestRepository) : BaseRxUseCase<DisplayOverviewQuestsUseCase.Parameters, OverviewStatePartialChange>() {

    override fun createObservable(parameters: Parameters): Observable<OverviewStatePartialChange> {
        return questRepository.listenForScheduledBetween(parameters.startDate.minusDays(parameters.showCompletedForPastDays), parameters.endDate)
            .map { quests ->
                val comparator = Comparator<Quest> { q1, q2 ->
                    when {
                        q1.scheduledDate!!.isEqual(q2.scheduledDate) -> q1.scheduledDate!!.compareTo(q2.scheduledDate)
                        q1.startMinute == null -> -1
                        q2.startMinute == null -> 1
                        else -> Integer.compare(q1.startMinute!!, q2.startMinute!!)
                    }
                }

                val sortedQuests = quests.sortedWith(comparator)
                val (completedQuests, incompleteQuests) = sortedQuests.partition { it.isCompleted }

                val tomorrow = parameters.today.plusDays(1)

                val (todayQuests, others) = incompleteQuests.partition { it.scheduledDate!!.isEqual(parameters.today) }
                val (tomorrowQuests, otherNonCompleted) = others.partition { it.scheduledDate!!.isEqual(tomorrow) }

                val upcomingQuests = otherNonCompleted.filter { it.scheduledDate!!.isAfter(parameters.today.minusDays(1)) }

                QuestsLoadedPartialChange(toOverviewQuestViewModel(todayQuests),
                    toOverviewQuestViewModel(tomorrowQuests),
                    toOverviewQuestViewModel(upcomingQuests),
                    toOverviewCompletedQuestViewModel(completedQuests)) as OverviewStatePartialChange
            }
            .startWith(QuestsLoadingPartialChange())
    }

    private fun toOverviewCompletedQuestViewModel(quests: List<Quest>): List<OverviewCompletedQuestViewModel> =
        quests.map { OverviewCompletedQuestViewModel.create(it) }

    private fun toOverviewQuestViewModel(quests: List<Quest>) =
        quests.map { OverviewQuestViewModel.create(it) }


    data class Parameters(
        val today: LocalDate,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val showCompletedForPastDays: Long
    )
}
