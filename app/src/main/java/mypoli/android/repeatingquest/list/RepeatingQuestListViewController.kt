package mypoli.android.repeatingquest.list

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.controller_repeating_quest_list.view.*
import kotlinx.android.synthetic.main.item_repeating_quest.view.*
import kotlinx.android.synthetic.main.view_inventory_toolbar.view.*
import mypoli.android.R
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.view.AndroidColor
import mypoli.android.common.view.AndroidIcon
import mypoli.android.common.view.setChildController
import mypoli.android.player.inventory.GemInventoryViewController

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/14/18.
 */
class RepeatingQuestListViewController(args: Bundle? = null) :
    ReduxViewController<RepeatingQuestListAction, RepeatingQuestListViewState, RepeatingQuestListPresenter>(
        args
    ) {
    override val presenter = RepeatingQuestListPresenter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.controller_repeating_quest_list, container, false
        )

        view.toolbarTitle.setText("Repeating quests")
        view.repeatingQuestList.layoutManager =
            LinearLayoutManager(container.context, LinearLayoutManager.VERTICAL, false)
        view.repeatingQuestList.adapter = RepeatingQuestAdapter(
            listOf(
                RepeatingQuestViewModel(
                    "Workout",
                    AndroidIcon.BUS,
                    AndroidColor.GREEN,
                    "Today",
                    2,
                    3
                ),
                RepeatingQuestViewModel(
                    "Run",
                    AndroidIcon.BIKE,
                    AndroidColor.BLUE,
                    "Tomorrow",
                    1,
                    5
                ),
                RepeatingQuestViewModel(
                    "Cook",
                    AndroidIcon.ACADEMIC,
                    AndroidColor.DEEP_ORANGE,
                    "Today",
                    4,
                    10
                )
            )
        )

        setChildController(view.playerGems, GemInventoryViewController())
        return view
    }

    override fun render(state: RepeatingQuestListViewState, view: View) {
    }

    data class RepeatingQuestViewModel(
        val name: String,
        val icon: AndroidIcon?,
        val color: AndroidColor,
        val next: String,
        val completedCount: Int,
        val allCount: Int
    )

    inner class RepeatingQuestAdapter(private var viewModels: List<RepeatingQuestViewModel> = listOf()) :
        RecyclerView.Adapter<RepeatingQuestAdapter.ViewHolder>() {

        override fun onBindViewHolder(
            holder: RepeatingQuestAdapter.ViewHolder,
            position: Int
        ) {
            val vm = viewModels[position]
            val view = holder.itemView
            view.rqName.text = vm.name
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RepeatingQuestAdapter.ViewHolder =
            ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_repeating_quest,
                    parent,
                    false
                )
            )

        override fun getItemCount() = viewModels.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    }

}