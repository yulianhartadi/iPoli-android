package io.ipoli.android.quest.overview.ui

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.daggerComponent
import io.ipoli.android.quest.data.Quest
import io.ipoli.android.quest.overview.DisplayOverviewQuestsUseCase
import io.ipoli.android.quest.overview.OverviewPresenter
import io.ipoli.android.quest.overview.di.DaggerOverviewComponent
import io.ipoli.android.quest.overview.di.OverviewComponent
import io.reactivex.Observable
import kotlinx.android.synthetic.main.controller_overview.view.*
import kotlinx.android.synthetic.main.overview_quest_item.view.*
import kotlinx.android.synthetic.main.view_error.view.*
import kotlinx.android.synthetic.main.view_loading.view.*
import org.threeten.bp.LocalDate
import timber.log.Timber


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/20/17.
 */
class OverviewController : BaseController<OverviewController, OverviewPresenter>() {

    private var restoringState: Boolean = false

    lateinit private var questList: RecyclerView

    private lateinit var adapter: OverviewAdapter

    val controllerComponent: OverviewComponent by lazy {
        val component = DaggerOverviewComponent.builder()
            .controllerComponent(daggerComponent)
            .build()
        component.inject(this@OverviewController)
        component
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        controllerComponent
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {

        val view = inflater.inflate(R.layout.controller_overview, container, false)
        questList = view.contentView
        questList.setHasFixedSize(true)
        questList.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)

        val delegatesManager = AdapterDelegatesManager<List<OverviewQuestViewModel>>()
            .addDelegate(OverviewAdapterDelegate(inflater))

        adapter = OverviewAdapter(delegatesManager)

        questList.adapter = adapter

        return view
    }

    override fun setRestoringViewState(restoringViewState: Boolean) {
        this.restoringState = restoringViewState
    }

    override fun createPresenter(): OverviewPresenter = controllerComponent.createPresenter()

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
                Timber.d("Loaded " + state.todayQuests.size)
                view?.loadingView?.visibility = View.GONE
                view?.errorView?.visibility = View.GONE
                view?.contentView?.visibility = View.VISIBLE
                adapter.items = state.todayQuests
                adapter.notifyDataSetChanged()
            }
        }
    }
}

class OverviewAdapter(manager: AdapterDelegatesManager<List<OverviewQuestViewModel>>) :
    ListDelegationAdapter<List<OverviewQuestViewModel>>(manager)

class OverviewAdapterDelegate(private val inflater: LayoutInflater) : AdapterDelegate<List<OverviewQuestViewModel>>() {

    override fun onBindViewHolder(items: List<OverviewQuestViewModel>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as QuestViewHolder
        val questViewModel = items[position]
        vh.bindQuestViewModel(questViewModel)
    }

    override fun isForViewType(items: List<OverviewQuestViewModel>, position: Int): Boolean = true

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

                if (isStarted) {
                    val drawable = itemView.runningIndicator.background as GradientDrawable
                    drawable.setColor(ContextCompat.getColor(itemView.context, categoryColor))
                }
                itemView.runningIndicator.visibility = if (isStarted) View.VISIBLE else View.GONE

                itemView.priorityIndicator.visibility = if (priority == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY) View.VISIBLE else View.GONE
                itemView.repeatingIndicator.visibility = if (isFromRepeatingQuest) View.VISIBLE else View.GONE
                itemView.challengeIndicator.visibility = if (isFromChallenge) View.VISIBLE else View.GONE

//                itemView.description.setText(description)
            }
        }
    }

}


interface OverviewStatePartialChange {
    fun computeNewState(prevState: OverviewViewState): OverviewViewState
}

class QuestsLoadedPartialChange(private val todayQuests: List<OverviewQuestViewModel>,
                                private val tomorrowQuests: List<OverviewQuestViewModel>,
                                private val upcomingQuests: List<OverviewQuestViewModel>,
                                private val completedQuests: List<OverviewQuestViewModel>) : OverviewStatePartialChange {
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
    val completedQuests: List<OverviewQuestViewModel> = listOf()
)

class OverviewLoadingState : OverviewViewState()

class OverviewQuestsLoadedState(todayQuests: List<OverviewQuestViewModel>,
                                tomorrowQuests: List<OverviewQuestViewModel>,
                                upcomingQuests: List<OverviewQuestViewModel>,
                                completedQuests: List<OverviewQuestViewModel>) :
    OverviewViewState(todayQuests, tomorrowQuests, upcomingQuests, completedQuests)

data class OverviewQuestViewModel(
    val name: String,
    val duration: Int,
    @DrawableRes val categoryImage: Int,
    @ColorRes val categoryColor: Int,
    val isFromRepeatingQuest: Boolean,
    val isFromChallenge: Boolean,
    val isStarted: Boolean,
    val priority: Int
) {
    companion object {
        fun fromQuest(quest: Quest): OverviewQuestViewModel =
            OverviewQuestViewModel(quest.name,
                quest.getDuration(),
                quest.categoryType.colorfulImage,
                quest.categoryType.color500,
                quest.isFromRepeatingQuest,
                quest.isFromChallenge,
                quest.isStarted,
                quest.getPriority())
    }
}
