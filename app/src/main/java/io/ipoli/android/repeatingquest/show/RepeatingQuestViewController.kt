package io.ipoli.android.repeatingquest.show

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.LayoutRes
import android.support.design.widget.AppBarLayout
import android.view.*
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import kotlinx.android.synthetic.main.controller_repeating_quest.view.*
import io.ipoli.android.MainActivity
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.text.DateFormatter
import io.ipoli.android.common.text.DurationFormatter
import io.ipoli.android.common.view.*
import io.ipoli.android.repeatingquest.edit.EditRepeatingQuestViewController


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/21/2018.
 */
class RepeatingQuestViewController(args: Bundle? = null) :
    ReduxViewController<RepeatingQuestAction, RepeatingQuestViewState, RepeatingQuestReducer>(args) {

    override val reducer = RepeatingQuestReducer

    private lateinit var repeatingQuestId: String

    abstract class AppBarStateChangeListener : AppBarLayout.OnOffsetChangedListener {

        private var currentState = State.IDLE

        enum class State {
            EXPANDED,
            COLLAPSED,
            IDLE
        }

        override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
            when {
                verticalOffset == 0 -> {
                    if (currentState != State.EXPANDED) {
                        onStateChanged(appBarLayout, State.EXPANDED)
                    }
                    currentState = State.EXPANDED
                }
                Math.abs(verticalOffset) >= appBarLayout.totalScrollRange -> {
                    if (currentState != State.COLLAPSED) {
                        onStateChanged(appBarLayout, State.COLLAPSED)
                    }
                    currentState = State.COLLAPSED
                }
                else -> {
                    if (currentState != State.IDLE) {
                        onStateChanged(appBarLayout, State.IDLE)
                    }
                    currentState = State.IDLE
                }
            }
        }

        abstract fun onStateChanged(appBarLayout: AppBarLayout, state: State)
    }

    constructor(
        repeatingQuestId: String
    ) : this() {
        this.repeatingQuestId = repeatingQuestId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(
            R.layout.controller_repeating_quest,
            container,
            false
        )
        setToolbar(view.toolbar)
        val collapsingToolbar = view.collapsingToolbarContainer
        collapsingToolbar.isTitleEnabled = false

        view.appbar.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {

                appBarLayout.post {
                    if (state == State.EXPANDED) {
                        val supportActionBar = (activity as MainActivity).supportActionBar
                        supportActionBar?.setDisplayShowTitleEnabled(false)
                    } else if (state == State.COLLAPSED) {
                        val supportActionBar = (activity as MainActivity).supportActionBar
                        supportActionBar?.setDisplayShowTitleEnabled(true)
                    }
                }

            }
        })

        return view
    }

    override fun onCreateLoadAction() =
        RepeatingQuestAction.Load(repeatingQuestId)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.repeating_quest_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {

            android.R.id.home ->
                router.handleBack()

            R.id.actionEdit -> {
                val changeHandler = FadeChangeHandler()
                rootRouter.pushController(
                    RouterTransaction.with(EditRepeatingQuestViewController(repeatingQuestId))
                        .pushChangeHandler(changeHandler)
                        .popChangeHandler(changeHandler)
                )
                true
            }
            R.id.actionDelete -> {
                dispatch(RepeatingQuestAction.Remove(repeatingQuestId))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onAttach(view: View) {
        super.onAttach(view)
        showBackButton()
    }

    override fun onDetach(view: View) {
        (activity as MainActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        super.onDetach(view)
    }

    override fun render(state: RepeatingQuestViewState, view: View) {
        when (state) {
            is RepeatingQuestViewState.Changed -> {
                colorLayout(view, state)

                renderName(state, view)
                renderProgress(view, state)
                renderSummaryStats(view, state)
            }

            RepeatingQuestViewState.Removed ->
                router.handleBack()

            is RepeatingQuestViewState.HistoryChanged ->
                view.historyChart.updateData(state.history)
        }
    }

    private fun renderSummaryStats(
        view: View,
        state: RepeatingQuestViewState.Changed
    ) {
        view.nextText.text = state.nextScheduledDateText
//        view.categoryName.text = state.category.name
//        view.categoryImage.setImageResource(R.drawable.ic_context_chores_white)
//        view.totalTimeSpent.text = state.timeSpentText
//        view.nextScheduledDate.text = state.nextScheduledDateText
//        view.questStreak.text = state.currentStreak.toString()
    }

    private fun renderName(
        state: RepeatingQuestViewState.Changed,
        view: View
    ) {
        toolbarTitle = state.name
        view.questName.text = state.name
    }

    private fun renderProgress(
        view: View,
        state: RepeatingQuestViewState.Changed
    ) {
        val inflater = LayoutInflater.from(view.context)
        view.progressContainer.removeAllViews()

        for (vm in state.progressViewModels) {
            val progressViewEmpty = inflater.inflate(
                vm.layout,
                view.progressContainer,
                false
            )
            val progressViewEmptyBackground =
                progressViewEmpty.background as GradientDrawable
            progressViewEmptyBackground.setStroke(
                ViewUtils.dpToPx(1.5f, view.context).toInt(),
                vm.color
            )

            progressViewEmptyBackground.setColor(vm.color)

            view.progressContainer.addView(progressViewEmpty)
        }

        view.frequencyText.text = state.frequencyText
    }

    private fun colorLayout(
        view: View,
        state: RepeatingQuestViewState.Changed
    ) {
        view.appbar.setBackgroundColor(colorRes(state.color500))
        view.toolbar.setBackgroundColor(colorRes(state.color500))
        view.collapsingToolbarContainer.setContentScrimColor(colorRes(state.color500))
        activity?.window?.navigationBarColor = colorRes(state.color500)
        activity?.window?.statusBarColor = colorRes(state.color700)
    }

    private val RepeatingQuestViewState.Changed.color500
        get() = color.androidColor.color500

    private val RepeatingQuestViewState.Changed.color700
        get() = color.androidColor.color700

    private val RepeatingQuestViewState.Changed.progressViewModels
        get() = progress.map {
            when (it) {
                RepeatingQuestViewState.Changed.ProgressModel.COMPLETE -> {
                    ProgressViewModel(
                        R.layout.repeating_quest_progress_indicator_empty,
                        attrData(R.attr.colorAccent)
                    )
                }

                RepeatingQuestViewState.Changed.ProgressModel.INCOMPLETE -> {
                    ProgressViewModel(
                        R.layout.repeating_quest_progress_indicator_empty,
                        colorRes(R.color.md_white)
                    )
                }
            }
        }

    private val RepeatingQuestViewState.Changed.timeSpentText
        get() = Time.of(totalDuration.intValue).toString()

    private val RepeatingQuestViewState.Changed.nextScheduledDateText
        get() = when {
            isCompleted -> stringRes(R.string.completed)
            nextScheduledDate != null -> {
                var res = stringRes(
                    R.string.repeating_quest_next,
                    DateFormatter.format(view!!.context, nextScheduledDate)
                )
                res += if (startTime != null) {
                    " $startTime - $endTime"
                } else {
                    " " + stringRes(
                        R.string.quest_for_time,
                        DurationFormatter.formatShort(view!!.context, duration)
                    )
                }
                res
            }
            else -> stringRes(
                R.string.repeating_quest_next,
                stringRes(R.string.unscheduled)
            )
        }

    private val RepeatingQuestViewState.Changed.frequencyText
        get() = when (repeat) {
            RepeatingQuestViewState.Changed.RepeatType.Daily -> {
                "Every day"
            }

            is RepeatingQuestViewState.Changed.RepeatType.Weekly -> {
                repeat.frequency.let {
                    if (it == 1) {
                        "Once per week"
                    } else {
                        "$it times per week"
                    }
                }
            }

            is RepeatingQuestViewState.Changed.RepeatType.Monthly -> {
                repeat.frequency.let {
                    if (it == 1) {
                        "Once per month"
                    } else {
                        "$it times per month"
                    }
                }
            }

            RepeatingQuestViewState.Changed.RepeatType.Yearly -> {
                "Once per year"
            }
        }

    data class ProgressViewModel(@LayoutRes val layout: Int, @ColorInt val color: Int)
}