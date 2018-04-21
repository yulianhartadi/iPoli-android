package io.ipoli.android.quest.bucketlist.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest
import org.threeten.bp.LocalDate

class CreateBucketListItemsUseCase :
    UseCase<CreateBucketListItemsUseCase.Params, List<CreateBucketListItemsUseCase.BucketListItem>> {

    override fun execute(parameters: Params): List<BucketListItem> {
        val quests = parameters.quests
        val today = parameters.currentDate
        val tomorrow = today.plusDays(1)
        val (completed, incompleted) = quests.partition { it.isCompleted }
        val (someday, withDueDate) = incompleted.partition { it.dueDate == null }
        val (overdue, notOverdue) = withDueDate.partition { it.dueDate!!.isBefore(today) }
        val (dueToday, dueOtherDay) = notOverdue.partition { it.dueDate == today }
        val (dueTomorrow, upcoming) = dueOtherDay.partition { it.dueDate == tomorrow }
        return createSectionWithQuests(BucketListItem.Overdue, overdue) +
                createSectionWithQuests(BucketListItem.Today, dueToday) +
                createSectionWithQuests(BucketListItem.Tomorrow, dueTomorrow) +
                createSectionWithQuests(BucketListItem.Upcoming, upcoming) +
                createSectionWithQuests(BucketListItem.SomeDay, someday) +
                createSectionWithQuests(BucketListItem.Completed, completed)
    }

    private fun createSectionWithQuests(
        sectionItem: BucketListItem,
        quests: List<Quest>
    ): List<BucketListItem> {
        if (quests.isEmpty()) {
            return emptyList()
        }
        val items = mutableListOf(sectionItem)
        items.addAll(quests.map { BucketListItem.QuestItem(it) })
        return items
    }

    data class Params(val quests: List<Quest>, val currentDate: LocalDate = LocalDate.now())

    sealed class BucketListItem {

        data class QuestItem(val quest: Quest) : BucketListItem()
        object Overdue : BucketListItem()
        object Today : BucketListItem()
        object Tomorrow : BucketListItem()
        object Upcoming : BucketListItem()
        object SomeDay : BucketListItem()
        object Completed : BucketListItem()
    }
}