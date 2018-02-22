package mypoli.android.repeatingquest.show

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.LayoutRes
import android.support.v4.view.ViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.controller_repeating_quest.view.*
import mypoli.android.MainActivity
import mypoli.android.R
import mypoli.android.common.ViewUtils
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.view.*


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/21/2018.
 */
class RepeatingQuestViewController :
    ReduxViewController<RepeatingQuestAction, RepeatingQuestViewState, RepeatingQuestReducer> {

    override val reducer = RepeatingQuestReducer

    private lateinit var repeatingQuestId: String

    constructor(args: Bundle? = null) : super(args)

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
        val view = inflater.inflate(
            R.layout.controller_repeating_quest,
            container,
            false
        )
        setToolbar(view.toolbar)
        val collapsingToolbar = view.collapsingToolbarContainer
        collapsingToolbar.isTitleEnabled = false

        view.appbar.addOnOffsetChangedListener({ _, verticalOffset ->
            val showTitleThreshold = 2 * ViewCompat.getMinimumHeight(collapsingToolbar)
            val supportActionBar = (activity as MainActivity).supportActionBar
            if (collapsingToolbar.height + verticalOffset < showTitleThreshold) {
                supportActionBar?.setDisplayShowTitleEnabled(true)
            } else {
                supportActionBar?.setDisplayShowTitleEnabled(false)
            }
        })

        return view
    }

    override fun onCreateLoadAction() =
        RepeatingQuestAction.Load(repeatingQuestId)

    override fun render(state: RepeatingQuestViewState, view: View) {
        when (state) {
            is RepeatingQuestViewState.Changed -> {
                toolbarTitle = state.name
                view.questName.text = state.name
                colorLayout(view, state)

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


//                val totalCount = currentPeriodHistory.getTotalCount()
//                val completedCount = currentPeriodHistory.getCompletedCount()
//                if (totalCount > 7) {
//                    val progressText = inflater.inflate(
//                        R.layout.repeating_quest_progress_text,
//                        progressContainer,
//                        false
//                    ) as TextView
//                    progressText.setText(
//                        getString(
//                            R.string.repeating_quest_completed_this_month,
//                            completedCount
//                        )
//                    )
//                    progressContainer.addView(progressText)
//                    return
//                }
//
//                val incomplete = currentPeriodHistory.getRemainingCount()
//
//                var progressColor = R.color.colorAccent
//
//                if (category == Category.WORK || category == Category.FUN || category == Category.CHORES) {
//                    progressColor = R.color.colorAccentAlternative
//                }
//
//                for (i in 1..completedCount) {
//                    val progressViewEmpty = inflater.inflate(
//                        R.layout.repeating_quest_progress_indicator_empty,
//                        progressContainer,
//                        false
//                    )
//                    val progressViewEmptyBackground =
//                        progressViewEmpty.getBackground() as GradientDrawable
//
//                    progressViewEmptyBackground.setStroke(
//                        ViewUtils.dpToPx(1.5f, resources) as Int,
//                        ContextCompat.getColor(this, progressColor)
//                    )
//                    progressViewEmptyBackground.setColor(
//                        ContextCompat.getColor(
//                            this,
//                            progressColor
//                        )
//                    )
//                    progressContainer.addView(progressViewEmpty)
//                }
//
//                for (i in 1..incomplete) {
//                    val progressViewEmpty = inflater.inflate(
//                        R.layout.repeating_quest_progress_indicator_empty,
//                        progressContainer,
//                        false
//                    )
//                    val progressViewEmptyBackground =
//                        progressViewEmpty.getBackground() as GradientDrawable
//                    progressViewEmptyBackground.setStroke(
//                        ViewUtils.dpToPx(1.5f, resources) as Int,
//                        Color.WHITE
//                    )
//                    progressViewEmptyBackground.setColor(Color.WHITE)
//                    progressContainer.addView(progressViewEmpty)
//                }
            }
        }
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
        get() = AndroidColor.valueOf(color.name).color500

    private val RepeatingQuestViewState.Changed.color700
        get() = AndroidColor.valueOf(color.name).color700

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

    data class ProgressViewModel(@LayoutRes val layout: Int, @ColorInt val color: Int)
}