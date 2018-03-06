package mypoli.android.challenge.show

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import kotlinx.android.synthetic.main.controller_challenge.view.*
import mypoli.android.MainActivity
import mypoli.android.R
import mypoli.android.common.ViewUtils
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.view.*
import mypoli.android.repeatingquest.show.RepeatingQuestViewController
import kotlin.math.roundToInt


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/05/2018.
 */
class ChallengeViewController(args: Bundle? = null) :
    ReduxViewController<ChallengeAction, ChallengeViewState, ChallengeReducer>(args) {

    override val reducer = ChallengeReducer

    private lateinit var challengeId: String

    private val yData = createYData()

    private fun createYData() =
        (0 until 30).map {
            getRandom(6f, 0f)
        }

    constructor(
        challengeId: String
    ) : this() {
        this.challengeId = challengeId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_challenge, container, false)
        setToolbar(view.toolbar)
        view.collapsingToolbarContainer.isTitleEnabled = false

        setupAppBar(view)

        setupHistoryChart(view.historyChart)

        return view
    }

    private fun setupAppBar(view: View) {
        view.appbar.addOnOffsetChangedListener(object :
            RepeatingQuestViewController.AppBarStateChangeListener() {
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
    }

    protected fun getRandom(range: Float, startsfrom: Float): Int {
        return ((Math.random() * range).toFloat() + startsfrom).toInt()
    }

    private fun setupHistoryChart(chart: LineChart) {
        with(chart) {
            description = null
            setTouchEnabled(false)
            setPinchZoom(false)
            extraBottomOffset = 20f
            extraTopOffset = 20f

            setDrawGridBackground(false)
//            setDrawBarShadow(true)
//            setDrawValueAboveBar(false)

            axisRight.axisMinimum = 0f
            axisRight.axisMaximum = 100f
            axisRight.spaceTop = 0f
            axisRight.setValueFormatter { value, axis -> "${value.toInt()}%" }

            axisLeft.isEnabled = false

            xAxis.setLabelCount(4, true)
            xAxis.yOffset = ViewUtils.dpToPx(4f, activity!!)

            val values = mapOf<Int, String>(
                0 to "10 Feb",
                10 to "17 Feb",
                19 to "24 Feb",
                29 to "03 Mar"
            )
            xAxis.setValueFormatter { value, _ ->
                values[value.roundToInt()]!!.toString()
            }

            legend.isEnabled = false

//            val data = CombinedData()
////            data.setData(createBarData())
//            data.setData(createLineData())

            data = createLineData()
            invalidate()
            animateX(1400, Easing.EasingOption.EaseInOutQuart)

        }

    }

//    private fun createBarData(): BarData {
//        val entries = ArrayList<BarEntry>()
//        //            val entries2 = ArrayList<BarEntry>()
//
//        yData.forEachIndexed { index, y ->
//            entries.add(BarEntry(index.toFloat(), y.toFloat()))
//        }
//
////        for (index in 0 until 30) {
////            entries.add(BarEntry(index.toFloat(), getRandom(3f, 0f).toFloat()))
////
////            // stacked
////            //                entries2.add(BarEntry(0f, floatArrayOf(getRandom(13f, 12f), getRandom(13f, 12f))))
////        }
//
//        //            val set1 = BarDataSet(entries1, "Bar 1")
//        //            set1.color = Color.rgb(60, 220, 78)
//        //            set1.valueTextColor = Color.rgb(60, 220, 78)
//        //            set1.valueTextSize = 10f
//        //            set1.axisDependency = YAxis.AxisDependency.LEFT
//        //
//        //            val set2 = BarDataSet(entries2, "")
//        //            set2.stackLabels = arrayOf("Stack 1", "Stack 2")
//        //            set2.setColors(*intArrayOf(Color.rgb(61, 165, 255), Color.rgb(23, 197, 255)))
//        //            set2.valueTextColor = Color.rgb(61, 165, 255)
//        //            set2.valueTextSize = 10f
//        //            set2.axisDependency = YAxis.AxisDependency.LEFT
//
//        val groupSpace = 0.06f
//        val barSpace = 0.02f // x2 dataset
//        val barWidth = 0.45f // x2 dataset
//        // (0.45 + 0.02) * 2 + 0.06 = 1.00 -> interval per "group"
//
//        val dataSet = BarDataSet(entries, "")
//
//        dataSet.color = colorRes(R.color.md_green_500)
//        dataSet.barShadowColor = Color.TRANSPARENT
//        dataSet.valueTextColor = Color.WHITE
//
//        val d = BarData(dataSet)
//        d.barWidth = barWidth
//        // make this BarData object grouped
//        //            d.groupBars(0f, groupSpace, barSpace) // start at x = 0
//        return d
//    }

    private fun createLineData(): LineData {

        val entries = ArrayList<Entry>()

        var sum = 25f
        yData.forEachIndexed { index, y ->
            entries.add(Entry(index.toFloat(), sum))
            sum += y
        }

//        var sum = 0f
//        for (index in 0 until 30) {
//            val newVal = getRandom(2f, 0f)
//            sum += newVal
//            entries.add(Entry(index.toFloat(), sum))
//        }

        val set = LineDataSet(entries, "Line DataSet")
//        set.color = Color.rgb(240, 238, 70)
        set.color = attrData(R.attr.colorAccent)
        set.lineWidth = ViewUtils.dpToPx(1f, activity!!)
        set.setDrawCircles(false)
        set.setDrawFilled(true)
        set.fillColor = attrData(R.attr.colorAccent)
        set.fillAlpha = 160
//        set.setCircleColor(Color.rgb(240, 238, 70))
//        set.circleRadius = 5f
//        set.fillColor = Color.rgb(240, 238, 70)
        set.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        set.setDrawValues(false)
//        set.valueTextSize = 10f
//        set.valueTextColor = Color.rgb(240, 238, 70)

        set.axisDependency = YAxis.AxisDependency.RIGHT

        val d = LineData()
        d.addDataSet(set)

        return d
    }

    override fun onCreateLoadAction() = ChallengeAction.Load(challengeId)

    override fun onAttach(view: View) {
        super.onAttach(view)
        showBackButton()
    }

    override fun onDetach(view: View) {
        (activity as MainActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        super.onDetach(view)
    }

    override fun render(state: ChallengeViewState, view: View) =
        when (state) {
            is ChallengeViewState.Changed -> {
                colorLayout(state, view)

                renderName(state.name, view)

                view.progress.progress = state.completedCount
                view.progress.secondaryProgress = state.completedCount
                view.progress.max = state.totalCount

                view.progressText.text = state.progressText

                view.endDate.setCompoundDrawablesWithIntrinsicBounds(
                    IconicsDrawable(view.context)
                        .icon(MaterialDesignIconic.Icon.gmi_hourglass_alt)
                        .colorRes(R.color.md_white)
                        .sizeDp(24),
                    null, null, null
                )
            }
            else -> {
            }
        }

    private fun colorLayout(
        state: ChallengeViewState.Changed,
        view: View
    ) {
        view.appbar.setBackgroundColor(colorRes(state.color500))
        view.toolbar.setBackgroundColor(colorRes(state.color500))
        view.collapsingToolbarContainer.setContentScrimColor(colorRes(state.color500))
        activity?.window?.navigationBarColor = colorRes(state.color500)
        activity?.window?.statusBarColor = colorRes(state.color700)
    }

    private fun renderName(
        name: String,
        view: View
    ) {
        toolbarTitle = name
        view.name.text = name
    }

    private val ChallengeViewState.Changed.color500
        get() = color.androidColor.color500

    private val ChallengeViewState.Changed.color700
        get() = color.androidColor.color700

    private val ChallengeViewState.Changed.progressText
        get() = "$completedCount of $totalCount ($progressPercent%) done"

}