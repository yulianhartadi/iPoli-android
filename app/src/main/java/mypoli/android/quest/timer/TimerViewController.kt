package mypoli.android.quest.timer

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bluelinelabs.conductor.RouterTransaction
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.controller_timer.view.*
import kotlinx.android.synthetic.main.item_timer_progress.view.*
import mypoli.android.R
import mypoli.android.common.ViewUtils
import mypoli.android.common.mvi.MviViewController
import mypoli.android.common.view.*
import mypoli.android.quest.CompletedQuestViewController
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 6.01.18.
 */
class TimerViewController :
    MviViewController<TimerViewState, TimerViewController, TimerPresenter, TimerIntent> {

    private lateinit var questId: String

    private val handler = Handler(Looper.getMainLooper())

    private val presenter by required { timerPresenter }

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
        val view = inflater.inflate(R.layout.controller_timer, container, false)

        renderTimerButton(view.startStop, TimerButton.START)

        val icon = IconicsDrawable(view.context)
            .icon(Ionicons.Icon.ion_android_done)
            .color(attrData(R.attr.colorAccent))
            .sizeDp(22)

        view.complete.setImageDrawable(icon)

        view.complete.post {
            view.complete.visibility = View.GONE
        }

        val minusIcon = IconicsDrawable(view.context)
            .icon(Ionicons.Icon.ion_minus)
            .color(attrData(R.attr.colorAccent))
            .sizeDp(22)

        view.removePomodoro.setImageDrawable(minusIcon)

        val addIcon = IconicsDrawable(view.context)
            .icon(Ionicons.Icon.ion_plus)
            .color(attrData(R.attr.colorAccent))
            .sizeDp(22)

        view.addPomodoro.setImageDrawable(addIcon)

        return view
    }

    private fun createProgressView(view: View) =
        LayoutInflater.from(view.context).inflate(
            R.layout.item_timer_progress,
            view.timerProgressContainer,
            false
        )

    override fun onAttach(view: View) {
        super.onAttach(view)
        enterFullScreen()
        send(TimerIntent.LoadData(questId))
    }

    override fun onDetach(view: View) {
        handler.removeCallbacksAndMessages(null)
        cancelAnimations(view)
        exitFullScreen()
        super.onDetach(view)
    }

    override fun render(state: TimerViewState, view: View) {
        view.questName.text = state.questName
        view.timerLabel.text = state.timerLabel

        when (state.type) {
            TimerViewState.StateType.SHOW_POMODORO -> {
                renderTimerProgress(view, state)
                renderTypeSwitch(view, state)
                view.pomodoroIndicatorsGroup.visible = true
                renderTimerIndicatorsProgress(view, state)

                view.addPomodoro.sendOnClick(TimerIntent.AddPomodoro)
                view.removePomodoro.sendOnClick(TimerIntent.RemovePomodoro)
                view.startStop.sendOnClick(TimerIntent.Start)
                view.complete.visibility = View.GONE
            }

            TimerViewState.StateType.SHOW_COUNTDOWN -> {
                renderTimerProgress(view, state)
                renderTypeSwitch(view, state)
                view.startStop.sendOnClick(TimerIntent.Start)
                view.pomodoroIndicatorsGroup.visible = false
            }

            TimerViewState.StateType.RESUMED -> {
                startTimer(view, state)
            }

            TimerViewState.StateType.TIMER_REPLACED -> {
                showShortToast(R.string.timer_replaced)
            }

            TimerViewState.StateType.TIMER_STOPPED -> {
                handler.removeCallbacksAndMessages(null)
                cancelAnimations(view)

                renderTimerButton(view.startStop, TimerButton.START)
                view.startStop.sendOnClick(TimerIntent.Start)
                view.setOnClickListener(null)
                view.complete.visibility = View.GONE
            }

            TimerViewState.StateType.RUNNING -> {
                view.timerProgress.progress = state.timerProgress
                if (state.showCompletePomodoroButton) {
                    renderTimerButton(view.startStop, TimerButton.DONE)
                    view.startStop.sendOnClick(TimerIntent.CompletePomodoro)
                }
            }

            TimerViewState.StateType.QUEST_COMPLETED ->
                showCompletedQuest(state.quest!!.id)
        }
    }

    private fun cancelAnimations(view: View) {
        view.notImportantGroup.views().forEach {
            it.animate().cancel()
            it.alpha = 1f
        }

        view.startStop.animate().cancel()
        view.startStop.y = originalTimerButtonsY(view)

        view.complete.animate().cancel()
        view.complete.y = originalTimerButtonsY(view)

        val childCount = view.timerProgressContainer.childCount
        for (i in 0 until childCount) {
            val child = view.timerProgressContainer.getChildAt(i)
            child.animate().cancel()
            child.alpha = 1f
        }
    }

    private fun originalTimerButtonsY(view: View) =
        view.timerLabel.y + view.timerLabel.height + ViewUtils.dpToPx(32f, view.context)

    private fun renderTypeSwitch(view: View, state: TimerViewState) {
        view.timerType.visible = state.showTimerTypeSwitch
        view.timerType.isChecked = state.timerType == TimerViewState.TimerType.POMODORO
        view.timerType.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) send(TimerIntent.ShowPomodoroTimer)
            else send(TimerIntent.ShowCountDownTimer)
        }
    }

    private fun startTimer(view: View, state: TimerViewState) {

        renderTimerProgress(view, state)
        renderTypeSwitch(view, state)
        renderTimerIndicatorsProgress(view, state)

        handler.removeCallbacksAndMessages(null)
        cancelAnimations(view)

        var updateTimer = {}

        updateTimer = {
            send(TimerIntent.Tick)
            handler.postDelayed(updateTimer, 1000)
        }

        handler.postDelayed(updateTimer, 1000)

        renderTimerButton(view.startStop, TimerButton.STOP)
        view.startStop.sendOnClick(TimerIntent.Stop)

        if (state.timerType == TimerViewState.TimerType.POMODORO) {
            view.pomodoroIndicatorsGroup.visible = true
            playBlinkIndicatorAnimation(view.timerProgressContainer.getChildAt(state.currentProgressIndicator))
        } else {
            view.complete.visibility = View.VISIBLE
            view.pomodoroIndicatorsGroup.visible = false
            view.complete.sendOnClick(TimerIntent.CompleteQuest)
        }

        view.setOnClickListener {
            playShowNotImportantViewsAnimation(view)
        }
        playHideNotImportantViewsAnimation(view)
    }

    private fun renderTimerProgress(
        view: View,
        state: TimerViewState
    ) {
        view.timerProgress.max = state.maxTimerProgress
        view.timerProgress.secondaryProgress = state.maxTimerProgress
        view.timerProgress.progress = state.timerProgress
    }

    private fun playBlinkIndicatorAnimation(view: View, reverse: Boolean = false) {
        view
            .animate()
            .alpha(if (reverse) 1f else 0f)
            .setDuration(mediumAnimTime)
            .withEndAction {
                playBlinkIndicatorAnimation(view, !reverse)
            }
            .start()
    }

    private fun renderTimerButton(button: ImageView, type: TimerButton) {
        val iconImage = when (type) {
            TimerButton.START -> Ionicons.Icon.ion_play
            TimerButton.STOP -> Ionicons.Icon.ion_stop
            TimerButton.DONE -> Ionicons.Icon.ion_android_done
        }

        val icon = IconicsDrawable(button.context)
            .icon(iconImage)
            .color(attrData(R.attr.colorAccent))
            .sizeDp(22)

        button.setImageDrawable(icon)
    }

    private fun playShowNotImportantViewsAnimation(view: View) {
        view.startStop
            .animate()
            .y(originalTimerButtonsY(view))
            .setDuration(shortAnimTime)
            .withEndAction {
                view.notImportantGroup.views().forEach {
                    it
                        .animate()
                        .alpha(1f)
                        .setDuration(longAnimTime)
                        .setStartDelay(0)
                        .withEndAction {
                            playHideNotImportantViewsAnimation(view)
                        }
                        .start()
                }
            }
            .start()

        view.complete
            .animate()
            .y(originalTimerButtonsY(view))
            .setDuration(shortAnimTime)
            .start()
    }

    private fun playHideNotImportantViewsAnimation(view: View) {
        view.notImportantGroup.views().forEach {
            it
                .animate()
                .alpha(0f)
                .setDuration(longAnimTime)
                .setStartDelay(3000)
                .withEndAction {
                    val centerY = view.timerProgress.y + view.timerProgress.height / 2
                    val y = centerY - view.startStop.height / 2
                    view.startStop
                        .animate()
                        .y(y)
                        .setDuration(shortAnimTime)
                        .start()

                    view.complete
                        .animate()
                        .y(y)
                        .setDuration(shortAnimTime)
                        .start()
                }
                .start()
        }

    }

    private fun renderTimerIndicatorsProgress(view: View, state: TimerViewState) {
        view.timerProgressContainer.removeAllViews()
        state.pomodoroProgress.forEach {
            addProgressIndicator(view, it)
        }
    }

    private fun addProgressIndicator(view: View, progress: PomodoroProgress) {
        val progressView = createProgressView(view)
        val progressDrawable = resources!!.getDrawable(
            R.drawable.timer_progress_item,
            view.context.theme
        ) as GradientDrawable

        when (progress) {
            PomodoroProgress.INCOMPLETE_WORK -> {
                progressDrawable.setColor(colorRes(R.color.md_grey_300))
            }

            PomodoroProgress.COMPLETE_WORK -> {
                progressDrawable.setColor(attrData(R.attr.colorAccent))
            }

            PomodoroProgress.INCOMPLETE_SHORT_BREAK -> {
                progressDrawable.setColor(colorRes(R.color.md_grey_300))
                progressView.setScale(0.5f)
            }

            PomodoroProgress.COMPLETE_SHORT_BREAK -> {
                progressDrawable.setColor(attrData(R.attr.colorAccent))
                progressView.setScale(0.5f)
            }

            PomodoroProgress.INCOMPLETE_LONG_BREAK -> {
                progressDrawable.setColor(colorRes(R.color.md_grey_300))
                progressView.setScale(0.75f)
            }

            PomodoroProgress.COMPLETE_LONG_BREAK -> {
                progressDrawable.setColor(attrData(R.attr.colorAccent))
                progressView.setScale(0.75f)
            }
        }
        progressView.timerItemProgress.background = progressDrawable

        if (view.timerProgressContainer.childCount > 0) {
            val lp = progressView.layoutParams as ViewGroup.MarginLayoutParams
            lp.marginStart = ViewUtils.dpToPx(4f, view.context).toInt()
        }

        view.timerProgressContainer.addView(progressView)
    }

    private fun showCompletedQuest(questId: String) {
        pushWithRootRouter(RouterTransaction.with(CompletedQuestViewController(questId)))
    }

    enum class TimerButton {
        START, STOP, DONE
    }

    companion object {
        const val TAG = "TimerViewController"
    }
}

enum class PomodoroProgress {
    INCOMPLETE_SHORT_BREAK,
    COMPLETE_SHORT_BREAK,
    INCOMPLETE_LONG_BREAK,
    COMPLETE_LONG_BREAK,
    INCOMPLETE_WORK,
    COMPLETE_WORK
}

