package io.ipoli.android.quest.overview.ui

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.text.DateFormatter
import io.ipoli.android.common.text.ScheduleTextFormatter
import io.ipoli.android.common.daggerComponent
import io.ipoli.android.quest.data.Quest
import io.ipoli.android.quest.overview.DisplayOverviewQuestsUseCase
import io.ipoli.android.quest.overview.OverviewPresenter
import io.ipoli.android.quest.overview.di.DaggerOverviewComponent
import io.ipoli.android.quest.overview.di.OverviewComponent
import io.reactivex.Observable
import kotlinx.android.synthetic.main.controller_overview.view.*
import kotlinx.android.synthetic.main.overview_completed_quest_item.view.*
import kotlinx.android.synthetic.main.overview_quest_item.view.*
import kotlinx.android.synthetic.main.view_error.view.*
import kotlinx.android.synthetic.main.view_loading.view.*
import org.threeten.bp.LocalDate


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/20/17.
 */
class OverviewController : BaseController<OverviewController, OverviewPresenter, OverviewComponent>() {

    lateinit private var questList: RecyclerView

    private lateinit var adapter: OverviewAdapter

    override fun buildComponent(): OverviewComponent =
        DaggerOverviewComponent.builder()
            .controllerComponent(daggerComponent)
            .build()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {

        val view = inflater.inflate(R.layout.controller_overview, container, false)
        questList = view.contentView
        questList.setHasFixedSize(true)
        questList.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)

        val delegatesManager = AdapterDelegatesManager<List<Any>>()
            .addDelegate(HeaderDelegate(inflater))
            .addDelegate(QuestViewModelDelegate(inflater))
            .addDelegate(CompletedQuestViewModelDelegate(inflater))

        adapter = OverviewAdapter(delegatesManager)

        questList.adapter = adapter

        return view
    }

    fun loadQuestsIntent(): Observable<DisplayOverviewQuestsUseCase.Parameters> =
        Observable.just(!restoringState).filter { _ -> true }.
            map {
                DisplayOverviewQuestsUseCase.Parameters(
                    LocalDate.now(),
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    1
                )
            }

    fun render(state: OverviewViewState) {
        when (state) {
            is OverviewLoadingState -> {
                view?.loadingView?.visibility = View.VISIBLE
                view?.errorView?.visibility = View.GONE
                view?.contentView?.visibility = View.GONE
            }
            is OverviewQuestsLoadedState -> {
                view?.loadingView?.visibility = View.GONE
                view?.errorView?.visibility = View.GONE
                view?.contentView?.visibility = View.VISIBLE

                val todayList = prependHeaderIfNotEmpty(state.todayQuests, R.string.today)
                val tomorrowList = prependHeaderIfNotEmpty(state.tomorrowQuests, R.string.tomorrow)
                val upcomingList = prependHeaderIfNotEmpty(state.upcomingQuests, R.string.next_7_days)
                val completedList = prependHeaderIfNotEmpty(state.completedQuests, R.string.completed)

                adapter.items = todayList + tomorrowList + upcomingList + completedList
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun prependHeaderIfNotEmpty(quests: List<Any>, @StringRes title: Int): List<Any> =
        if (quests.isNotEmpty()) listOf(HeaderItem(title)) + quests else quests
}

data class HeaderItem(@StringRes val title: Int)

class OverviewAdapter(manager: AdapterDelegatesManager<List<Any>>) :
    ListDelegationAdapter<List<Any>>(manager)

class HeaderDelegate(private val inflater: LayoutInflater) : AdapterDelegate<List<Any>>() {
    override fun isForViewType(items: List<Any>, position: Int): Boolean =
        items[position] is HeaderItem

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder =
        HeaderViewHolder(inflater.inflate(R.layout.overview_header_item, parent, false))

    override fun onBindViewHolder(items: List<Any>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as HeaderDelegate.HeaderViewHolder
        val headerItem = items[position] as HeaderItem
        val headerView = vh.itemView as TextView
        headerView.setText(headerItem.title)
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)
}

class CompletedQuestViewModelDelegate(private val inflater: LayoutInflater) : AdapterDelegate<List<Any>>() {
    override fun isForViewType(items: List<Any>, position: Int): Boolean =
        items[position] is OverviewCompletedQuestViewModel

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder =
        CompletedQuestViewModelHolder(inflater.inflate(R.layout.overview_completed_quest_item, parent, false))

    override fun onBindViewHolder(items: List<Any>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as CompletedQuestViewModelHolder
        val viewModel = items[position] as OverviewCompletedQuestViewModel
        vh.bind(viewModel)
    }

    inner class CompletedQuestViewModelHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(viewModel: OverviewCompletedQuestViewModel) {
            itemView.completedQuestName.text = viewModel.name
            itemView.completedQuestDueDate.text = DateFormatter.formatWithoutYear(itemView.context, viewModel.dueDate, LocalDate.now())
        }
    }
}

class QuestViewModelDelegate(private val inflater: LayoutInflater) : AdapterDelegate<List<Any>>() {

    override fun onBindViewHolder(items: List<Any>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as QuestViewHolder
        val questViewModel = items[position]
        vh.bindQuestViewModel(questViewModel as OverviewQuestViewModel)
    }

    override fun isForViewType(items: List<Any>, position: Int): Boolean =
        items[position] is OverviewQuestViewModel

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder =
        QuestViewHolder(inflater.inflate(R.layout.overview_quest_item, parent, false))

    inner class QuestViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bindQuestViewModel(overviewQuestViewModel: OverviewQuestViewModel) {
            with(overviewQuestViewModel) {
                //                RxView.clicks(itemView.buyReward).takeUntil(RxView.detaches(itemView)).map { reward }.subscribe(clickSubject)
//                RxView.clicks(itemView.delete).takeUntil(RxView.detaches(itemView)).map { reward }.subscribe(deleteSubject)
//                itemView.setOnClickListener { clickListener(reward) }
                itemView.name.text = name
                itemView.scheduleInfo.text = duration.toString()
                itemView.categoryImage.setImageResource(categoryImage)

                if (isForToday || isForTomorrow) {
                    itemView.dueDate.visibility = View.GONE
                } else {
                    itemView.dueDate.visibility = View.VISIBLE
                    itemView.dueDate.text = DateFormatter.formatWithoutYear(itemView.context, dueDate, LocalDate.now())
                }

                if (isStarted) {
                    val drawable = itemView.runningIndicator.background as GradientDrawable
                    drawable.setColor(ContextCompat.getColor(itemView.context, categoryColor))
                }
                itemView.runningIndicator.visibility = if (isStarted) View.VISIBLE else View.GONE

                itemView.priorityIndicator.visibility = if (priority == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY) View.VISIBLE else View.GONE
                itemView.repeatingIndicator.visibility = if (isFromRepeatingQuest) View.VISIBLE else View.GONE
                itemView.challengeIndicator.visibility = if (isFromChallenge) View.VISIBLE else View.GONE

                itemView.scheduleInfo.text = ScheduleTextFormatter(true).format(overviewQuestViewModel, itemView.context)

                itemView.scheduleInfo.visibility = if (itemView.scheduleInfo.text.isNotEmpty()) View.VISIBLE else View.GONE

                itemView.moreMenu.setOnClickListener { button ->
                    showPopupMenu(overviewQuestViewModel, button)
                }
            }
        }

        private fun showPopupMenu(model: OverviewQuestViewModel, button: View) {
            val pm = PopupMenu(itemView.context, button)
            pm.inflate(R.menu.overview_actions_menu)

            val startItem = pm.menu.findItem(R.id.quest_start)
            startItem.setTitle(if (model.isStarted) R.string.stop else R.string.start)

            val scheduleQuestItem = pm.menu.findItem(R.id.schedule_quest)
            scheduleQuestItem.setTitle(if (model.isForToday) R.string.snooze_for_tomorrow else R.string.do_today)

            pm.show()
        }
    }
}

