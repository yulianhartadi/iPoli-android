package mypoli.android.quest.schedule.agenda

import android.content.res.ColorStateList
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import kotlinx.android.synthetic.main.controller_agenda.view.*
import kotlinx.android.synthetic.main.item_agenda_month_divider.view.*
import kotlinx.android.synthetic.main.item_agenda_quest.view.*
import mypoli.android.R
import mypoli.android.common.ViewUtils
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.view.AutoUpdatableAdapter
import mypoli.android.common.view.EndlessRecyclerViewScrollListener
import mypoli.android.common.view.colorRes
import mypoli.android.common.view.visible
import mypoli.android.quest.schedule.agenda.widget.SwipeToCompleteCallback

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/26/18.
 */
class AgendaViewController(args: Bundle? = null) :
    ReduxViewController<AgendaAction, AgendaViewState, AgendaPresenter>(args) {


    override val presenter get() = AgendaPresenter()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.controller_agenda, container, false)
        val layoutManager =
            LinearLayoutManager(container.context, LinearLayoutManager.VERTICAL, false)
        view.agendaList.layoutManager = layoutManager
        val adapter = AgendaAdapter()
        view.agendaList.adapter = adapter

        val swipeHandler = object : SwipeToCompleteCallback(
            view.context,
            R.drawable.ic_done_white_24dp,
            R.color.md_green_500
        ) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                dispatch(AgendaAction.CompleteQuest(viewHolder.adapterPosition))
            }

            override fun canSwipe(
                recyclerView: RecyclerView?,
                viewHolder: RecyclerView.ViewHolder?
            ) = viewHolder is AgendaAdapter.QuestViewHolder
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(view.agendaList)

        return view
    }

    override fun render(state: AgendaViewState, view: View) {
        when (state.type) {
            AgendaState.StateType.DATA_CHANGED -> {
                ViewUtils.goneViews(view.topLoader, view.bottomLoader)
                val agendaList = view.agendaList
                (agendaList.adapter as AgendaAdapter).updateAll(state.agendaItems)
                if (state.scrollToPosition >= 0) {
                    agendaList.scrollToPosition(state.scrollToPosition)
                }
                agendaList.post {

                    agendaList.addOnScrollListener(
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
                            20
                        )
                    )
                }
            }

            AgendaState.StateType.SHOW_TOP_LOADER -> {
                ViewUtils.showViews(view.topLoader)
            }

            AgendaState.StateType.SHOW_BOTTOM_LOADER -> {
                ViewUtils.showViews(view.bottomLoader)
            }
        }
    }

    interface AgendaViewModel

    data class QuestViewModel(
        val name: String,
        val startTime: String,
        @ColorRes val color: Int,
        val icon: IIcon,
        val showDivider: Boolean = true
    ) : AgendaViewModel

    data class DateHeaderViewModel(val text: String) : AgendaViewModel
    data class MonthDividerViewModel(
        @DrawableRes val image: Int, val text: String
    ) : AgendaViewModel

    data class WeekHeaderViewModel(val text: String) : AgendaViewModel

    enum class ItemType {
        QUEST, DATE_HEADER, MONTH_DIVIDER, WEEK_HEADER
    }

    inner class AgendaAdapter(private var viewModels: MutableList<AgendaViewModel> = mutableListOf()) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(), AutoUpdatableAdapter {

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val vm = viewModels[holder.adapterPosition]
            val itemView = holder.itemView

            val type = ItemType.values()[getItemViewType(position)]
            when (type) {
                ItemType.QUEST -> bindQuestViewModel(itemView, vm as QuestViewModel)
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

        private fun bindWeekHeaderViewModel(
            view: View,
            viewModel: WeekHeaderViewModel
        ) {
            (view as TextView).text = viewModel.text
        }

        private fun bindMonthDividerViewModel(
            view: View,
            viewModel: MonthDividerViewModel
        ) {
            view.dateLabel.text = viewModel.text
            view.monthImage.setImageResource(viewModel.image)
        }

        private fun bindDateHeaderViewModel(
            view: View,
            viewModel: DateHeaderViewModel
        ) {
            (view as TextView).text = viewModel.text
        }

        private fun bindQuestViewModel(
            view: View,
            vm: QuestViewModel
        ) {
            view.questName.text = vm.name
            view.questIcon.backgroundTintList =
                ColorStateList.valueOf(colorRes(vm.color))
            view.questIcon.setImageDrawable(
                IconicsDrawable(view.context)
                    .icon(vm.icon)
                    .colorRes(R.color.md_white)
                    .sizeDp(24)
            )
            view.questStartTime.text = vm.startTime
            view.divider.visible = vm.showDivider
        }

        override fun getItemCount() = viewModels.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                ItemType.QUEST.ordinal -> QuestViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_agenda_quest,
                        parent,
                        false
                    )
                )
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
                else -> null
            }

        inner class QuestViewHolder(view: View) : RecyclerView.ViewHolder(view)
        inner class DateHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)
        inner class MonthDividerViewHolder(view: View) : RecyclerView.ViewHolder(view)
        inner class WeekHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemViewType(position: Int) =
            when (viewModels[position]) {
                is QuestViewModel -> ItemType.QUEST.ordinal
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