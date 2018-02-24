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
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.TemporalAdjusters

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
    private val nonePaint = Paint()
    private val dayPaint = Paint()

    private val dayTextBounds = Rect()

    private val today = LocalDate.of(2018, Month.FEBRUARY, 28)

    private val rowData: List<RowData>

    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM")

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

        nonePaint.color = Color.GRAY
        nonePaint.isAntiAlias = true

        dayPaint.color = Color.BLACK
        dayPaint.isAntiAlias = true
        dayPaint.textAlign = Paint.Align.CENTER
        dayPaint.textSize = ViewUtils.spToPx(18, context).toFloat()

        rowData = createRowData()
    }

    private fun createRowData(): List<RowData> {

        val data = mutableListOf<RowData>()

        val nextWeekFirst =
            today.plusDays(7).with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek))

        val lastOfMonth = today.minusWeeks(1).with(TemporalAdjusters.lastDayOfMonth())

        val shouldSplitBottom = lastOfMonth.isBefore(nextWeekFirst)

        val firstWeekLast =
            today.minusWeeks(3).with(DateUtils.lastDayOfWeek)

        val firstOfMonth = today.with(TemporalAdjusters.firstDayOfMonth())

        val shouldSplitTop = firstOfMonth.isAfter(firstWeekLast)

        require(
            !(shouldSplitTop && shouldSplitBottom),
            { "Should not be able to split top AND bottom" })

        if (shouldSplitTop) {
            val firstWeekFirst =
                firstWeekLast.with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek))

            val firstWeekMonthLast = firstWeekFirst.with(TemporalAdjusters.lastDayOfMonth())

            var thisWeekStart = firstWeekFirst


            while (true) {
                val thisWeekEnd = thisWeekStart.plusWeeks(1)

                if (thisWeekStart.monthValue != thisWeekEnd.monthValue) {
                    data.add(
                        RowData.CellRow(
                            createWeekWithNoneCellsAtEnd(
                                firstWeekMonthLast
                            )
                        )
                    )
                    break
                } else {
                    data.add(RowData.CellRow(createCellsForWeek(thisWeekStart)))
                    thisWeekStart = thisWeekStart.plusWeeks(1)
                }
            }

            data.add(RowData.MonthRow(YearMonth.of(today.year, today.month)))

            if (firstOfMonth.dayOfWeek != DateUtils.firstDayOfWeek) {
                data.add(RowData.CellRow(createWeekWithNoneCellsAtStart(firstOfMonth)))
            }

            val fullWeeksToAdd = 7 - data.size

            val firstWeekStart =
                firstOfMonth.with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek))
                    .plusWeeks(1)

            data.addAll(
                createCellsForWeeks(
                    weeksToAdd = fullWeeksToAdd,
                    firstWeekStart = firstWeekStart
                )
            )

        } else if (shouldSplitBottom) {

            val firstWeekStart =
                today.minusWeeks(3).with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek))
            data.addAll(createCellsForWeeks(weeksToAdd = 3, firstWeekStart = firstWeekStart))

            data.add(RowData.CellRow(createWeekWithNoneCellsAtEnd(lastOfMonth)))
            val nextMonthFirst = lastOfMonth.plusDays(1)
            data.add(RowData.MonthRow(YearMonth.of(nextMonthFirst.year, nextMonthFirst.month)))
            data.add(RowData.CellRow(createWeekWithNoneCellsAtStart(nextMonthFirst)))

            if (nextMonthFirst.dayOfWeek != DateUtils.firstDayOfWeek) {
                val lastWeekStart = nextMonthFirst.plusWeeks(1).with(
                    TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek)
                )
                data.add(RowData.CellRow(createCellsForWeek(lastWeekStart)))
            }
        } else {
            data.add(RowData.MonthRow(YearMonth.of(today.year, today.month)))

            data.add(RowData.CellRow(createWeekWithNoneCellsAtStart(firstOfMonth)))

            data.addAll(
                createCellsForWeeks(
                    weeksToAdd = 3,
                    firstWeekStart = firstOfMonth.plusWeeks(1).with(
                        TemporalAdjusters.previousOrSame(
                            DateUtils.firstDayOfWeek
                        )
                    )
                )
            )

            data.add(RowData.CellRow(createWeekWithNoneCellsAtEnd(lastOfMonth)))
        }

        return data
    }

    private fun createCellsForWeeks(
        weeksToAdd: Int,
        firstWeekStart: LocalDate
    ): List<RowData> {

        val data = mutableListOf<RowData>()

        var currentWeekStart = firstWeekStart
        (1..weeksToAdd).forEach {
            data.add(RowData.CellRow(createCellsForWeek(currentWeekStart)))
            currentWeekStart = currentWeekStart.plusWeeks(1)
        }

        return data
    }

    private fun createWeekWithNoneCellsAtStart(firstOfMonth: LocalDate): List<Cell> {
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
        return cells
    }

    private fun createWeekWithNoneCellsAtEnd(
        lastOfMonth: LocalDate
    ): List<Cell> {

        val thisWeekStart =
            lastOfMonth.with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek))

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

        rowData.forEachIndexed { i, rowData ->

            val y = (i + 1) * rowPadding + ((i + 1) * circleRadius * 2)

            when (rowData) {
                is RowData.MonthRow -> {

                    val monthText = monthFormatter.format(rowData.month)
                    dayPaint.getTextBounds(monthText, 0, monthText.length, dayTextBounds)

                    canvas.drawText(
                        monthText,
                        (totalWidth / 2).toFloat(),
                        y - dayTextBounds.exactCenterY(),
                        dayPaint
                    )
                }

                is RowData.CellRow -> {


                    rowData.cells.forEachIndexed { j, cell ->

                        val paint = when (cell.type) {
                            Cell.CellType.COMPLETED -> completedPaint
                            Cell.CellType.NONE -> nonePaint
                            else -> skippedPaint
                        }

                        canvas.drawCircle(cxs[j].toFloat(), y, circleRadius, paint)


                    }
                }
            }
        }

    }
}