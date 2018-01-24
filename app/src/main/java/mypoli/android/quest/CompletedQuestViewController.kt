package mypoli.android.quest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.controller_completed_quest.view.*
import mypoli.android.R
import mypoli.android.common.datetime.Duration
import mypoli.android.common.datetime.Minute
import mypoli.android.common.mvi.MviViewController
import mypoli.android.common.text.DateFormatter
import mypoli.android.common.text.DurationFormatter
import mypoli.android.common.view.goneViews
import mypoli.android.common.view.showViews
import mypoli.android.quest.CompletedQuestViewState.StateType.DATA_LOADED
import mypoli.android.quest.CompletedQuestViewState.Timer
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/24/18.
 */
class CompletedQuestViewController :
    MviViewController<CompletedQuestViewState, CompletedQuestViewController, CompletedQuestPresenter, CompletedQuestIntent> {

    private lateinit var questId: String

    private val presenter by required { completedQuestPresenter }

    constructor(args: Bundle? = null) : super(args)

    constructor(questId: String) : super() {
        this.questId = questId
    }

    override fun createPresenter() = presenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.controller_completed_quest, container, false)
        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        send(CompletedQuestIntent.LoadData(questId))
    }

    override fun render(state: CompletedQuestViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {

                view.questName.text = state.name

                state.icon?.let {
                    view.questName.setCompoundDrawablesWithIntrinsicBounds(
                        IconicsDrawable(view.context)
                            .icon(it.icon)
                            .colorRes(R.color.md_white)
                            .sizeDp(24),
                        null,
                        null,
                        null
                    )
                }
                view.questName.setBackgroundResource(state.color!!.color500)

                view.questDate.text = DateFormatter.format(view.context, state.completeAt)
                view.questTime.text = "${state.startedAt} - ${state.finishedAt}"
                view.questProgressDuration.text =
                    DurationFormatter.formatShort(view.context, state.totalDuration!!.intValue)

                renderTimer(state.timer!!, view, state.totalDuration)
                renderBounty(view, state)
            }
        }
    }

    private fun renderBounty(view: View, state: CompletedQuestViewState) {
        view.bountyCoins.text = "+${state.coins} life coins"
        view.bountyXP.text = "+${state.experience} XP"
        if (state.bounty != null) {
            view.bonusItemGroup.showViews()
            view.bonusItemImage.setImageResource(state.bounty.image)
        } else {
            view.bonusItemGroup.goneViews()
        }
    }

    private fun renderTimer(
        timer: Timer,
        view: View,
        totalDuration: Duration<Minute>
    ) {
        when (timer) {
            is Timer.Pomodoro -> {
                view.pomodoroGroup.showViews()
                view.timerGroup.showViews()
                view.pomodoro.text =
                    "${timer.completedPomodoros}/${timer.totalPomodoros} pomodoros"

                view.questWorkTime.text = createDurationLabel(
                    view,
                    "Work",
                    timer.workDuration,
                    timer.overdueWorkDuration
                )

                view.questBreakTime.text = createDurationLabel(
                    view,
                    "Break",
                    timer.breakDuration,
                    timer.overdueBreakDuration
                )

            }

            is Timer.Countdown -> {
                view.pomodoroGroup.goneViews()
                view.timerGroup.showViews()

                view.questWorkTime.text = createDurationLabel(
                    view,
                    "Work",
                    totalDuration,
                    timer.overdueDuration
                )
            }

            Timer.Untracked -> {
                view.pomodoroGroup.goneViews()
                view.timerGroup.goneViews()
            }
        }
    }

    private fun createDurationLabel(
        view: View,
        startLabel: String,
        duration: Duration<Minute>,
        overdueDuration: Duration<Minute>
    ): String {
        var label = "$startLabel: ${DurationFormatter.formatShort(
            view.context,
            duration.intValue
        )}"
        if (overdueDuration.intValue > 0) {
            label += " (${DurationFormatter.formatShort(
                view.context,
                overdueDuration.intValue
            )})"
        }
        return label
    }
}