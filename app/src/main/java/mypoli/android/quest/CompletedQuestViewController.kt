package mypoli.android.quest

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.ColorInt
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.controller_completed_quest.view.*
import kotlinx.android.synthetic.main.view_default_toolbar.view.*
import mypoli.android.R
import mypoli.android.common.datetime.Duration
import mypoli.android.common.datetime.Minute
import mypoli.android.common.mvi.MviViewController
import mypoli.android.common.text.DateFormatter
import mypoli.android.common.text.DurationFormatter
import mypoli.android.common.view.*
import mypoli.android.quest.CompletedQuestViewState.StateType.DATA_LOADED
import mypoli.android.quest.CompletedQuestViewState.Timer
import mypoli.android.quest.timer.TimerViewController
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

        rootRouter.getControllerWithTag(TimerViewController.TAG)?.let {
            rootRouter.popController(it)
        }

        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_completed_quest, container, false)

        setToolbar(view.toolbar)
        toolbarTitle = "Completed Quest"

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            rootRouter.popController(this)
//            popCurrentFromRootRouter()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAttach(view: View) {
        showBackButton()
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
                val color = state.color!!
                view.questName.setBackgroundResource(color.color500)

                view.questDate.text = DateFormatter.format(view.context, state.completeAt)
                view.questTime.text = "${state.startedAt} - ${state.finishedAt}"
                view.questProgressDuration.text =
                    DurationFormatter.formatShort(view.context, state.totalDuration!!.intValue)

                renderTimer(state.timer!!, view, state)
                renderBounty(view, state)

                view.questDurationProgress.secondaryProgressTintList =
                    ColorStateList.valueOf(colorRes(color.color100))

                view.questDurationProgress.progressTintList =
                    ColorStateList.valueOf(colorRes(color.color300))

                view.questDurationProgress.backgroundTintList =
                    ColorStateList.valueOf(colorRes(color.color500))

                view.level.text = "Lvl ${state.playerLevel!!}"

                view.levelProgress.backgroundTintList =
                    ColorStateList.valueOf(attrData(R.attr.colorAccent))

                view.levelProgress.progressTintList =
                    ColorStateList.valueOf(
                        lighten(attrData(R.attr.colorAccent), 0.6f)
                    )

                view.levelProgress.secondaryProgressTintList =
                    ColorStateList.valueOf(
                        lighten(attrData(R.attr.colorAccent), 0.3f)
                    )

                view.levelProgress.max = state.playerLevelMaxProgress!!
                view.levelProgress.secondaryProgress = state.playerLevelMaxProgress
                playProgressAnimation(view.levelProgress, 0, state.playerLevelProgress!!)
            }
        }
    }

    private fun lighten(@ColorInt color: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[1] *= factor
        return Color.HSVToColor(hsv)
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
        state: CompletedQuestViewState
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

                view.questDurationProgress.max = state.totalDuration!!.intValue

                view.questDurationProgress.secondaryProgress = state.totalDuration.intValue

                playProgressAnimation(
                    view.questDurationProgress,
                    0,
                    (timer.workDuration + timer.overdueWorkDuration).intValue
                )
            }

            is Timer.Countdown -> {
                view.pomodoroGroup.goneViews()
                view.timerGroup.showViews()

                view.questWorkTime.text = createDurationLabel(
                    view,
                    "Work",
                    state.totalDuration!!,
                    timer.overdueDuration
                )

                val isOverdue = timer.overdueDuration.intValue > 0

                view.questDurationProgress.max = timer.duration.intValue
                view.questDurationProgress.secondaryProgress = timer.duration.intValue

                if (isOverdue) {
                    view.questDurationProgress.secondaryProgressTintList =
                        ColorStateList.valueOf(colorRes(state.color!!.color300))

                    view.questDurationProgress.progressTintList =
                        ColorStateList.valueOf(colorRes(state.color.color700))

                    playProgressAnimation(
                        view.questDurationProgress,
                        0,
                        timer.overdueDuration.intValue
                    )

                } else {

                    playProgressAnimation(
                        view.questDurationProgress,
                        0,
                        state.totalDuration.intValue
                    )
                }
            }

            Timer.Untracked -> {
                view.pomodoroGroup.goneViews()
                view.timerGroup.goneViews()

                view.questDurationProgress.max = state.totalDuration!!.intValue

                playProgressAnimation(view.questDurationProgress, 0, state.totalDuration.intValue)
            }
        }
    }

    private fun playProgressAnimation(view: ProgressBar, from: Int, to: Int) {
        val animator = ObjectAnimator.ofInt(view, "progress", from, to)
        animator.duration = intRes(android.R.integer.config_mediumAnimTime).toLong()
        animator.start()
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