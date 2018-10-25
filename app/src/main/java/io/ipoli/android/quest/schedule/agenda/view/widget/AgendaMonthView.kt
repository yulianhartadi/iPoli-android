package io.ipoli.android.quest.schedule.agenda.view.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.MonthView
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.common.view.attrData
import io.ipoli.android.quest.Color


@Suppress("unused")
class AgendaMonthView(context: Context) : MonthView(context) {

    private val selectedBackgroundPaint = Paint()
    private val currentBackgroundPaint = Paint()

    private val currentDayTextPaint: Paint

    private val colorPaints: Map<Color, Paint>

    private var radius = 0f

    init {

        selectedBackgroundPaint.color = context.attrData(R.attr.colorAccent)

        currentBackgroundPaint.color = context.attrData(R.attr.colorPrimary)

        currentDayTextPaint = createTextPaint(context, R.color.md_light_text_50, 14)
        currentDayTextPaint.color = context.attrData(R.attr.colorAccent)
        currentDayTextPaint.style = Paint.Style.FILL

        colorPaints = Color.values().map {
            val p = Paint()
            p.initWithColor(AndroidColor.valueOf(it.name).color500)
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

    override fun onPreviewHook() {
        radius = (Math.min(mItemWidth, mItemHeight) / 11 * 5.2).toFloat()
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
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius, selectedBackgroundPaint)
        return false
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
        val data = calendar.scheme

        val parts = data.split(",")
    }

    override fun onDrawText(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {

        val cx = (x + mItemWidth / 2).toFloat()

        if (calendar.isCurrentDay) {
            val cy = (y + mItemHeight / 2).toFloat()
            canvas.drawCircle(
                cx,
                cy,
                radius,
                currentBackgroundPaint
            )
        }

        val baselineY = mTextBaseLine + y

        canvas.drawText(
            calendar.day.toString(),
            cx,
            baselineY,
            when {
                isSelected -> mCurDayTextPaint
                calendar.isCurrentDay -> mCurDayTextPaint
                calendar.isCurrentMonth -> mSchemeTextPaint
                else -> mOtherMonthTextPaint
            }
        )
    }

}