package mypoli.android.repeatingquest.list

import android.content.res.ColorStateList
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import kotlinx.android.synthetic.main.controller_repeating_quest_list.view.*
import kotlinx.android.synthetic.main.item_repeating_quest.view.*
import mypoli.android.R
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.view.*

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/14/18.
 */
class RepeatingQuestListViewController(args: Bundle? = null) :
    ReduxViewController<RepeatingQuestListAction, RepeatingQuestListViewState, RepeatingQuestListReducer>(
        args
    ) {

    override val reducer = RepeatingQuestListReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        toolbarTitle = stringRes(R.string.drawer_repeating_quests)
        val view = inflater.inflate(
            R.layout.controller_repeating_quest_list, container, false
        )
        view.repeatingQuestList.layoutManager =
            LinearLayoutManager(container.context, LinearLayoutManager.VERTICAL, false)
        view.repeatingQuestList.adapter = RepeatingQuestAdapter(
            listOf(
                RepeatingQuestViewModel(
                    "Workout",
                    AndroidIcon.BUS.icon,
                    AndroidColor.GREEN.color500,
                    "Next: Today",
                    2,
                    3
                ),
                RepeatingQuestViewModel(
                    "Run",
                    AndroidIcon.BIKE.icon,
                    AndroidColor.BLUE.color500,
                    "Next: Tomorrow",
                    1,
                    5
                ),
                RepeatingQuestViewModel(
                    "Cook",
                    AndroidIcon.ACADEMIC.icon,
                    AndroidColor.DEEP_ORANGE.color500,
                    "Next: Today",
                    4,
                    10
                )
            )
        )
        return view
    }

    override fun render(state: RepeatingQuestListViewState, view: View) {
    }

    data class RepeatingQuestViewModel(
        val name: String,
        val icon: IIcon,
        @ColorRes val color: Int,
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

            view.rqIcon.backgroundTintList =
                ColorStateList.valueOf(colorRes(vm.color))
            view.rqIcon.setImageDrawable(
                IconicsDrawable(view.context)
                    .icon(vm.icon)
                    .colorRes(R.color.md_white)
                    .sizeDp(24)
            )
            view.rqNext.text = vm.next
            val progressBar = view.rqProgressBar
//            progressBar.setOnTouchListener { _, _ ->
//                true
//            }
            progressBar.max = vm.allCount
            progressBar.progress = vm.completedCount
            view.rqProgress.text = "${vm.completedCount}/${vm.allCount}"
            progressBar.progressTintList = ColorStateList.valueOf(colorRes(vm.color))

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                val dr = view.rqProgressBar.tickMark
//                dr.setTintList(ColorStateList.valueOf(colorRes(vm.color)))
//
////                view.rqProgressBar.tickMarkTintList = ColorStateList.valueOf(colorRes(vm.color))
//            }

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