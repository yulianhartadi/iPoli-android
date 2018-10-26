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
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.common.view.attrData
import io.ipoli.android.quest.Color
import org.json.JSONArray

@Suppress("unused")
class AgendaWeekView(context: Context) : WeekView(context) {

    companion object {
        const val MINUTES = 16 * 40
        const val EVENT_HEIGHT_DP = 5
        const val GAP_DP = 2
    }

    private val selectedBorderPaint = Paint()
    private val currentDayPaint = Paint()

    private val whiteTextPaint = TextPaint()

    private val dividerPaint = Paint()

    private val itemPaint = Paint()

    private val colorStrokePaints: Map<Color, Paint>

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

        colorStrokePaints = Color.values().map {
            val p = Paint()
            p.initWithColor(AndroidColor.valueOf(it.name).color500)
            p.strokeWidth = ViewUtils.dpToPx(EVENT_HEIGHT_DP.toFloat(), context)
            p.style = Paint.Style.STROKE
            it to p
        }.toMap()
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

        val data = JSONArray(calendar.scheme)
        val items = WeekViewItem.createItemsFromJson(data, context)

        canvas.drawLine(
            (x + mItemWidth).toFloat(),
            0f,
            (x + mItemWidth).toFloat(),
            mItemHeight.toFloat(),
            dividerPaint
        )

        val dayBounds = dayBounds(calendar)
        val gap = ViewUtils.dpToPx(GAP_DP.toFloat(), context)
        val topY = Math.max(dayBounds.height(), dayBounds.width()) * 2 + gap

        val minuteWidth = mItemWidth / MINUTES.toFloat()
        val eventHeight = ViewUtils.dpToPx(EVENT_HEIGHT_DP.toFloat(), context)

        items.forEachIndexed { index, item ->
            val iy = topY + index * (eventHeight + gap)
            val startX = x.toFloat() + item.startMinute * minuteWidth

            canvas.drawLine(
                startX,
                iy,
                startX + item.duration * minuteWidth,
                iy,
                colorStrokePaints[item.color]
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
        }

        canvas.drawText(
            calendar.day.toString(),
            cx.toFloat(),
            baselineY,
            textPaint
        )


//        val gap = ViewUtils.dpToPx(4f, context)
//        val questsTopY = radius * 2 + gap
//
//        if (calendar.isCurrentDay) {
//            canvas.drawLine(
//                x.toFloat(),
//                questsTopY,
//                (x + mItemWidth).toFloat(),
//                questsTopY,
//                colorStrokePaints[Color.GREEN]
//            )

//            canvas.drawLine(
//                x.toFloat() + mItemWidth / 8,
//                questsTopY + EVENT_HEIGHT_PX + gap,
//                x.toFloat() + mItemWidth / 8 + mItemWidth / 4,
//                questsTopY + EVENT_HEIGHT_PX + gap,
//                colorStrokePaints[Color.ORANGE]
//            )
//
//            canvas.drawLine(
//                x.toFloat() + mItemWidth / 4,
//                questsTopY + 2 * EVENT_HEIGHT_PX + 2 * gap,
//                x.toFloat() + mItemWidth / 6 + mItemWidth / 5,
//                questsTopY + 2 * EVENT_HEIGHT_PX + 2 * gap,
//                colorStrokePaints[Color.RED]
//            )
//
//            canvas.drawLine(
//                x.toFloat() + mItemWidth / 3,
//                questsTopY + 3 * EVENT_HEIGHT_PX + 3 * gap,
//                x.toFloat() + mItemWidth / 3 + mItemWidth / 4,
//                questsTopY + 3 * EVENT_HEIGHT_PX + 3 * gap,
//                colorStrokePaints[Color.PURPLE]
//            )
//
//            canvas.drawLine(
//                x.toFloat() + mItemWidth / 2,
//                questsTopY + 4 * EVENT_HEIGHT_PX + 4 * gap,
//                x.toFloat() + mItemWidth / 2 + mItemWidth / 3,
//                questsTopY + 4 * EVENT_HEIGHT_PX + 4 * gap,
//                colorStrokePaints[Color.BLUE]
//            )
//
//            canvas.drawLine(
//                x.toFloat() + mItemWidth / 1.5f,
//                questsTopY + 5 * EVENT_HEIGHT_PX + 5 * gap,
//                x.toFloat() + mItemWidth / 1.5f + mItemWidth / 3,
//                questsTopY + 5 * EVENT_HEIGHT_PX + 5 * gap,
//                colorStrokePaints[Color.GREEN]
//            )


//        }

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
        val color: Color,
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
                    WeekViewItem(
                        color = Color.valueOf(o.getString("color")),
                        duration = o.getInt("duration"),
                        startMinute = o.getInt("start")
                    )
                }
            }
        }
    }

}