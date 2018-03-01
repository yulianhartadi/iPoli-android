package mypoli.android.repeatingquest.list

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.controller_repeating_quest_list.view.*
import kotlinx.android.synthetic.main.item_repeating_quest.view.*
import mypoli.android.R
import mypoli.android.common.ViewUtils
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.text.DateFormatter
import mypoli.android.common.text.DurationFormatter
import mypoli.android.common.view.*
import mypoli.android.repeatingquest.entity.frequencyType
import mypoli.android.repeatingquest.list.RepeatingQuestListViewState.StateType.CHANGED
import mypoli.android.repeatingquest.show.RepeatingQuestViewController

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
        view.repeatingQuestList.adapter = RepeatingQuestAdapter()
        return view
    }

    override fun onCreateLoadAction() =
        RepeatingQuestListAction.LoadData

    override fun onAttach(view: View) {
        super.onAttach(view)
        activity?.window?.navigationBarColor = attrData(R.attr.colorPrimary)
        activity?.window?.statusBarColor = attrData(R.attr.colorPrimaryDark)
    }

    override fun render(state: RepeatingQuestListViewState, view: View) {
        when (state.type) {
            CHANGED -> {
                view.loader.visible = false
                view.emptyGroup.visible = state.showEmptyView

                (view.repeatingQuestList.adapter as RepeatingQuestAdapter).updateAll(
                    state.toViewModels(
                        view.context
                    )
                )
            }
        }
    }

    data class RepeatingQuestViewModel(
        val id: String,
        val name: String,
        val icon: IIcon,
        @ColorRes val color: Int,
        val next: String,
        val completedCount: Int,
        val allCount: Int,
        val isCompleted: Boolean,
        val frequency: String
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
            view.rqFrequency.text = vm.frequency

            val progressBar = view.rqProgressBar
            val progress = view.rqProgress
            if (vm.isCompleted) {
                ViewUtils.hideViews(progressBar, progress)
            } else {
                ViewUtils.showViews(progressBar, progress)
                progressBar.max = vm.allCount
                progressBar.progress = vm.completedCount
                progressBar.progressTintList = ColorStateList.valueOf(colorRes(vm.color))
                progress.text = "${vm.completedCount}/${vm.allCount}"
            }

            view.setOnClickListener {
                val changeHandler = FadeChangeHandler()
                rootRouter.pushController(
                    RouterTransaction.with(RepeatingQuestViewController(vm.id))
                        .pushChangeHandler(changeHandler)
                        .popChangeHandler(changeHandler)
                )
            }

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

        fun updateAll(viewModels: List<RepeatingQuestViewModel>) {
            this.viewModels = viewModels
            notifyDataSetChanged()
        }

    }

    private fun RepeatingQuestListViewState.toViewModels(context: Context): List<RepeatingQuestViewModel> {
        val frequencies = stringsRes(R.array.repeating_quest_frequencies)
        return repeatingQuests.map {
            val next = when {
                it.isCompleted -> stringRes(R.string.completed)
                it.nextDate != null -> {
                    var res = stringRes(
                        R.string.repeating_quest_next,
                        DateFormatter.format(context, it.nextDate)
                    )
                    res += if (it.startTime != null) {
                        " ${it.startTime} - ${it.endTime}"
                    } else {
                        " " + stringRes(
                            R.string.quest_for_time,
                            DurationFormatter.formatShort(view!!.context, it.duration)
                        )
                    }
                    res
                }
                else -> stringRes(
                    R.string.repeating_quest_next,
                    stringRes(R.string.unscheduled)
                )
            }
            RepeatingQuestListViewController.RepeatingQuestViewModel(
                id = it.id,
                name = it.name,
                icon = it.icon?.let { AndroidIcon.valueOf(it.name).icon }
                    ?: Ionicons.Icon.ion_android_clipboard,
                color = AndroidColor.valueOf(it.color.name).color500,
                next = next,
                completedCount = it.periodProgress!!.completedCount,
                allCount = it.periodProgress.allCount,
                isCompleted = it.isCompleted,
                frequency = frequencies[it.repeatingPattern.frequencyType.ordinal]
            )
        }
    }
}