package io.ipoli.android.habit.show

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.MonthView
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.common.view.attrData
import io.ipoli.android.common.view.colorRes
import io.ipoli.android.habit.usecase.CreateHabitHistoryItemsUseCase.HabitHistoryItem.State
import io.ipoli.android.quest.Color


@Suppress("unused")
class HabitProgressMonthView(context: Context) : MonthView(context) {


    private val failedPaint = Paint()
    private val failedProgressPaint = Paint()
    private val failedRemainingPaint = Paint()
    private val whiteTextPaint: Paint
    private val blackTextPaint: Paint
    private val darkTextPaint: Paint
    private val lightTextPaint: Paint
    private val currentDayTextPaint: Paint

    private val colorPaints: Map<Color, Paint>
    private val colorStrokePaints: Map<Color, Paint>
    private val lightColorStrokePaints: Map<Color, Paint>

    init {

        failedPaint.color = context.colorRes(R.color.md_red_100)

        failedProgressPaint.color = context.colorRes(R.color.md_red_700)
        failedProgressPaint.style = Paint.Style.STROKE
        failedProgressPaint.strokeWidth = ViewUtils.dpToPx(2.5f, context)
        failedProgressPaint.isAntiAlias = true

        failedRemainingPaint.color = context.colorRes(R.color.md_red_200)
        failedRemainingPaint.style = Paint.Style.STROKE
        failedRemainingPaint.strokeWidth = ViewUtils.dpToPx(2.5f, context)
        failedRemainingPaint.isAntiAlias = true

        whiteTextPaint = createTextPaint(context, R.color.md_light_text_100, 14)
        lightTextPaint = createTextPaint(context, R.color.md_light_text_50, 14)
        blackTextPaint = createTextPaint(context, R.color.md_dark_text_100, 14)
        darkTextPaint = createTextPaint(context, R.color.md_dark_text_38, 14)

        currentDayTextPaint = createTextPaint(context, R.color.md_light_text_50, 14)
        currentDayTextPaint.color = context.attrData(R.attr.colorAccent)
        currentDayTextPaint.style = Paint.Style.FILL

        colorPaints = Color.values().map {
            val p = Paint()
            p.initWithColor(AndroidColor.valueOf(it.name).color500)
            it to p
        }.toMap()

        colorStrokePaints = Color.values().map {
            val p = Paint()
            p.initWithColor(AndroidColor.valueOf(it.name).color500)
            p.strokeWidth = ViewUtils.dpToPx(2.5f, context)
            p.style = Paint.Style.STROKE
            it to p
        }.toMap()

        lightColorStrokePaints = Color.values().map {
            val p = Paint()
            p.initWithColor(AndroidColor.valueOf(it.name).color100)
            p.strokeWidth = ViewUtils.dpToPx(2.5f, context)
            p.style = Paint.Style.STROKE
            it to p
        }.toMap()
    }

