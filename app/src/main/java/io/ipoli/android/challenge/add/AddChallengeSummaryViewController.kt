package io.ipoli.android.challenge.add

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import kotlinx.android.synthetic.main.controller_add_challenge_summary.view.*
import kotlinx.android.synthetic.main.item_challenge_summary_quest.view.*
import io.ipoli.android.R
import io.ipoli.android.challenge.add.AddChallengeSummaryViewState.StateType.DATA_CHANGED
import io.ipoli.android.challenge.add.AddChallengeSummaryViewState.StateType.INITIAL
import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.text.DateFormatter
import io.ipoli.android.common.view.visible
import io.ipoli.android.quest.BaseQuest
import io.ipoli.android.quest.Icon
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.RepeatingQuest
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/10/18.
 */
sealed class AddChallengeSummaryAction : Action {
    object Save : AddChallengeSummaryAction()
}

object AddChallengeSummaryReducer : BaseViewStateReducer<AddChallengeSummaryViewState>() {
    override val stateKey = key<AddChallengeSummaryViewState>()

    override fun reduce(
        state: AppState,
        subState: AddChallengeSummaryViewState,
        action: Action
    ) = when (action) {
        AddChallengeAction.UpdateSummary -> {
            val s = state.stateFor(AddChallengeViewState::class.java)
            val motivationList = s.motivationList
            subState.copy(
                type = DATA_CHANGED,
                name = s.name,
                icon = s.icon,
                difficulty = s.difficulty,
                end = s.end,
                motivation1 = if (motivationList.isNotEmpty()) motivationList[0] else "",
                motivation2 = if (motivationList.size > 1) motivationList[1] else "",
                motivation3 = if (motivationList.size > 2) motivationList[2] else "",
                quests = s.allQuests.filter { s.selectedQuestIds.contains(it.id) }
            )
        }

        else -> subState
    }

    override fun defaultState() =
        AddChallengeSummaryViewState(
            type = INITIAL,
            name = "",
            icon = null,
            difficulty = Challenge.Difficulty.NORMAL,
            end = LocalDate.now(),
            quests = listOf(),
            motivation1 = "",
            motivation2 = "",
            motivation3 = ""
        )
}

data class AddChallengeSummaryViewState(
    val type: AddChallengeSummaryViewState.StateType,
    val name: String,
    val icon: Icon?,
    val difficulty: Challenge.Difficulty,
    val end: LocalDate,
    val motivation1: String,
    val motivation2: String,
    val motivation3: String,
    val quests: List<BaseQuest>
) : ViewState {
    enum class StateType {
        INITIAL,
        DATA_CHANGED
    }
}

class AddChallengeSummaryViewController(args: Bundle? = null) :
    ReduxViewController<AddChallengeSummaryAction, AddChallengeSummaryViewState, AddChallengeSummaryReducer>(
        args
    ) {
    override val reducer = AddChallengeSummaryReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.controller_add_challenge_summary, container, false)
        view.challengeQuests.layoutManager =
            LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)
        view.challengeQuests.adapter = QuestAdapter()
        view.challengeAccept.dispatchOnClick(AddChallengeSummaryAction.Save)
        return view
    }

    override fun colorLayoutBars() {}

    override fun render(state: AddChallengeSummaryViewState, view: View) {
        when (state.type) {
            DATA_CHANGED -> {
                view.challengeName.text = state.name
                view.challengeIcon.setImageDrawable(
                    IconicsDrawable(view.context)
                        .icon(state.iicon)
                        .colorRes(R.color.md_white)
                        .sizeDp(24)
                )
                view.challengeDifficulty.text = state.difficultyText
                view.challengeEnd.text = state.formattedEndDate
                view.challengeMotivation1.text = state.motivation1
                view.challengeMotivation1.visibility =
                    if (state.motivation1.isNotEmpty()) View.VISIBLE else View.GONE
                view.challengeMotivation2.text = state.motivation2
                view.challengeMotivation2.visibility =
                    if (state.motivation2.isNotEmpty()) View.VISIBLE else View.GONE
                view.challengeMotivation3.text = state.motivation3
                view.challengeMotivation3.visibility =
                    if (state.motivation3.isNotEmpty()) View.VISIBLE else View.GONE

                (view.challengeQuests.adapter as QuestAdapter).updateAll(state.questViewModels)
                if (state.quests.isNotEmpty()) {
                    view.challengeQuestsEmptyState.visibility = View.GONE
                    view.challengeQuests.visibility = View.VISIBLE
                } else {
                    view.challengeQuestsEmptyState.visibility = View.VISIBLE
                    view.challengeQuests.visibility = View.GONE
                }
            }
        }
    }

    data class QuestViewModel(
        val icon: IIcon,
        val name: String,
        val isRepeating: Boolean
    )

    inner class QuestAdapter(private var viewModels: List<QuestViewModel> = listOf()) :
        RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount() = viewModels.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = viewModels[position]
            val view = holder.itemView
            view.questIcon.setImageDrawable(
                IconicsDrawable(view.context)
                    .icon(vm.icon)
                    .colorRes(R.color.md_white)
                    .sizeDp(24)
            )
            view.questName.text = vm.name
            view.repeatingIndicator.visible = vm.isRepeating
        }

        fun updateAll(viewModels: List<QuestViewModel>) {
            this.viewModels = viewModels
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_challenge_summary_quest,
                    parent,
                    false
                )
            )

    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private val AddChallengeSummaryViewState.iicon: IIcon
        get() = icon?.androidIcon?.icon ?: GoogleMaterial.Icon.gmd_local_florist

    private val AddChallengeSummaryViewState.difficultyText: String
        get() = view!!.resources.getStringArray(R.array.difficulties)[difficulty.ordinal]

    private val AddChallengeSummaryViewState.formattedEndDate: String
        get() = DateFormatter.format(view!!.context, end)

    private val AddChallengeSummaryViewState.questViewModels: List<QuestViewModel>
        get() = quests.map {
            when (it) {
                is Quest -> QuestViewModel(
                    icon = it.icon?.androidIcon?.icon ?: GoogleMaterial.Icon.gmd_local_florist,
                    name = it.name,
                    isRepeating = false
                )
                is RepeatingQuest -> QuestViewModel(
                    icon = it.icon?.androidIcon?.icon ?: GoogleMaterial.Icon.gmd_local_florist,
                    name = it.name,
                    isRepeating = true
                )
            }

        }
}