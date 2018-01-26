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
        view.agendaList.adapter = QuestAdapter(
            listOf(
                QuestViewModel("Run", Time.Companion.at(10, 30), AndroidColor.BLUE, AndroidIcon.ACADEMIC),
                QuestViewModel("Walk", Time.Companion.at(20, 30), AndroidColor.DEEP_ORANGE, AndroidIcon.BIKE),
                QuestViewModel("Dance", Time.Companion.at(8, 30), AndroidColor.GREEN, AndroidIcon.BUS)
            )
        )
        return view
    }

    data class QuestViewModel(
        val name: String,
        val startTime: Time,
        val color: AndroidColor,
        val icon: AndroidIcon
    )

    inner class QuestAdapter(private var viewModels: List<QuestViewModel> = listOf()) :
        RecyclerView.Adapter<QuestAdapter.ViewHolder>() {
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = viewModels[position]
            val itemView = holder.itemView

            itemView.questName.text = vm.name
            itemView.questIcon.backgroundTintList =
                ColorStateList.valueOf(colorRes(vm.color.color500))
            itemView.questIcon.setImageDrawable(
                IconicsDrawable(itemView.context)
                    .icon(vm.icon.icon)
                    .colorRes(R.color.md_white)
                    .sizeDp(24)
            )
            itemView.questStartTime.text = vm.startTime.toString()
        }

        override fun getItemCount() = viewModels.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_agenda_quest,
                    parent,
                    false
                )
            )

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        fun updateAll(viewModels: List<QuestViewModel>) {
            this.viewModels = viewModels
            notifyDataSetChanged()
        }

    }


}