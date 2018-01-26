package mypoli.android.quest.agenda

import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.RestoreViewOnCreateController
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.controller_agenda.view.*
import kotlinx.android.synthetic.main.item_agenda_quest.view.*
import mypoli.android.R
import mypoli.android.common.datetime.Time
import mypoli.android.common.view.AndroidColor
import mypoli.android.common.view.AndroidIcon
import mypoli.android.common.view.colorRes
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/26/18.
 */
class AgendaViewController : RestoreViewOnCreateController() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.controller_agenda, container, false)
        view.agendaList.layoutManager =
            LinearLayoutManager(container.context, LinearLayoutManager.VERTICAL, false)
        view.agendaList.adapter = AgendaAdapter(
            listOf(
                QuestViewModel(
                    "Run",
                    Time.Companion.at(10, 30),
                    AndroidColor.BLUE,
                    AndroidIcon.ACADEMIC
                ),
                QuestViewModel(
                    "Walk",
                    Time.Companion.at(20, 30),
                    AndroidColor.DEEP_ORANGE,
                    AndroidIcon.BIKE
                ),
                QuestViewModel(
                    "Dance",
                    Time.Companion.at(8, 30),
                    AndroidColor.GREEN,
                    AndroidIcon.BUS
                )
            )
        )
        return view
    }

    interface AgendaViewModel

    data class QuestViewModel(
        val name: String,
        val startTime: Time,
        val color: AndroidColor,
        val icon: AndroidIcon
    ) : AgendaViewModel

    data class DateViewModel(val date: LocalDate) : AgendaViewModel
    data class MonthDividerViewModel(val image: Int) : AgendaViewModel
    data class EmptyDaysViewModel(val label: String) : AgendaViewModel

    enum class ItemType {
        QUEST, DATE, MONTH_DIVIDER, EMPTY_DAYS
    }

    inner class AgendaAdapter(private var viewModels: List<AgendaViewModel> = listOf()) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val vm = viewModels[position]
            val itemView = holder.itemView

            val type = getItemViewType(position)
            when(type) {
                ItemType.QUEST.ordinal -> bindQuestViewModel(itemView, vm as QuestViewModel)
                ItemType.DATE.ordinal -> bindDateViewModel(itemView, vm as DateViewModel)
                ItemType.MONTH_DIVIDER.ordinal -> bindMonthDividerViewModel(itemView, vm as MonthDividerViewModel)
                ItemType.EMPTY_DAYS.ordinal -> bindEmptyDaysViewModel(itemView, vm as EmptyDaysViewModel)
            }
        }

        private fun bindEmptyDaysViewModel(
            view: View,
            viewModel: EmptyDaysViewModel
        ) {

        }

        private fun bindMonthDividerViewModel(
            view: View,
            viewModel: MonthDividerViewModel
        ) {

        }

        private fun bindDateViewModel(
            view: View,
            viewModel: DateViewModel
        ) {

        }

        private fun bindQuestViewModel(
            view: View,
            vm: QuestViewModel
        ) {
            view.questName.text = vm.name
            view.questIcon.backgroundTintList =
                ColorStateList.valueOf(colorRes(vm.color.color500))
            view.questIcon.setImageDrawable(
                IconicsDrawable(view.context)
                    .icon(vm.icon.icon)
                    .colorRes(R.color.md_white)
                    .sizeDp(24)
            )
            view.questStartTime.text = vm.startTime.toString()
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
                ItemType.DATE.ordinal -> DateViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_agenda_quest,
                        parent,
                        false
                    )
                )
                ItemType.MONTH_DIVIDER.ordinal -> MonthDividerViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_agenda_quest,
                        parent,
                        false
                    )
                )
                ItemType.EMPTY_DAYS.ordinal -> EmptyDaysViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_agenda_quest,
                        parent,
                        false
                    )
                )
                else -> null
            }

        inner class QuestViewHolder(view: View) : RecyclerView.ViewHolder(view)
        inner class DateViewHolder(view: View) : RecyclerView.ViewHolder(view)
        inner class MonthDividerViewHolder(view: View) : RecyclerView.ViewHolder(view)
        inner class EmptyDaysViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemViewType(position: Int) =
            when (viewModels[position]) {
                is QuestViewModel -> ItemType.QUEST.ordinal
                is DateViewModel -> ItemType.DATE.ordinal
                is MonthDividerViewModel -> ItemType.MONTH_DIVIDER.ordinal
                is EmptyDaysViewModel -> ItemType.EMPTY_DAYS.ordinal
                else -> super.getItemViewType(position)

            }

        fun updateAll(viewModels: List<QuestViewModel>) {
            this.viewModels = viewModels
            notifyDataSetChanged()
        }

    }


}