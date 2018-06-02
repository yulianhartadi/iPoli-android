package io.ipoli.android.growth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.design.widget.TabLayout
import android.support.v7.widget.LinearLayoutManager
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.chart.AwesomenessScoreMarker
import io.ipoli.android.common.chart.FocusHoursMarker
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.text.DurationFormatter
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.anim.AccelerateDecelerateEasingFunction
import io.ipoli.android.common.view.recyclerview.BaseRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.RecyclerViewViewModel
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.growth.usecase.CalculateGrowthStatsUseCase
import kotlinx.android.synthetic.main.controller_growth.view.*
import kotlinx.android.synthetic.main.item_growth_challenge.view.*
import kotlinx.android.synthetic.main.item_growth_tag.view.*
import kotlinx.android.synthetic.main.view_loader.view.*
import org.threeten.bp.format.TextStyle
import java.util.*
import kotlin.math.roundToInt

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 05/23/2018.
 */
class GrowthViewController(args: Bundle? = null) :
    ReduxViewController<GrowthAction, GrowthViewState, GrowthReducer>(args = args) {

    override val reducer = GrowthReducer

    private val pieFormatter =
        IValueFormatter { value, _, _, _ -> "${value.roundToInt()}%" }

    private val tabListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {

        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {

        }

        override fun onTabSelected(tab: TabLayout.Tab) {
            dispatch(TAB_INDEX_TO_ACTION[tab.position]!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = container.inflate(R.layout.controller_growth)

        setupTagTimeChart(view.tagTimeChart)

        view.focusTimeChart.marker = FocusHoursMarker(view.context)
        setupLineChart(view.focusTimeChart)
        view.awesomenessChart.marker = AwesomenessScoreMarker(view.context)
        setupLineChart(view.awesomenessChart)
        view.awesomenessChart.axisRight.axisMaximum = 6f

        view.challengesProgress.layoutManager = LinearLayoutManager(view.context)
        view.challengesProgress.adapter = ChallengeAdapter()

        view.tagList.layoutManager = LinearLayoutManager(view.context)
        view.tagList.adapter = TagTimeSpentAdapter()

        return view
    }

    private fun setupTagTimeChart(chart: PieChart) {

        chart.legend.isEnabled = false
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)
        chart.transparentCircleRadius = chart.holeRadius
        chart.setUsePercentValues(true)
        chart.setEntryLabelColor(colorRes(R.color.md_dark_text_54))
        chart.setEntryLabelTextSize(14f)
        chart.setExtraOffsets(0.0f, 5.0f, 0.0f, 5.0f)
        chart.rotationAngle = 270f
    }

    override fun onCreateLoadAction() = GrowthAction.Load

    override fun onAttach(view: View) {
        view.tabs.getTabAt(0)!!.select()
        view.tabs.addOnTabSelectedListener(tabListener)
        super.onAttach(view)
        toolbarTitle = stringRes(R.string.drawer_growth)
    }

    override fun onDetach(view: View) {
        view.tabs.removeOnTabSelectedListener(tabListener)
        super.onDetach(view)
    }

    override fun render(state: GrowthViewState, view: View) {
        when (state.type) {

            GrowthViewState.StateType.TODAY_DATA_LOADED -> {

                view.loader.gone()
                view.growthContentContainer.visible()
                view.focusTimeTitle.setText(R.string.growth_focus_time_title)
                renderSummaryStats(state, view)
                renderTagTimeSpent(state, view)
                renderChallengeProgress(state, view)
                renderCharts(state, view)
            }

            GrowthViewState.StateType.WEEK_DATA_LOADED -> {
                view.focusTimeTitle.setText(R.string.growth_focus_time_title)
                renderSummaryStats(state, view)
                renderTagTimeSpent(state, view)
                renderChallengeProgress(state, view)
                renderCharts(state, view)
            }

            GrowthViewState.StateType.MONTH_DATA_LOADED -> {
                view.focusTimeTitle.setText(R.string.growth_focus_time_month_title)
                renderSummaryStats(state, view)
                renderTagTimeSpent(state, view)
                renderChallengeProgress(state, view)
                renderCharts(state, view)
            }

            else -> {
            }
        }
    }

    private fun renderTagTimeSpent(state: GrowthViewState, view: View) {

        if (state.tagTimeSpent.isEmpty()) {
            val textRes = when (state.type) {
                GrowthViewState.StateType.TODAY_DATA_LOADED ->
                    R.string.growth_tag_time_spent_no_progress_today

                GrowthViewState.StateType.WEEK_DATA_LOADED ->
                    R.string.growth_tag_time_spent_no_progress_week

                GrowthViewState.StateType.MONTH_DATA_LOADED ->
                    R.string.growth_tag_time_spent_no_progress_month

                else -> throw IllegalArgumentException("Unknown text for state type ${state.type}")
            }

            view.tagList.gone()
            view.tagTimeChart.gone()
            view.tagTimeSpentEmpty.setText(textRes)
            view.tagTimeSpentEmpty.visible()
        } else {
            view.tagTimeSpentEmpty.gone()
            view.tagList.visible()
            view.tagTimeChart.visible()
            (view.tagList.adapter as TagTimeSpentAdapter).updateAll(state.tagTimeSpentViewModels)
            view.tagTimeChart.post {
                renderTagTimeSpentChart(state, view)
            }
        }
    }

    private fun renderChallengeProgress(state: GrowthViewState, view: View) {
        if (state.challengeProgress.isEmpty()) {
            val textRes = when (state.type) {
                GrowthViewState.StateType.TODAY_DATA_LOADED ->
                    R.string.growth_challenge_no_progress_today

                GrowthViewState.StateType.WEEK_DATA_LOADED ->
                    R.string.growth_challenge_no_progress_week

                GrowthViewState.StateType.MONTH_DATA_LOADED ->
                    R.string.growth_challenge_no_progress_month

                else -> throw IllegalArgumentException("Unknown text for state type ${state.type}")
            }
            view.challengeProgressEmpty.setText(textRes)
            view.challengeProgressEmpty.visible()
        } else {
            view.challengeProgressEmpty.gone()
            (view.challengesProgress.adapter as ChallengeAdapter).updateAll(state.challengeViewModels)
        }
    }

    private fun renderSummaryStats(
        state: GrowthViewState,
        view: View
    ) {
        val hoursAnim = ValueAnimator.ofInt(0, state.timeSpent.asHours.intValue)

        hoursAnim.addUpdateListener {
            view.hoursValue.text = "${it.animatedValue}"
        }

        val xpAnim = ValueAnimator.ofInt(0, state.experience)

        xpAnim.addUpdateListener {
            view.xpValue.text = "${it.animatedValue}"
        }

        val coinsAnim = ValueAnimator.ofInt(0, state.coins)

        coinsAnim.addUpdateListener {
            view.coinsValue.text = "${it.animatedValue}"
        }

        val anim = AnimatorSet()
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.duration = longAnimTime
        anim.playTogether(hoursAnim, xpAnim, coinsAnim)
        anim.start()
    }

    private fun renderCharts(state: GrowthViewState, view: View) {
        val valueFormatter = IndexAxisValueFormatter()

        valueFormatter.values = state.progressEntries
            .map {
                when (it) {
                    is CalculateGrowthStatsUseCase.ProgressEntry.Today ->
                        it.periodEnd.toString(shouldUse24HourFormat)

                    is CalculateGrowthStatsUseCase.ProgressEntry.Week ->
                        it.date.dayOfWeek.getDisplayName(
                            TextStyle.SHORT_STANDALONE,
                            Locale.getDefault()
                        )

                    is CalculateGrowthStatsUseCase.ProgressEntry.Month -> {
                        val startMonth = it.weekStart.month
                        val endMonth = it.weekEnd.month

                        val sm = startMonth.getDisplayName(
                            TextStyle.SHORT,
                            Locale.getDefault()
                        )

                        if (startMonth != endMonth) {

                            val em = endMonth.getDisplayName(
                                TextStyle.SHORT,
                                Locale.getDefault()
                            )

                            "${it.weekStart.dayOfMonth} $sm - ${it.weekEnd.dayOfMonth} $em"
                        } else {
                            "${it.weekStart.dayOfMonth} - ${it.weekEnd.dayOfMonth} $sm"
                        }
                    }
                }
            }.toTypedArray()

        view.focusTimeChart.xAxis.valueFormatter = valueFormatter
        view.awesomenessChart.xAxis.valueFormatter = valueFormatter
        view.focusTimeChart.highlightValue(null)
        view.awesomenessChart.highlightValue(null)


        view.focusTimeChart.post {
            renderFocusTime(state, view)
        }

        view.awesomenessChart.post {
            renderAwesomenessScore(state, view)
        }
    }

    private fun renderTagTimeSpentChart(state: GrowthViewState, view: View) {

        val ts = state.tagTimeSpentForChart

        if (ts.isEmpty()) {
            view.tagTimeChart.gone()
            return
        }

        view.tagTimeChart.visible()

        val pieData = ts.map {
            PieEntry(it.timeSpent.intValue.toFloat(), it.name)
        }

        val dataSet = PieDataSet(pieData, "")
        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
        dataSet.valueTextColor = colorRes(R.color.md_white)
        dataSet.valueTextSize = 12f
        dataSet.setDrawIcons(false)
        dataSet.colors = ts.map { colorRes(it.color.androidColor.color500) }

        dataSet.valueLinePart1Length = 0.5f
        dataSet.valueLinePart2Length = 1f
        dataSet.isUsingSliceColorAsValueLineColor = true

        val data = PieData(dataSet)
        data.setValueFormatter(pieFormatter)
        view.tagTimeChart.data = data
        view.tagTimeChart.highlightValues(null)
        view.tagTimeChart.invalidate()
        view.tagTimeChart.animateY(longAnimTime.toInt(), AccelerateDecelerateEasingFunction)
    }

    private fun renderFocusTime(
        state: GrowthViewState,
        view: View
    ) {

        view.focusTimeChart.axisRight.axisMaximum =
            Math.max(
                state.productiveHoursGoal.toFloat() + 1,
                state.progressEntries.map { it.productiveMinutes.asHours.intValue }.max()!! + 1f
            )
        view.focusTimeChart.axisRight.axisMinimum = 0f

        val d1 = createLineDataSet(
            createFocusTimeData(state),
            stringRes(R.string.productive_hours),
            colorRes(R.color.md_blue_500)
        )

        val set = LineDataSet(

            state.progressEntries.mapIndexed { i, _ ->
                Entry(i.toFloat(), state.productiveHoursGoal.toFloat())
            }, stringRes(R.string.daily_goal)
        )

        set.isHighlightEnabled = false
        set.color = attrData(R.attr.colorAccent)
        set.lineWidth = ViewUtils.dpToPx(0.5f, view.context)
        set.setDrawCircles(false)

        set.mode = LineDataSet.Mode.LINEAR
        set.setDrawValues(false)
        set.axisDependency = YAxis.AxisDependency.RIGHT

        view.focusTimeChart.data = LineData(set, d1)
        view.focusTimeChart.invalidate()
        view.focusTimeChart.animateX(longAnimTime.toInt(), AccelerateDecelerateEasingFunction)
    }

    private fun renderAwesomenessScore(state: GrowthViewState, view: View) {

        view.awesomenessChart.axisRight.axisMinimum = 0f

        val dummySet = LineDataSet(

            state.progressEntries.mapIndexed { i, _ ->
                Entry(i.toFloat(), state.productiveHoursGoal.toFloat())
            }, ""
        )

        dummySet.isVisible = false
        dummySet.isHighlightEnabled = false
        dummySet.axisDependency = YAxis.AxisDependency.RIGHT
        dummySet.color = colorRes(R.color.md_white)

        view.awesomenessChart.data =
            LineData(
                createLineDataSet(
                    createAwesomeScoreData(state),
                    stringRes(R.string.score),
                    colorRes(R.color.md_yellow_800)
                ),
                dummySet
            )
        view.awesomenessChart.invalidate()
        view.awesomenessChart.animateX(longAnimTime.toInt(), AccelerateDecelerateEasingFunction)
    }

    private fun setupLineChart(chart: LineChart) {
        with(chart) {
            description = null
            setExtraOffsets(0f, 0f, 0f, 0f)
            isDoubleTapToZoomEnabled = false

            setTouchEnabled(true)
            setPinchZoom(false)
            extraTopOffset = 16f
            extraBottomOffset = 16f

            setDrawGridBackground(false)

            axisRight.axisMinimum = 0f
            axisRight.spaceTop = 0f
            axisRight.granularity = 1f
            axisRight.setDrawAxisLine(false)
            axisRight.textSize = ViewUtils.spToPx(5, activity!!).toFloat()
            axisRight.textColor = colorRes(R.color.md_dark_text_54)
            axisRight.setValueFormatter { value, _ -> "    ${value.toInt()}" }

            axisLeft.isEnabled = false

            xAxis.yOffset = ViewUtils.dpToPx(6f, activity!!)
            xAxis.isGranularityEnabled = true
            xAxis.granularity = 1f
            xAxis.textSize = ViewUtils.spToPx(4.5f, activity!!).toFloat()
            xAxis.textColor = colorRes(R.color.md_dark_text_54)
            xAxis.setDrawGridLines(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setAvoidFirstLastClipping(true)

            xAxis.labelRotationAngle = 335f

            legend.textColor = colorRes(R.color.md_dark_text_54)
            legend.textSize = ViewUtils.spToPx(5, activity!!).toFloat()
            legend.form = Legend.LegendForm.CIRCLE
            legend.xEntrySpace = ViewUtils.dpToPx(4f, activity!!)

            setDrawBorders(false)
        }
    }

    private fun createAwesomeScoreData(state: GrowthViewState) =
        state.progressEntries.slice(0.until(state.showProgressCount)).mapIndexed { i, p ->
            Entry(i.toFloat(), p.awesomenessScore.toFloat(), p.awesomenessScore.toFloat())
        }

    private fun createFocusTimeData(state: GrowthViewState) =
        state.progressEntries.slice(0.until(state.showProgressCount)).mapIndexed { i, p ->
            Entry(i.toFloat(), (p.productiveMinutes.intValue / 60f), p.productiveMinutes)
        }

    private fun createLineDataSet(
        entries: List<Entry>,
        label: String,
        color: Int
    ): LineDataSet {
        val set = LineDataSet(entries, label)
        set.color = color
        set.lineWidth = ViewUtils.dpToPx(1f, activity!!)
        set.setDrawCircles(true)
        set.circleRadius = ViewUtils.dpToPx(3f, activity!!)
        set.setCircleColor(colorRes(R.color.md_white))
        set.circleHoleRadius = ViewUtils.dpToPx(2f, activity!!)
        set.circleHoleColor = color
        set.highLightColor = attrData(R.attr.colorAccent)

        set.mode = LineDataSet.Mode.LINEAR
        set.setDrawValues(false)
        set.axisDependency = YAxis.AxisDependency.RIGHT
        return set
    }

    data class ChallengeViewModel(
        override val id: String,
        val name: String,
        @ColorRes val color: Int,
        val progress: Int,
        val progressText: String
    ) : RecyclerViewViewModel

    inner class ChallengeAdapter :
        BaseRecyclerViewAdapter<ChallengeViewModel>(R.layout.item_growth_challenge) {

        override fun onBindViewModel(vm: ChallengeViewModel, view: View, holder: SimpleViewHolder) {
            view.cName.text = vm.name

            val drawable = view.cProgress.progressDrawable as LayerDrawable
            val progressDrawable = drawable.getDrawable(1)
            progressDrawable.setColorFilter(
                colorRes(vm.color),
                PorterDuff.Mode.SRC_ATOP
            )

            val animator = ObjectAnimator.ofInt(view.cProgress, "progress", 0, vm.progress)
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.duration = intRes(android.R.integer.config_mediumAnimTime).toLong()
            animator.start()

            view.cProgressText.text = vm.progressText
        }
    }

    data class TagTimeSpentViewModel(
        override val id: String,
        val name: String,
        val icon: IIcon,
        @ColorRes val color: Int,
        val totalTime: String,
        val focusTime: String
    ) : RecyclerViewViewModel

    inner class TagTimeSpentAdapter :
        BaseRecyclerViewAdapter<TagTimeSpentViewModel>(R.layout.item_growth_tag) {

        override fun onBindViewModel(
            vm: TagTimeSpentViewModel,
            view: View,
            holder: SimpleViewHolder
        ) {
            view.tagName.text = vm.name
            view.tagIcon.backgroundTintList = ColorStateList.valueOf(colorRes(vm.color))
            view.tagIcon.setImageDrawable(smallListItemIcon(vm.icon))

            view.tagTimeSpent.text = "${vm.totalTime} ("

            val focusTimeSpannable = SpannableString("${vm.focusTime} )")
            focusTimeSpannable.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                vm.focusTime.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            view.tagFocusTimeSpent.text = focusTimeSpannable
        }
    }

    private val GrowthViewState.challengeViewModels
        get() = challengeProgress.map {
            val durationText = DurationFormatter.formatShort(activity!!, it.timeSpent.intValue)
            ChallengeViewModel(
                id = it.challengeId,
                name = it.name,
                color = it.color.androidColor.color500,
                progress = it.progressPercent,
                progressText = "${it.completeQuestCount}/${it.totalQuestCount} done\n$durationText"
            )
        }

    private val GrowthViewState.tagTimeSpentViewModels
        get() = tagTimeSpent.map {
            TagTimeSpentViewModel(
                id = it.tagId,
                name = it.name,
                icon = it.icon?.androidIcon?.icon ?: MaterialDesignIconic.Icon.gmi_label,
                color = it.color.androidColor.color500,
                totalTime = DurationFormatter.formatNarrow(it.timeSpent.intValue),
                focusTime = DurationFormatter.formatNarrow(it.focusTime.intValue)
            )
        }

    private val GrowthViewState.tagTimeSpentForChart
        get() = tagTimeSpent.filter { it.timeSpentPercent >= 10 }


    companion object {
        private val TAB_INDEX_TO_ACTION = mapOf(
            0 to GrowthAction.ShowToday,
            1 to GrowthAction.ShowWeek,
            2 to GrowthAction.ShowMonth
        )
    }
}