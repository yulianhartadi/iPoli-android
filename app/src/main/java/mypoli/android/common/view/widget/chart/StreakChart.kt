package mypoli.android.common.view.widget.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import mypoli.android.common.ViewUtils
import mypoli.android.common.datetime.DateUtils
import mypoli.android.common.datetime.daysUntil
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.YearMonth
import org.threeten.bp.temporal.TemporalAdjusters
import timber.log.Timber

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/23/2018.
 */
class StreakChart @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val completedPaint = Paint()
    private val skippedPaint = Paint()
    private val failedPaint = Paint()
    private val dayPaint = Paint()

    private val dayTextBounds = Rect()

    private val today = LocalDate.of(2018, Month.FEBRUARY, 15)

    private val cellTypes: List<RowData>


    data class Cell(val dayOfMonth: Int, val type: CellType) {
        enum class CellType {
            COMPLETED, FAILED, SKIPPED, TODAY_SKIP, TODAY_DO, FUTURE, NONE
        }
    }


    sealed class RowData {
        data class MonthRow(val month: YearMonth) : RowData()
        data class CellRow(val cells: List<Cell>) : RowData()
    }


    init {
        completedPaint.color = Color.GREEN
        completedPaint.isAntiAlias = true

        skippedPaint.color = Color.BLUE
        skippedPaint.isAntiAlias = true

        failedPaint.color = Color.RED
        failedPaint.isAntiAlias = true

        dayPaint.color = Color.WHITE
        dayPaint.isAntiAlias = true
        dayPaint.textAlign = Paint.Align.CENTER
        dayPaint.textSize = ViewUtils.spToPx(12, context).toFloat()

        cellTypes = findCellTypes()

        Timber.d("AAA $cellTypes")
    }

    private fun findCellTypes(): List<RowData> {

        val data = mutableListOf<RowData>()

        val nextWeekFirst =
            today.plusDays(7).with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek))

        val lastOfMonth = today.with(TemporalAdjusters.lastDayOfMonth())

        val shouldSplitBottom = lastOfMonth.isBefore(nextWeekFirst)

        val firstWeekLast =
            today.minusWeeks(3).with(DateUtils.lastDayOfWeek)

        val firstOfMonth = today.with(TemporalAdjusters.firstDayOfMonth())

        val shouldSplitTop = firstOfMonth.isAfter(firstWeekLast)

        if (shouldSplitTop) {
            val firstWeekFirst =
                firstWeekLast.with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek))

            val firstWeekMonthLast = firstWeekFirst.with(TemporalAdjusters.lastDayOfMonth())

            var thisWeekStart = firstWeekFirst


            while (true) {
                val thisWeekEnd = thisWeekStart.plusDays(7)

                if (thisWeekStart.monthValue != thisWeekEnd.monthValue) {
                    data.add(RowData.CellRow(createWeekWithNoneCellsAtEnd(thisWeekStart, firstWeekMonthLast)))
                    break
                } else {
                    val cells = mutableListOf<Cell>()

                    val startDayOfMonth = thisWeekStart.dayOfMonth

                    (0.until(7)).forEach {
                        cells.add(Cell(startDayOfMonth + it, Cell.CellType.COMPLETED))
                    }

                    data.add(RowData.CellRow(cells))
                    thisWeekStart = thisWeekStart.plusDays(7)
                }
            }

            data.add(RowData.MonthRow(YearMonth.of(today.year, today.month)))

            val firstOfWeek =
                firstOfMonth.with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek))

            val noneCellCount = firstOfWeek.daysUntil(firstOfMonth).toInt()

            val cells = mutableListOf<Cell>()

            val firstOfWeekDayOfMonth = firstOfWeek.dayOfMonth

            (0 until noneCellCount).forEach {
                cells.add(Cell(firstOfWeekDayOfMonth + it, Cell.CellType.NONE))
            }

            val lastOfWeek = firstOfMonth.with(DateUtils.lastDayOfWeek)

            val cellCount = (firstOfMonth.daysUntil(lastOfWeek) + 1).toInt()

            (1..cellCount).forEach {
                cells.add(Cell(it, Cell.CellType.COMPLETED))
            }

            data.add(RowData.CellRow(cells))

            val fullWeeksToAdd = 6 - data.size

            var weekStart = firstOfWeek

            (1..fullWeeksToAdd).forEach {
                weekStart = weekStart.plusDays(7)
                data.add(RowData.CellRow(createCellsForWeek(weekStart)))
            }

            weekStart = weekStart.plusDays(7)

            val weekEnd = weekStart.with(DateUtils.lastDayOfWeek)

            if (weekStart.monthValue != weekEnd.monthValue) {
                // pad with none
                data.add(RowData.CellRow(createWeekWithNoneCellsAtEnd(weekStart, lastOfMonth)))
            } else {
                data.add(RowData.CellRow(createCellsForWeek(weekStart)))
            }
        }

        return data
    }

    private fun createWeekWithNoneCellsAtEnd(
        thisWeekStart: LocalDate,
        lastOfMonth: LocalDate
    ): List<Cell> {
        val daysBetweenEnd = (thisWeekStart.daysUntil(lastOfMonth) + 1).toInt()

        val cells = mutableListOf<Cell>()

        val startDayOfMonth = thisWeekStart.dayOfMonth

        (0 until daysBetweenEnd).forEach {
            cells.add(Cell(startDayOfMonth + it, Cell.CellType.COMPLETED))
        }

        val noneCellCount = 7 - daysBetweenEnd

        (1..noneCellCount).forEach {
            cells.add(Cell(it, Cell.CellType.NONE))
        }
        return cells
    }

    private fun createCellsForWeek(weekStart: LocalDate) =
        (0 until 7).map {
            Cell(weekStart.dayOfMonth + it, Cell.CellType.COMPLETED)
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val circleRadius = ViewUtils.dpToPx(16f, context)
        val rowPadding = ViewUtils.dpToPx(8f, context)

        val totalWidth = measuredWidth

        val cxs = (1..7).map { it * (totalWidth / 8) }
        val cys = (1..6).map { it * rowPadding + (it * circleRadius * 2) }

        cys.forEach { y ->
            cxs.forEach { x ->
                canvas.drawCircle(
                    x.toFloat(),
                    y,
                    circleRadius,
                    completedPaint
                )

                val text = "31"

                dayPaint.getTextBounds(text, 0, text.length, dayTextBounds);


                canvas.drawText(text, x.toFloat(), y - dayTextBounds.exactCenterY(), dayPaint)
            }
        }


    }
}