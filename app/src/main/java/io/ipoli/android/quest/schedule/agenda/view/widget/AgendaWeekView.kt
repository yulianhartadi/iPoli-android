package io.ipoli.android.quest.schedule.agenda.view.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.WeekView
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.view.attrData
import io.ipoli.android.quest.schedule.summary.view.widget.ScheduleItem
import org.json.JSONArray

@Suppress("unused")
class AgendaWeekView(context: Context) : WeekView(context) {

    private val selectedBorderPaint = Paint()
    private val currentDayPaint = Paint()

    private val whiteTextPaint = TextPaint()

    private val dividerPaint = Paint()

    private val itemPaint = Paint()

    init {

        whiteTextPaint.isFakeBoldText = true
        whiteTextPaint.isAntiAlias = true
        whiteTextPaint.color = ContextCompat.getColor(context, R.color.md_white)
        whiteTextPaint.textSize = ViewUtils.spToPx(12, context).toFloat()

        selectedBorderPaint.style = Paint.Style.STROKE
        selectedBorderPaint.strokeWidth = ViewUtils.dpToPx(1.5f, context)
        selectedBorderPaint.color = context.attrData(R.attr.colorAccent)

        currentDayPaint.style = Paint.Style.FILL
        currentDayPaint.isAntiAlias = true
        currentDayPaint.color = context.attrData(R.attr.colorPrimary)

        dividerPaint.style = Paint.Style.STROKE
        dividerPaint.strokeWidth = ViewUtils.dpToPx(1f, context)
        dividerPaint.color = context.attrData(android.R.attr.listDivider)

        itemPaint.isAntiAlias = true
        itemPaint.style = Paint.Style.FILL
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

    override fun onDrawSelected(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        hasScheme: Boolean
    ): Boolean {

        val widthOffset = ViewUtils.dpToPx(1f, context)
        val heightOffset = ViewUtils.dpToPx(0.5f, context)

//        SelectionRectangle(
//            left = x.toFloat() + widthOffset,
//            top = mItemHeight - heightOffset,
//            right = (x + mItemWidth).toFloat() - widthOffset,
//            bottom = heightOffset
//        ).draw(canvas, selectedBorderPaint)

        return true
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int) {

        val data = JSONArray(calendar.scheme)

        val items = ScheduleItem.createItemsFromJson(data, context)


    }


    override fun onDrawText(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {

        val textBounds = Rect()
        val textPaint = when {
            calendar.isCurrentDay -> mCurDayTextPaint
            calendar.isCurrentMonth -> mSchemeTextPaint
            else -> mOtherMonthTextPaint
        }

        val day = calendar.day.toString()
        textPaint.getTextBounds(day, 0, day.length, textBounds)


        val radius = Math.max(textBounds.height(), textBounds.width())
        val baselineY = mTextBaseLine - mItemHeight / 2 + radius
        val cx = x + mItemWidth / 2

        if (calendar.isCurrentDay) {


            canvas.drawCircle(
                cx.toFloat(),
                baselineY - textBounds.height() / 2,
                radius.toFloat(),
                currentDayPaint
            )
        }

        canvas.drawText(
            day,
            cx.toFloat(),
            baselineY,
            textPaint
        )

        canvas.drawLine(
            (x + mItemWidth).toFloat(),
            0f,
            (x + mItemWidth).toFloat(),
            mItemHeight.toFloat(),
            dividerPaint
        )

    }

}