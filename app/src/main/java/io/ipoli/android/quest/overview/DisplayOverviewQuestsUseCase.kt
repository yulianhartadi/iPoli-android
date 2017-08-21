package io.ipoli.android.quest.overview

import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.quest.data.Quest
import io.ipoli.android.quest.overview.ui.OverviewQuestViewModel
import io.ipoli.android.quest.overview.ui.OverviewStatePartialChange
import io.ipoli.android.quest.overview.ui.QuestsLoadedPartialChange
import io.ipoli.android.quest.overview.ui.QuestsLoadingPartialChange
import io.ipoli.android.quest.persistence.QuestRepository
import io.reactivex.Observable
import org.threeten.bp.LocalDate
import timber.log.Timber

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/20/17.
 */
class DisplayOverviewQuestsUseCase(private val questRepository: QuestRepository) : BaseRxUseCase<DisplayOverviewQuestsUseCase.Parameters, OverviewStatePartialChange>() {

    override fun createObservable(params: Parameters): Observable<OverviewStatePartialChange> {
        return questRepository.findScheduledBetween(params.startDate.minusDays(params.showCompletedForPastDays), params.endDate)
            .map { quests ->
                Timber.d(" Quests size " + quests.size)
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

                val tomorrow = params.today.plusDays(1)

                val (todayQuests, others) = incompleteQuests.partition { it.scheduledDate!!.isEqual(params.today) }
                val (tomorrowQuests, upcomingQuests) = others.partition { it.scheduledDate!!.isEqual(tomorrow) }

                QuestsLoadedPartialChange(toOverviewQuestViewModel(todayQuests),
                    toOverviewQuestViewModel(tomorrowQuests),
                    toOverviewQuestViewModel(upcomingQuests),
                    toOverviewQuestViewModel(completedQuests)) as OverviewStatePartialChange
            }
            .startWith(QuestsLoadingPartialChange())
    }

    private fun toOverviewQuestViewModel(quests: List<Quest>) =
        quests.map { OverviewQuestViewModel.create(it) }


    data class Parameters(
        val today: LocalDate,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val showCompletedForPastDays: Long
    )
}
