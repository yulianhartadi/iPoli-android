package io.ipoli.android.repeatingquest.list.ui

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.daggerComponent
import io.ipoli.android.common.text.NextScheduledDateFormatter
import io.ipoli.android.common.text.PeriodProgressFormatter
import io.ipoli.android.repeatingquest.list.RepeatingQuestListPresenter
import io.ipoli.android.repeatingquest.list.di.DaggerRepeatingQuestListComponent
import io.ipoli.android.repeatingquest.list.di.RepeatingQuestListComponent
import io.ipoli.android.repeatingquest.list.usecase.RepeatingQuestListViewState
import io.reactivex.Observable
import kotlinx.android.synthetic.main.controller_repeating_quest_list.view.*
import kotlinx.android.synthetic.main.item_repeating_quest.view.*
import kotlinx.android.synthetic.main.view_empty.view.*
import kotlinx.android.synthetic.main.view_error.view.*
import kotlinx.android.synthetic.main.view_loading.view.*
import timber.log.Timber

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/22/17.
 */
class RepeatingQuestListController : BaseController<RepeatingQuestListController, RepeatingQuestListPresenter, RepeatingQuestListComponent>() {

    lateinit private var questList: RecyclerView

    private lateinit var adapter: RepeatingQuestAdapter

    override fun buildComponent(): RepeatingQuestListComponent =
        DaggerRepeatingQuestListComponent.builder()
            .controllerComponent(daggerComponent)
            .build()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_repeating_quest_list, container, false)

        questList = view.questList
        questList.setHasFixedSize(true)
        questList.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)

        val delegatesManager = AdapterDelegatesManager<List<RepeatingQuestViewModel>>()
            .addDelegate(RepeatingQuestViewModelAdapterDelegate(inflater))

        adapter = RepeatingQuestAdapter(delegatesManager)

        questList.adapter = adapter
        return view
    }

    fun loadRepeatingQuestsIntent(): Observable<Boolean> =
        Observable.just(creatingState).filter { _ -> true }

    fun render(state: RepeatingQuestListViewState) {
        val contentView = view!!
        when (state) {

            is RepeatingQuestListViewState.Loading -> {
                contentView.loadingView.visibility = View.VISIBLE
                contentView.emptyView.visibility = View.GONE
                contentView.errorView.visibility = View.GONE
                contentView.questList.visibility = View.GONE
            }

            is RepeatingQuestListViewState.Empty -> {
                contentView.emptyView.visibility = View.VISIBLE
                contentView.emptyText.text = "No Repeating Quests. Create one?"
                contentView.loadingView.visibility = View.GONE
                contentView.errorView.visibility = View.GONE
                contentView.questList.visibility = View.GONE
            }

            is RepeatingQuestListViewState.DataLoaded -> {
                contentView.questList.visibility = View.VISIBLE
                contentView.loadingView.visibility = View.GONE
                contentView.emptyView.visibility = View.GONE
                contentView.errorView.visibility = View.GONE
                adapter.items = state.repeatingQuests
                adapter.notifyDataSetChanged()
            }

            is RepeatingQuestListViewState.Error -> {
                Timber.e(state.error)
            }
        }
    }
}

class RepeatingQuestAdapter(manager: AdapterDelegatesManager<List<RepeatingQuestViewModel>>) :
    ListDelegationAdapter<List<RepeatingQuestViewModel>>(manager)

class RepeatingQuestViewModelAdapterDelegate(private val inflater: LayoutInflater) : AdapterDelegate<List<RepeatingQuestViewModel>>() {

    override fun onBindViewHolder(items: List<RepeatingQuestViewModel>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as ViewHolder
        val viewModel = items[position]
        vh.bind(viewModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder =
        ViewHolder(inflater.inflate(R.layout.item_repeating_quest, parent, false))

    override fun isForViewType(items: List<RepeatingQuestViewModel>, position: Int): Boolean = true

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(viewModel: RepeatingQuestViewModel) {
            val context = itemView.context
            itemView.name.text = viewModel.name
            itemView.categoryImage.setImageResource(viewModel.categoryImage)
            itemView.nextScheduledDate.text = NextScheduledDateFormatter.format(context,
                viewModel.nextScheduledDate,
                viewModel.duration,
                viewModel.startTime)

            itemView.progress.text = PeriodProgressFormatter.format(context, viewModel.remainingCount, viewModel.repeatType)

            val inflater = LayoutInflater.from(context)

            for (i in 1..viewModel.completedCount) {
                val progressView = inflater.inflate(R.layout.repeating_quest_progress_indicator,
                    itemView.progressContainer,
                    false)
                val progressViewBackground = progressView.background as GradientDrawable
                progressViewBackground.setColor(ContextCompat.getColor(context, viewModel.categoryColor))
                itemView.progressContainer.addView(progressView)
            }

            for (i in 1..viewModel.remainingCount) {
                val progressViewEmpty = inflater.inflate(R.layout.repeating_quest_empty_progress_indicator,
                    itemView.progressContainer, false)
                val progressViewEmptyBackground = progressViewEmpty.background as GradientDrawable

                val strokeWidth = ViewUtils.dpToPx(1f, context).toInt()
                val strokeColor = ContextCompat.getColor(context, viewModel.categoryColor)
                progressViewEmptyBackground.setStroke(strokeWidth, strokeColor)
                itemView.progressContainer.addView(progressViewEmpty)
            }

            itemView.moreMenu.setOnClickListener { v ->
                val popupMenu = PopupMenu(context, v)
                popupMenu.inflate(R.menu.repeating_quest_actions_menu)
                popupMenu.show()
            }
        }
    }
}