interface OverviewStatePartialChange {
    fun computeNewState(prevState: OverviewViewState): OverviewViewState
}

class QuestsLoadedPartialChange(private val todayQuests: List<OverviewQuestViewModel>,
                                private val tomorrowQuests: List<OverviewQuestViewModel>,
                                private val upcomingQuests: List<OverviewQuestViewModel>,
                                private val completedQuests: List<OverviewCompletedQuestViewModel>) : OverviewStatePartialChange {
    override fun computeNewState(prevState: OverviewViewState) =
        OverviewQuestsLoadedState(todayQuests, tomorrowQuests, upcomingQuests, completedQuests)
}

class QuestsLoadingPartialChange : OverviewStatePartialChange {
    override fun computeNewState(prevState: OverviewViewState): OverviewViewState =
        OverviewLoadingState()
}

open class OverviewViewState(
    val todayQuests: List<OverviewQuestViewModel> = listOf(),
    val tomorrowQuests: List<OverviewQuestViewModel> = listOf(),
    val upcomingQuests: List<OverviewQuestViewModel> = listOf(),
    val completedQuests: List<OverviewCompletedQuestViewModel> = listOf()
)

class OverviewLoadingState : OverviewViewState()

class OverviewQuestsLoadedState(todayQuests: List<OverviewQuestViewModel>,
                                tomorrowQuests: List<OverviewQuestViewModel>,
                                upcomingQuests: List<OverviewQuestViewModel>,
                                completedQuests: List<OverviewCompletedQuestViewModel>) :
    OverviewViewState(todayQuests, tomorrowQuests, upcomingQuests, completedQuests)

data class OverviewCompletedQuestViewModel(val name: String,
                                           val dueDate: LocalDate) {
    companion object {
        fun create(quest: Quest): OverviewCompletedQuestViewModel =
            OverviewCompletedQuestViewModel(quest.name, quest.completedAtDate!!)
    }
}

data class OverviewQuestViewModel(
    val name: String,
    val duration: Int,
    val startTime: Time?,
    val dueDate: LocalDate,
    @DrawableRes val categoryImage: Int,
    @ColorRes val categoryColor: Int,
    val isFromRepeatingQuest: Boolean,
    val isFromChallenge: Boolean,
    val isStarted: Boolean,
    val priority: Int,
    val isForToday: Boolean,
    val isForTomorrow: Boolean
) {
    companion object {
        fun create(quest: Quest): OverviewQuestViewModel {
            val startTime = if (quest.startMinute == null) {
                null
            } else Time.of(quest.startMinute!!)

            return OverviewQuestViewModel(
                quest.name,
                quest.getDuration(),
                startTime,
                quest.scheduledDate!!,
                quest.categoryType.colorfulImage,
                quest.categoryType.color500,
                quest.isFromRepeatingQuest,
                quest.isFromChallenge,
                quest.isStarted,
                quest.getPriority(),
                quest.scheduledDate!!.isEqual(LocalDate.now()),
                quest.scheduledDate!!.isEqual(LocalDate.now().plusDays(1))
            )
        }
    }
}
