package mypoli.android.challenge.list

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.controller_challenge_list.view.*
import kotlinx.android.synthetic.main.item_challenge.view.*
import kotlinx.android.synthetic.main.item_complete_challenge.view.*
import kotlinx.android.synthetic.main.view_empty_list.view.*
import kotlinx.android.synthetic.main.view_loader.view.*
import mypoli.android.R
import mypoli.android.challenge.add.AddChallengeViewController
import mypoli.android.challenge.list.ChallengeListViewController.ChallengeItemViewModel.*
import mypoli.android.challenge.predefined.category.ChallengeCategoryListViewController
import mypoli.android.challenge.show.ChallengeViewController
import mypoli.android.common.datetime.daysUntil
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.text.DateFormatter
import mypoli.android.common.text.DurationFormatter
import mypoli.android.common.view.*
import mypoli.android.common.view.recyclerview.SimpleViewHolder
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/05/2018.
 */
class ChallengeListViewController(args: Bundle? = null) :
    ReduxViewController<ChallengeListAction, ChallengeListViewState, ChallengeListReducer>(
        args
    ) {

    override val reducer = ChallengeListReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(
            R.layout.controller_challenge_list, container, false
        )
        view.challengeList.layoutManager =
            LinearLayoutManager(container.context, LinearLayoutManager.VERTICAL, false)
        view.challengeList.adapter = ChallengeAdapter()

        view.addChallenge.dispatchOnClick(ChallengeListAction.AddChallenge)
        return view
    }

    override fun onCreateLoadAction() = ChallengeListAction.Load

    override fun onAttach(view: View) {
        super.onAttach(view)
        toolbarTitle = stringRes(R.string.drawer_challenges)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.challenge_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.actionPredefinedChallenges) {
            val handler = FadeChangeHandler()
            rootRouter.pushController(
                RouterTransaction.with(ChallengeCategoryListViewController())
                    .pushChangeHandler(handler)
                    .popChangeHandler(handler)
            )
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun render(state: ChallengeListViewState, view: View) {
        when (state) {

            is ChallengeListViewState.Loading -> {
                view.loader.invisible()
                view.emptyContainer.invisible()
                view.challengeList.invisible()
            }

            is ChallengeListViewState.Changed -> {
                view.challengeList.visible()
                view.loader.invisible()
                view.emptyContainer.invisible()
                (view.challengeList.adapter as ChallengeAdapter).updateAll(
                    state.toViewModels(
                        view.context
                    )
                )
            }

            ChallengeListViewState.Empty -> {
                view.emptyContainer.visible()
                view.loader.invisible()
                view.challengeList.invisible()

                view.emptyImage.setImageResource(R.drawable.challenge_list_empty_state)
                view.emptyTitle.setText(R.string.empty_challenges_title)
                view.emptyText.setText(R.string.empty_challenges_text)
            }

            ChallengeListViewState.ShowAdd -> {
                val handler = FadeChangeHandler()
                rootRouter.pushController(
                    RouterTransaction.with(AddChallengeViewController())
                        .pushChangeHandler(handler)
                        .popChangeHandler(handler)
                )
            }
        }
    }

    companion object {
        const val INCOMPLETE_ITEM_VIEW_TYPE = 0
        const val COMPLETE_LABEL_ITEM_VIEW_TYPE = 1
        const val COMPLETE_ITEM_VIEW_TYPE = 2
    }

    inner class ChallengeAdapter(private var viewModels: List<ChallengeItemViewModel> = listOf()) :
        RecyclerView.Adapter<SimpleViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                INCOMPLETE_ITEM_VIEW_TYPE ->
                    SimpleViewHolder(
                        inflater.inflate(R.layout.item_challenge, parent, false)
                    )

                COMPLETE_LABEL_ITEM_VIEW_TYPE ->
                    SimpleViewHolder(
                        inflater.inflate(R.layout.item_list_section, parent, false)
                    )

                COMPLETE_ITEM_VIEW_TYPE ->
                    SimpleViewHolder(
                        inflater.inflate(R.layout.item_complete_challenge, parent, false)
                    )

                else -> throw IllegalArgumentException("Unknown viewType $viewType")
            }
        }


        override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {

            val vm = viewModels[position]
            if (holder.itemViewType == INCOMPLETE_ITEM_VIEW_TYPE) {
                bindIncompleteChallenge(holder.itemView, vm as Incomplete)
            } else if (holder.itemViewType == COMPLETE_LABEL_ITEM_VIEW_TYPE) {
                bindCompleteLabel(holder.itemView, vm as CompleteLabel)
            } else if (holder.itemViewType == COMPLETE_ITEM_VIEW_TYPE) {
                bindCompleteChallenge(holder.itemView, vm as Complete)
            }

        }

        private fun bindCompleteChallenge(
            view: View,
            vm: Complete
        ) {
            view.ccName.text = vm.name

            view.ccIcon.backgroundTintList =
                ColorStateList.valueOf(colorRes(vm.color))
            view.ccIcon.setImageDrawable(
                IconicsDrawable(view.context)
                    .icon(vm.icon)
                    .colorRes(R.color.md_white)
                    .sizeDp(24)
            )

            view.ccStart.text = vm.start
            view.ccComplete.text = vm.complete

            view.setOnClickListener {
                val changeHandler = FadeChangeHandler()
                rootRouter.pushController(
                    RouterTransaction.with(ChallengeViewController(vm.id))
                        .pushChangeHandler(changeHandler)
                        .popChangeHandler(changeHandler)
                )
            }
        }

        private fun bindCompleteLabel(
            view: View,
            vm: CompleteLabel
        ) {
            (view as TextView).text = vm.label
        }

        private fun bindIncompleteChallenge(
            view: View,
            vm: Incomplete
        ) {
            view.cName.text = vm.name

            view.cIcon.backgroundTintList =
                ColorStateList.valueOf(colorRes(vm.color))
            view.cIcon.setImageDrawable(
                IconicsDrawable(view.context)
                    .icon(vm.icon)
                    .colorRes(R.color.md_white)
                    .sizeDp(24)
            )

            view.cNext.text = vm.next
            view.cEnd.text = vm.end

            view.cProgress.max = vm.progressMax
            view.cProgress.progress = vm.progress

            view.setOnClickListener {
                val changeHandler = FadeChangeHandler()
                rootRouter.pushController(
                    RouterTransaction.with(ChallengeViewController(vm.id))
                        .pushChangeHandler(changeHandler)
                        .popChangeHandler(changeHandler)
                )
            }
        }

        override fun getItemCount() = viewModels.size

        fun updateAll(viewModels: List<ChallengeItemViewModel>) {
            this.viewModels = viewModels
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int) =
            when (viewModels[position]) {
                is Incomplete -> INCOMPLETE_ITEM_VIEW_TYPE
                is CompleteLabel -> COMPLETE_LABEL_ITEM_VIEW_TYPE
                is Complete -> COMPLETE_ITEM_VIEW_TYPE
            }
    }

    sealed class ChallengeItemViewModel {

        data class Incomplete(
            val id: String,
            val name: String,
            val icon: IIcon,
            @ColorRes val color: Int,
            val next: String,
            val end: String,
            val progress: Int,
            val progressMax: Int
        ) : ChallengeItemViewModel()

        data class CompleteLabel(val label: String) : ChallengeItemViewModel()
        data class Complete(
            val id: String,
            val name: String,
            val icon: IIcon,
            @ColorRes val color: Int,
            val start: String,
            val complete: String
        ) : ChallengeItemViewModel()
    }

    private fun ChallengeListViewState.Changed.toViewModels(context: Context): List<ChallengeListViewController.ChallengeItemViewModel> {
        return challenges.map {
            when (it) {
                is ChallengeListViewState.ChallengeItem.Incomplete -> {
                    val c = it.challenge
                    val next = when {
                        c.nextDate != null -> {
                            var res = stringRes(
                                R.string.repeating_quest_next,
                                DateFormatter.format(context, c.nextDate)
                            )
                            res += if (c.nextStartTime != null) {
                                " ${c.nextStartTime} - ${c.nextEndTime}"
                            } else {
                                " " + stringRes(
                                    R.string.quest_for_time,
                                    DurationFormatter.formatShort(view!!.context, c.nextDuration!!)
                                )
                            }
                            res
                        }
                        else -> stringRes(
                            R.string.repeating_quest_next,
                            stringRes(R.string.unscheduled)
                        )
                    }

                    val daysUntilComplete = LocalDate.now().daysUntil(c.end)

                    val end = when {
                        daysUntilComplete == 0L -> stringRes(R.string.ends_today)
                        daysUntilComplete <= 7 -> stringRes(
                            R.string.ends_in_days,
                            daysUntilComplete
                        )
                        else -> stringRes(
                            R.string.ends_at_date,
                            DateFormatter.formatWithoutYear(view!!.context, c.end)
                        )
                    }

                    Incomplete(
                        id = c.id,
                        name = c.name,
                        color = AndroidColor.valueOf(c.color.name).color500,
                        icon = c.icon?.let { AndroidIcon.valueOf(it.name).icon }
                            ?: Ionicons.Icon.ion_android_clipboard,
                        next = next,
                        end = end,
                        progress = c.progress.completedCount,
                        progressMax = c.progress.allCount
                    )
                }

                is ChallengeListViewState.ChallengeItem.CompleteLabel ->
                    CompleteLabel(stringRes(R.string.completed))

                is ChallengeListViewState.ChallengeItem.Complete ->
                    with(it.challenge) {
                        Complete(
                            id = id,
                            name = name,
                            color = AndroidColor.valueOf(color.name).color500,
                            icon = icon?.let { AndroidIcon.valueOf(it.name).icon }
                                ?: Ionicons.Icon.ion_android_clipboard,
                            start = stringRes(
                                R.string.started_at_date,
                                DateFormatter.format(activity!!, start)
                            ),
                            complete = stringRes(
                                R.string.completed_at_date,
                                DateFormatter.format(activity!!, completedAtDate)
                            )
                        )
                    }
            }


        }
    }

}