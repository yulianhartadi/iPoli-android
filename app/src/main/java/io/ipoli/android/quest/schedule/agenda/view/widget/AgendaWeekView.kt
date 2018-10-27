package io.ipoli.android.quest.schedule.agenda.view.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.WeekView
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.common.view.attrData
import org.json.JSONArray
import org.json.JSONObject

@Suppress("unused")
class AgendaWeekView(context: Context) : WeekView(context) {

    companion object {
        const val MINUTES = 16 * 60
        const val EVENT_HEIGHT_DP = 5
        const val GAP_DP = 2
    }

    private val selectedDayPaint = Paint()
    private val currentDayPaint = Paint()
    private val dividerPaint = Paint()
    private val itemPaint = Paint()

    init {

        selectedDayPaint.style = Paint.Style.FILL
        selectedDayPaint.isAntiAlias = true
        selectedDayPaint.color = context.attrData(R.attr.colorAccent)

        currentDayPaint.style = Paint.Style.FILL
        currentDayPaint.isAntiAlias = true
        currentDayPaint.color = context.attrData(R.attr.colorPrimary)

        dividerPaint.style = Paint.Style.STROKE
        dividerPaint.strokeWidth = ViewUtils.dpToPx(1f, context)
        dividerPaint.color = context.attrData(android.R.attr.listDivider)

        itemPaint.isAntiAlias = true
        itemPaint.style = Paint.Style.STROKE
        itemPaint.strokeWidth = ViewUtils.dpToPx(EVENT_HEIGHT_DP.toFloat(), context)
    }

    private fun Paint.initWithColor(@ColorRes color: Int) {
        isAntiAlias = true
        style = Paint.Style.FILL
        this.color = ContextCompat.getColor(context, color)
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

        val data = JSONObject(calendar.scheme)
        val weekIndicators = data.getJSONArray("weekIndicators")
        if(weekIndicators.length() == 0) {
            return
        }
        val items = WeekViewItem.createItemsFromJson(weekIndicators, context)

        val dayBounds = dayBounds(calendar)
        val gap = ViewUtils.dpToPx(GAP_DP.toFloat(), context)
        val topY =
            2 * Math.max(dayBounds.height(), dayBounds.width()) + ViewUtils.dpToPx(8f, context)

        val minuteWidth = mItemWidth / MINUTES.toFloat()
        val eventHeight = ViewUtils.dpToPx(EVENT_HEIGHT_DP.toFloat(), context)

        items.forEachIndexed { index, item ->
            itemPaint.color = item.color
            val iy = topY + index * (eventHeight + gap)
            val startX = x.toFloat() + item.startMinute * minuteWidth

            canvas.drawLine(
                startX,
                iy,
                startX + item.duration * minuteWidth,
                iy,
                itemPaint
            )

        }

    }


    override fun onDrawText(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {

        val textPaint = when {
            calendar.isCurrentDay -> mCurDayTextPaint
            calendar.isCurrentMonth -> mSchemeTextPaint
            else -> mOtherMonthTextPaint
        }
        val dayBounds = dayBounds(calendar)

        canvas.drawLine(
            (x + mItemWidth).toFloat(),
            0f,
            (x + mItemWidth).toFloat(),
            mItemHeight.toFloat(),
            dividerPaint
        )

        val radius = Math.max(dayBounds.height(), dayBounds.width())
        val baselineY = mTextBaseLine - mItemHeight / 2 + radius
        val cx = x + mItemWidth / 2

        if (calendar.isCurrentDay) {
            canvas.drawCircle(
                cx.toFloat(),
                baselineY - dayBounds.height() / 2,
                radius.toFloat(),
                currentDayPaint
            )
        } else if(isSelected) {
            canvas.drawCircle(
                cx.toFloat(),
                baselineY - dayBounds.height() / 2,
                radius.toFloat(),
                selectedDayPaint
            )
        }

        canvas.drawText(
            calendar.day.toString(),
            cx.toFloat(),
            baselineY,
            textPaint
        )
    }

    private fun dayBounds(calendar: Calendar): Rect {
        val textBounds = Rect()
        val textPaint = when {
            calendar.isCurrentDay -> mCurDayTextPaint
            calendar.isCurrentMonth -> mSchemeTextPaint
            else -> mOtherMonthTextPaint
        }

        val day = calendar.day.toString()
        textPaint.getTextBounds(day, 0, day.length, textBounds)
        return textBounds
    }

    data class WeekViewItem(
        @ColorInt val color: Int,
        val duration: Int,
        val startMinute: Int
    ) {
        companion object {

            fun createItemsFromJson(data: JSONArray, context: Context): List<WeekViewItem> {
                if (data.length() == 0) {
                    return emptyList()
                }
                return (0 until data.length()).map {
                    val o = data.getJSONObject(it)
                    val type = o.getString("type")
                    WeekViewItem(
                        color = if (type == "quest") {
                            val c = AndroidColor.valueOf(o.getString("color"))
                            ContextCompat.getColor(context, c.color500)
                        } else o.getString("color").toInt(),
                        duration = o.getInt("duration"),
                        startMinute = o.getInt("start")
                    )
                }
            }
        }
    }

}