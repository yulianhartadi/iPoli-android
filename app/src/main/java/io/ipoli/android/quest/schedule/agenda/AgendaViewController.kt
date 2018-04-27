package io.ipoli.android.quest.schedule.agenda

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bluelinelabs.conductor.RouterTransaction
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.common.view.recyclerview.SwipeToCompleteCallback
import io.ipoli.android.quest.CompletedQuestViewController
import io.ipoli.android.quest.show.QuestViewController
import kotlinx.android.synthetic.main.controller_agenda.view.*
import kotlinx.android.synthetic.main.item_agenda_event.view.*
import kotlinx.android.synthetic.main.item_agenda_month_divider.view.*
import kotlinx.android.synthetic.main.item_agenda_quest.view.*
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 1/26/18.
 */
class AgendaViewController(args: Bundle? = null) :
    ReduxViewController<AgendaAction, AgendaViewState, AgendaReducer>(args) {

    override val reducer = AgendaReducer

    private lateinit var scrollToPositionListener: RecyclerView.OnScrollListener

    private lateinit var startDate: LocalDate

    constructor(startDate: LocalDate) : this() {
        this.startDate = startDate
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.controller_agenda, container, false)
        val layoutManager =
            LinearLayoutManager(container.context, LinearLayoutManager.VERTICAL, false)
        view.agendaList.layoutManager = layoutManager
        view.agendaList.adapter = AgendaAdapter()

        val swipeHandler = object : SwipeToCompleteCallback(
            view.context,
            R.drawable.ic_done_white_24dp,
            R.color.md_green_500,
            R.drawable.ic_close_white_24dp,
            R.color.md_amber_500
        ) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.END) {
                    dispatch(AgendaAction.CompleteQuest(viewHolder.adapterPosition))
                } else if (direction == ItemTouchHelper.START) {
                    dispatch(AgendaAction.UndoCompleteQuest(viewHolder.adapterPosition))
                }
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView?,
                viewHolder: RecyclerView.ViewHolder?
            ) = when (viewHolder) {
                is AgendaAdapter.QuestViewHolder -> ItemTouchHelper.END
                is AgendaAdapter.CompletedQuestViewHolder -> ItemTouchHelper.START
                else -> 0
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(view.agendaList)

        return view
    }

    override fun onCreateLoadAction() = AgendaAction.Load(startDate)

    override fun onDetach(view: View) {
        view.agendaList.clearOnScrollListeners()
        super.onDetach(view)
    }

    override fun render(state: AgendaViewState, view: View) {

        when (state.type) {
            AgendaViewState.StateType.DATA_CHANGED -> {
                ViewUtils.goneViews(view.topLoader, view.bottomLoader)
                val agendaList = view.agendaList
                agendaList.clearOnScrollListeners()
                (agendaList.adapter as AgendaAdapter).updateAll(state.toAgendaItemViewModels())
                addScrollListeners(agendaList, state)
            }

            AgendaViewState.StateType.SHOW_TOP_LOADER -> {
                ViewUtils.showViews(view.topLoader)
            }

            AgendaViewState.StateType.SHOW_BOTTOM_LOADER -> {
                ViewUtils.showViews(view.bottomLoader)
            }
        }
    }

    private fun addScrollListeners(
        agendaList: RecyclerView,
        state: AgendaViewState
    ) {

        val endlessRecyclerViewScrollListener =
            EndlessRecyclerViewScrollListener(
                agendaList.layoutManager as LinearLayoutManager,
                { side, position ->
                    agendaList.clearOnScrollListeners()
                    if (side == EndlessRecyclerViewScrollListener.Side.TOP) {
                        dispatch(AgendaAction.LoadBefore(position))
                    } else {
                        dispatch(AgendaAction.LoadAfter(position))
                    }
                },
                15
            )
        val changeItemScrollListener = ChangeItemScrollListener(
            agendaList.layoutManager as LinearLayoutManager,
            { pos ->
                dispatch(AgendaAction.FirstVisibleItemChanged(pos))
            }
        )


        scrollToPositionListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                agendaList.addOnScrollListener(endlessRecyclerViewScrollListener)
                agendaList.addOnScrollListener(changeItemScrollListener)
                agendaList.removeOnScrollListener(scrollToPositionListener)
            }
        }

        if (state.scrollToPosition != null) {
            agendaList.addOnScrollListener(scrollToPositionListener)
            (agendaList.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                state.scrollToPosition,
                0
            )
        } else {
            agendaList.addOnScrollListener(endlessRecyclerViewScrollListener)
            agendaList.addOnScrollListener(changeItemScrollListener)
        }
    }

    private fun showCompletedQuest(questId: String) {
        pushWithRootRouter(RouterTransaction.with(CompletedQuestViewController(questId)))
    }

    private fun showQuest(questId: String) {
        pushWithRootRouter(
            QuestViewController.routerTransaction(questId)
        )
    }

    data class TagViewModel(val name: String, @ColorRes val color: Int)

    interface AgendaViewModel

    data class QuestViewModel(
        val id: String,
        val name: String,
        val tags: List<TagViewModel>,
        val startTime: String,
        @ColorRes val color: Int,
        val icon: IIcon,
        val isCompleted: Boolean,
        val showDivider: Boolean,
        val isRepeating: Boolean,
        val isFromChallenge: Boolean,
        val isPlaceholder: Boolean
    ) : AgendaViewModel

    data class EventViewModel(
        val name: String,
        val startTime: String, @ColorInt val color: Int,
        val icon: IIcon,
        val showDivider: Boolean
    ) :
        AgendaViewModel

    data class DateHeaderViewModel(val text: String) : AgendaViewModel
    data class MonthDividerViewModel(
        @DrawableRes val image: Int, val text: String
    ) : AgendaViewModel

    data class WeekHeaderViewModel(val text: String) : AgendaViewModel

    enum class ItemType {
        QUEST_PLACEHOLDER, QUEST, COMPLETED_QUEST, EVENT, DATE_HEADER, MONTH_DIVIDER, WEEK_HEADER
    }

    inner class AgendaAdapter(private var viewModels: MutableList<AgendaViewModel> = mutableListOf()) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(), AutoUpdatableAdapter {

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val vm = viewModels[holder.adapterPosition]
            val itemView = holder.itemView

            val type = ItemType.values()[getItemViewType(position)]
            when (type) {
                ItemType.QUEST_PLACEHOLDER -> bindPlaceholderViewModel(
                    itemView,
                    vm as QuestViewModel
                )
                ItemType.QUEST -> bindQuestViewModel(itemView, vm as QuestViewModel)
                ItemType.COMPLETED_QUEST -> bindCompleteQuestViewModel(
                    itemView,
                    vm as QuestViewModel
                )
                ItemType.EVENT ->
                    bindEventViewModel(itemView, vm as EventViewModel)
                ItemType.DATE_HEADER -> bindDateHeaderViewModel(itemView, vm as DateHeaderViewModel)
                ItemType.MONTH_DIVIDER -> bindMonthDividerViewModel(
                    itemView,
                    vm as MonthDividerViewModel
                )
                ItemType.WEEK_HEADER -> bindWeekHeaderViewModel(
                    itemView,
                    vm as WeekHeaderViewModel
                )
            }
        }

        private fun bindPlaceholderViewModel(
            view: View,
            vm: QuestViewModel
        ) {
            view.setOnClickListener(null)
            view.questName.text = vm.name
            bindQuest(view, vm)
        }

        private fun bindEventViewModel(view: View, viewModel: EventViewModel) {

            view.eventDivider.visible = viewModel.showDivider

            view.eventName.text = viewModel.name
            view.eventStartTime.text = viewModel.startTime

            view.eventIcon.backgroundTintList =
                ColorStateList.valueOf(viewModel.color)
            view.eventIcon.setImageDrawable(
                IconicsDrawable(view.context)
                    .icon(viewModel.icon)
                    .colorRes(R.color.md_white)
                    .paddingDp(3)
                    .sizeDp(24)
            )
        }

        private fun bindWeekHeaderViewModel(
            view: View,
            viewModel: WeekHeaderViewModel
        ) {
            view.setOnClickListener(null)
            (view as TextView).text = viewModel.text
        }

        private fun bindMonthDividerViewModel(
            view: View,
            viewModel: MonthDividerViewModel
        ) {
            view.setOnClickListener(null)
            view.dateLabel.text = viewModel.text
            view.monthImage.setImageResource(viewModel.image)
        }

        private fun bindDateHeaderViewModel(
            view: View,
            viewModel: DateHeaderViewModel
        ) {
            view.setOnClickListener(null)
            (view as TextView).text = viewModel.text
        }

        private fun bindCompleteQuestViewModel(
            view: View,
            vm: QuestViewModel
        ) {

            view.setOnClickListener {
                showCompletedQuest(vm.id)
            }

            val span = SpannableString(vm.name)
            span.setSpan(StrikethroughSpan(), 0, vm.name.length, 0)

            view.questName.text = span
            bindQuest(view, vm)
        }

        private fun bindQuestViewModel(
            view: View,
            vm: QuestViewModel
        ) {
            view.setOnClickListener {
                showQuest(vm.id)
            }
            view.questName.text = vm.name
            bindQuest(view, vm)
        }

        private fun bindQuest(
            view: View,
            vm: QuestViewModel
        ) {
            view.questIcon.backgroundTintList =
                ColorStateList.valueOf(colorRes(vm.color))
            view.questIcon.setImageDrawable(
                IconicsDrawable(view.context)
                    .icon(vm.icon)
                    .colorRes(R.color.md_white)
                    .paddingDp(3)
                    .sizeDp(24)
            )

            if (vm.tags.isNotEmpty()) {
                view.questTagName.visible()
                renderTag(view, vm.tags.first())
            } else {
                view.questTagName.gone()
            }

            view.questStartTime.text = vm.startTime
            view.divider.visible = vm.showDivider

            view.questRepeatIndicator.visibility = if (vm.isRepeating) View.VISIBLE else View.GONE
            view.questChallengeIndicator.visibility =
                if (vm.isFromChallenge) View.VISIBLE else View.GONE
        }

        private fun renderTag(view: View, tag: TagViewModel) {
            view.questTagName.text = tag.name
            TextViewCompat.setTextAppearance(
                view.questTagName,
                R.style.TextAppearance_AppCompat_Caption
            )

            val indicator = view.questTagName.compoundDrawablesRelative[0] as GradientDrawable
            indicator.mutate()
            val size = ViewUtils.dpToPx(8f, view.context).toInt()
            indicator.setSize(size, size)
            indicator.setColor(colorRes(tag.color))
            view.questTagName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                indicator,
                null,
                null,
                null
            )
        }

        override fun getItemCount() = viewModels.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {

                ItemType.QUEST_PLACEHOLDER.ordinal -> {
                    val view = LayoutInflater.from(parent.context).inflate(
                        R.layout.item_agenda_quest,
                        parent,
                        false
                    )
                    view.layoutParams.width = parent.width
                    QuestPlaceholderViewHolder(
                        view
                    )
                }

                ItemType.QUEST.ordinal -> {
                    val view = LayoutInflater.from(parent.context).inflate(
                        R.layout.item_agenda_quest,
                        parent,
                        false
                    )
                    view.layoutParams.width = parent.width
                    QuestViewHolder(
                        view
                    )
                }
                ItemType.COMPLETED_QUEST.ordinal -> {
                    val view = LayoutInflater.from(parent.context).inflate(
                        R.layout.item_agenda_quest,
                        parent,
                        false
                    )
                    view.layoutParams.width = parent.width
                    CompletedQuestViewHolder(
                        view
                    )
                }

                ItemType.EVENT.ordinal -> {
                    val view = LayoutInflater.from(parent.context).inflate(
                        R.layout.item_agenda_event,
                        parent,
                        false
                    )
                    view.layoutParams.width = parent.width
                    SimpleViewHolder(
                        view
                    )
                }

                ItemType.DATE_HEADER.ordinal -> DateHeaderViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_agenda_date_header,
                        parent,
                        false
                    )
                )
                ItemType.MONTH_DIVIDER.ordinal -> MonthDividerViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_agenda_month_divider,
                        parent,
                        false
                    )
                )
                ItemType.WEEK_HEADER.ordinal -> WeekHeaderViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_agenda_week_header,
                        parent,
                        false
                    )
                )
                else -> {
                    throw IllegalArgumentException("Unknown viewType $viewType")
                }
            }

        inner class QuestPlaceholderViewHolder(view: View) : RecyclerView.ViewHolder(view)
        inner class QuestViewHolder(view: View) : RecyclerView.ViewHolder(view)
        inner class CompletedQuestViewHolder(view: View) : RecyclerView.ViewHolder(view)
        inner class DateHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)
        inner class MonthDividerViewHolder(view: View) : RecyclerView.ViewHolder(view)
        inner class WeekHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemViewType(position: Int) =
            when (viewModels[position]) {

                is QuestViewModel -> if ((viewModels[position] as QuestViewModel).isCompleted)
                    ItemType.COMPLETED_QUEST.ordinal
                else if ((viewModels[position] as QuestViewModel).isPlaceholder)
                    ItemType.QUEST_PLACEHOLDER.ordinal
                else
                    ItemType.QUEST.ordinal
                is EventViewModel -> ItemType.EVENT.ordinal
                is DateHeaderViewModel -> ItemType.DATE_HEADER.ordinal
                is MonthDividerViewModel -> ItemType.MONTH_DIVIDER.ordinal
                is WeekHeaderViewModel -> ItemType.WEEK_HEADER.ordinal
                else -> super.getItemViewType(position)

            }

        fun updateAll(viewModels: List<AgendaViewModel>) {
            val oldViewModels = this.viewModels
            val newViewModels = viewModels.toMutableList()
            this.viewModels = newViewModels
            autoNotify(oldViewModels, newViewModels, { vm1, vm2 ->
                vm1 == vm2
            })
        }
    }
}