    private fun createTextPaint(context: Context, @ColorRes color: Int, textSize: Int): Paint {
        val paint = Paint()
        paint.color = ContextCompat.getColor(context, color)
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = ViewUtils.spToPx(textSize, context).toFloat()
        paint.isFakeBoldText = true

        return paint
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
        return true
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
        val data = calendar.scheme

        val parts = data.split(",")
        val state = State.valueOf(parts[0])
        val color = Color.valueOf(parts[1])
        val shouldBeDone = parts[2].toBoolean()
        val isPreviousCompleted = parts[3].toBoolean()
        val isNextCompleted = parts[4].toBoolean()
        val timesADay = parts[5].toInt()
        val completedCount = parts[6].toInt()

        val strokePaint = colorStrokePaints[color]

        val radius = (Math.min(mItemHeight, mItemWidth) - ViewUtils.dpToPx(8f, context)) / 2
        val halfFullWidth = mItemWidth / 2
        val halfFullHeight = mItemHeight / 2

        val centerX = x.toFloat() + halfFullWidth
        val centerY = y.toFloat() + halfFullHeight

        when (state) {
            State.COMPLETED -> {
                if (isPreviousCompleted) {
                    canvas.drawLine(
                        x.toFloat(),
                        centerY,
                        centerX,
                        centerY,
                        strokePaint
                    )
                }
                if (isNextCompleted) {
                    canvas.drawLine(
                        centerX,
                        centerY,
                        (x + mItemWidth).toFloat(),
                        centerY,
                        strokePaint
                    )
                }

                canvas.drawCircle(
                    centerX,
                    centerY,
                    radius,
                    colorPaints[color]
                )
            }
            State.NOT_COMPLETED_TODAY -> {

                if (timesADay > 1) {

                    val gap = 15f
                    val ark = 360 / timesADay - gap

                    val borderOffset = ViewUtils.dpToPx(2.5f, context) / 2
                    val r = RectF(
                        centerX - radius - borderOffset,
                        centerY - radius - borderOffset,
                        centerX + radius + borderOffset,
                        centerY + radius + borderOffset
                    )

                    val offset = gap / 2f - 90f

                    (0..completedCount).forEach {
                        canvas.drawArc(r, offset + (it * (ark + gap)), ark, false, strokePaint)
                    }

                    (completedCount until timesADay).forEach {
                        canvas.drawArc(
                            r,
                            offset + (it * (ark + gap)),
                            ark,
                            false,
                            lightColorStrokePaints[color]
                        )
                    }

                } else {
                    canvas.drawCircle(
                        centerX,
                        centerY,
                        radius,
                        strokePaint
                    )
                }


            }
            State.FAILED -> {
                canvas.drawCircle(
                    centerX,
                    centerY,
                    radius,
                    failedPaint
                )

                if (timesADay > 1) {

                    val gap = 15f
                    val ark = 360 / timesADay - gap

                    val borderOffset = ViewUtils.dpToPx(2.5f, context) / 2
                    val r = RectF(
                        centerX - radius - borderOffset,
                        centerY - radius - borderOffset,
                        centerX + radius + borderOffset,
                        centerY + radius + borderOffset
                    )

                    val offset = gap / 2f - 90f

                    (0..completedCount).forEach {
                        canvas.drawArc(
                            r,
                            offset + (it * (ark + gap)),
                            ark,
                            false,
                            failedProgressPaint
                        )
                    }

                    (completedCount until timesADay).forEach {
                        canvas.drawArc(
                            r,
                            offset + (it * (ark + gap)),
                            ark,
                            false,
                            failedRemainingPaint
                        )
                    }

                }
            }

            State.CONNECTED -> {
                if (isPreviousCompleted) {
                    canvas.drawLine(
                        x.toFloat(),
                        centerY,
                        centerX - radius,
                        centerY,
                        strokePaint
                    )
                }
                if (isNextCompleted) {
                    canvas.drawLine(
                        centerX + radius,
                        centerY,
                        (x + mItemWidth).toFloat(),
                        centerY,
                        strokePaint
                    )
                }
                canvas.drawCircle(
                    centerX,
                    centerY,
                    radius,
                    strokePaint
                )
            }

            else -> {
            }
        }

        canvas.drawText(
            calendar.day.toString(),
            (x + halfFullWidth).toFloat(),
            mTextBaseLine + y,
            when {
                calendar.isCurrentDay && (state == State.NOT_COMPLETED_TODAY || state == State.FAILED) -> currentDayTextPaint
                calendar.isCurrentDay && state == State.COMPLETED -> blackTextPaint
                state == State.FAILED -> darkTextPaint
                shouldBeDone -> if (state == State.COMPLETED) whiteTextPaint else mCurDayTextPaint
                else -> if (state == State.COMPLETED) lightTextPaint else mOtherMonthTextPaint
            }
        )
    }

    override fun onDrawText(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {
    }

}