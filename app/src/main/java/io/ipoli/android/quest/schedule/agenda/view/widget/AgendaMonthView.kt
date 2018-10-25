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


    private val whiteTextPaint: Paint
    private val blackTextPaint: Paint
    private val darkTextPaint: Paint
    private val lightTextPaint: Paint
    private val currentDayTextPaint: Paint

    private val colorPaints: Map<Color, Paint>
    private val colorStrokePaints: Map<Color, Paint>
    private val lightColorStrokePaints: Map<Color, Paint>

    init {

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
    }

    override fun onDrawText(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {
        val baselineY = mTextBaseLine + y
        val cx = (x + mItemWidth / 2).toFloat()

        if (isSelected) {
            canvas.drawText(
                calendar.day.toString(),
                cx,
                baselineY,
                mSelectTextPaint
            )
        } else if (hasScheme) {
            canvas.drawText(
                calendar.day.toString(),
                cx,
                baselineY,
                if (calendar.isCurrentDay)
                    mCurDayTextPaint
                else if (calendar.isCurrentMonth) mSchemeTextPaint else mOtherMonthTextPaint
            )

        } else {
            canvas.drawText(
                calendar.day.toString(), cx, baselineY,
                if (calendar.isCurrentDay)
                    mCurDayTextPaint
                else if (calendar.isCurrentMonth) mCurMonthTextPaint else mOtherMonthTextPaint
            )
        }
    }

}