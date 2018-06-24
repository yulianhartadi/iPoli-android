package io.ipoli.android.quest

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.annotation.ColorInt
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.R
import io.ipoli.android.common.datetime.Duration
import io.ipoli.android.common.datetime.Minute
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.text.DateFormatter
import io.ipoli.android.common.text.DurationFormatter
import io.ipoli.android.common.view.*
import io.ipoli.android.quest.CompletedQuestViewState.StateType.DATA_LOADED
import io.ipoli.android.quest.CompletedQuestViewState.Timer
import io.ipoli.android.tag.Tag
import kotlinx.android.synthetic.main.controller_completed_quest.view.*
import kotlinx.android.synthetic.main.item_quest_tag_list.view.*
import kotlinx.android.synthetic.main.view_default_toolbar.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 1/24/18.
 */
class CompletedQuestViewController :
    ReduxViewController<CompletedQuestAction, CompletedQuestViewState, CompletedQuestReducer> {

    private lateinit var questId: String

    override val reducer = CompletedQuestReducer

    constructor(args: Bundle? = null) : super(args)

    constructor(questId: String) : super() {
        this.questId = questId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_completed_quest, container, false)

        setToolbar(view.toolbar)
        toolbarTitle = "Completed Quest"

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            return router.handleBack()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateLoadAction() = CompletedQuestAction.Load(questId)

    override fun onAttach(view: View) {
        showBackButton()
        super.onAttach(view)

    }

    override fun render(state: CompletedQuestViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {

                view.questName.text = state.name
                renderTags(state.tags, view)

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
                view.questTime.text =
                    "${state.startedAt!!.toString(shouldUse24HourFormat)} - ${state.finishedAt!!.toString(
                        shouldUse24HourFormat
                    )}"
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

                view.levelProgress.animateProgressFromZero(state.playerLevelProgress!!)
            }

            else -> {
            }
        }
    }

    private fun renderTags(
        tags: List<Tag>,
        view: View
    ) {
        view.questTagList.removeAllViews()

        val inflater = LayoutInflater.from(activity!!)
        tags.forEach { tag ->
            val item = inflater.inflate(R.layout.item_quest_tag_list, view.questTagList, false)
            renderTag(item, tag)
            view.questTagList.addView(item)
        }
    }

    private fun renderTag(view: View, tag: Tag) {
        view.tagName.text = tag.name
        val indicator = view.tagName.compoundDrawablesRelative[0] as GradientDrawable
        indicator.setColor(colorRes(AndroidColor.valueOf(tag.color.name).color500))
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

                view.questDurationProgress.animateProgressFromZero((timer.workDuration + timer.overdueWorkDuration).intValue)
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

                    view.questDurationProgress.animateProgressFromZero(timer.overdueDuration.intValue)

                } else {

                    view.questDurationProgress.animateProgressFromZero(state.totalDuration.intValue)
                }
            }

            Timer.Untracked -> {
                view.pomodoroGroup.goneViews()
                view.timerGroup.goneViews()

                view.questDurationProgress.max = state.totalDuration!!.intValue
                view.questDurationProgress.animateProgressFromZero(state.totalDuration.intValue)
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