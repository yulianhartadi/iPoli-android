package io.ipoli.android.quest.schedule.summary.view.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.MonthView
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.common.view.attrData
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.schedule.summary.usecase.CreateScheduleSummaryUseCase
import io.ipoli.android.quest.schedule.summary.usecase.CreateScheduleSummaryUseCase.ScheduleSummaryItem.Fullness

class SelectionRectangle(
    private val left: Float,
    private val top: Float,
    private val right: Float,
    private val bottom: Float
) {

    fun draw(canvas: Canvas, paint: Paint) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            canvas.drawRect(left, bottom, right, top, paint)
        } else {
            canvas.drawRect(left, top, right, bottom, paint)
        }
    }
}

@Suppress("unused")
class ProgressMonthView(context: Context) : MonthView(context) {

    private val noFullnessPaint = Paint()
    private val lightFullnessPaint = Paint()
    private val mediumFullnessPaint = Paint()
    private val highFullnessPaint = Paint()

    private val selectedBorderPaint = Paint()
    private val currentDayBorderPaint = Paint()

    private val colorPaints: Map<Color, Paint>

    init {

        noFullnessPaint.initWithColor(android.R.color.transparent)
        lightFullnessPaint.initWithColor(R.color.md_red_50)
        mediumFullnessPaint.initWithColor(R.color.md_red_100)
        highFullnessPaint.initWithColor(R.color.md_red_200)

        selectedBorderPaint.style = Paint.Style.STROKE
        selectedBorderPaint.strokeWidth = ViewUtils.dpToPx(1.5f, context)
        selectedBorderPaint.color = context.attrData(R.attr.colorAccent)

        currentDayBorderPaint.style = Paint.Style.STROKE
        currentDayBorderPaint.strokeWidth = ViewUtils.dpToPx(1.5f, context)
        currentDayBorderPaint.color = context.attrData(R.attr.colorPrimary)

        colorPaints = Color.values().map {
            val p = Paint()
            p.initWithColor(AndroidColor.valueOf(it.name).color500)
            it to p
        }.toMap()
    }

    private fun Paint.initWithColor(@ColorRes color: Int) {
        isAntiAlias = true
        style = Paint.Style.FILL
        this.color = ContextCompat.getColor(context, color)
    }

    override fun onDrawSelected(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean
    ): Boolean {
        val widthOffset = ViewUtils.dpToPx(1f, context)
        val heightOffset = ViewUtils.dpToPx(0.5f, context)

        SelectionRectangle(
            left = x.toFloat() + widthOffset,
            top = y + mItemHeight - heightOffset,
            right = (x + mItemWidth).toFloat() - widthOffset,
            bottom = y.toFloat() + heightOffset
        ).draw(canvas, selectedBorderPaint)

        return true
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {

        val data = calendar.scheme

        val parts = data.split(",")

        val morningFullness =
            Fullness.valueOf(parts[0])
        val afternoonFullness =
            Fullness.valueOf(parts[1])
        val eveningFullness =
            Fullness.valueOf(parts[2])

        val schedulePaints = listOf(
            fullnessPaint(morningFullness),
            fullnessPaint(afternoonFullness),
            fullnessPaint(eveningFullness)
        )

        val fullHeight = mItemHeight - ViewUtils.dpToPx(16f, context)
        val morningStart = y.toFloat() + ViewUtils.dpToPx(4f, context)
        val morningEnd = morningStart + fullHeight / 3

        val afternoonStart = morningEnd
        val afternoonEnd = afternoonStart + fullHeight / 3

        val eveningStart = afternoonEnd
        val eveningEnd = eveningStart + fullHeight / 3

        val cellStart = x + ViewUtils.dpToPx(4f, context)
        val cellEnd = cellStart + mItemWidth - ViewUtils.dpToPx(8f, context)

        canvas.drawRect(
            cellStart,
            morningStart,
            cellEnd,
            morningEnd,
            schedulePaints.first()
        )

        canvas.drawRect(
            cellStart,
            afternoonStart,
            cellEnd,
            afternoonEnd,
            schedulePaints[1]
        )

        canvas.drawRect(
            cellStart,
            eveningStart,
            cellEnd,
            eveningEnd,
            schedulePaints[2]
        )

        val tagPaints = parts.subList(3, parts.size).map { colorPaints[Color.valueOf(it)]!! }

        drawIndicators(x.toFloat(), canvas, eveningEnd, Math.min(tagPaints.size, 4), tagPaints)
    }

    private fun fullnessPaint(fullness: CreateScheduleSummaryUseCase.ScheduleSummaryItem.Fullness) =
        when (fullness) {
            Fullness.NONE -> noFullnessPaint
            Fullness.LIGHT -> lightFullnessPaint
            Fullness.MEDIUM -> mediumFullnessPaint
            Fullness.HIGH -> highFullnessPaint
        }

    private fun drawIndicators(
        x: Float,
        canvas: Canvas,
        eveningEnd: Float,
        indicatorCount: Int,
        colors: List<Paint>
    ) {
        val circleDiameter = ViewUtils.dpToPx(6f, context)
        val circlePadding = ViewUtils.dpToPx(4f, context)

        val indicatorsWidth = indicatorCount * circleDiameter + (indicatorCount - 1) * circlePadding

        val indicatorStart = x + mItemWidth / 2 - indicatorsWidth / 2 + circleDiameter / 2

        val indicatorWidth = circleDiameter + circlePadding

        for (i in 0 until indicatorCount) {
            canvas.drawCircle(
                indicatorStart + indicatorWidth * i,
                eveningEnd + ViewUtils.dpToPx(6f, context),
                circleDiameter / 2,
                colors[i]
            )
        }
    }

    override fun onDrawText(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {

        val baselineY = mTextBaseLine + y - (mItemHeight / 8)
        val cx = x + mItemWidth / 2

        canvas.drawText(
            calendar.day.toString(),
            cx.toFloat(),
            baselineY,
            when {
                calendar.isCurrentDay -> mCurDayTextPaint
                calendar.isCurrentMonth -> mSchemeTextPaint
                else -> mOtherMonthTextPaint
            }
        )

        if (calendar.isCurrentDay) {
            val widthOffset = ViewUtils.dpToPx(1f, context)
            val heightOffset = ViewUtils.dpToPx(0.5f, context)

            SelectionRectangle(
                left = x.toFloat() + widthOffset,
                top = y + mItemHeight - heightOffset,
                right = (x + mItemWidth).toFloat() - widthOffset,
                bottom = y.toFloat() + heightOffset
            ).draw(canvas, currentDayBorderPaint)
        }
    }

}