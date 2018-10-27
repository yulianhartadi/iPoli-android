package io.ipoli.android.quest.schedule.agenda.view.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.MonthView
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.view.attrData
import org.json.JSONArray
import org.json.JSONObject


@Suppress("unused")
class AgendaMonthView(context: Context) : MonthView(context) {

    private val selectedBackgroundPaint = Paint()
    private val currentBackgroundPaint = Paint()

    private val currentDayTextPaint: Paint

    private val itemPaint = Paint()

    private var radius = 0f
    private var scheduleItemRadius = 0f
    private var itemPadding = 0f
    private var itemSpacing = 0f

    init {

        selectedBackgroundPaint.isAntiAlias = true
        selectedBackgroundPaint.color = context.attrData(R.attr.colorAccent)

        currentBackgroundPaint.isAntiAlias = true
        currentBackgroundPaint.color = context.attrData(R.attr.colorPrimary)

        currentDayTextPaint = createTextPaint(context, R.color.md_light_text_50, 14)
        currentDayTextPaint.color = context.attrData(R.attr.colorAccent)
        currentDayTextPaint.style = Paint.Style.FILL

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

    override fun onPreviewHook() {
        radius = (Math.min(mItemWidth, mItemHeight) / 11 * 5.2).toFloat()
        scheduleItemRadius = ViewUtils.dpToPx(2f, context)
        itemPadding = ViewUtils.dpToPx(6f, context)
        itemSpacing = ViewUtils.dpToPx(4f, context)
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
        return true
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
        val data = JSONObject(calendar.scheme)
        val monthIndicators = data.getJSONArray("monthIndicators")

        val items = AgendaMonthView.ScheduleItem.createItemsFromJson(monthIndicators)

        val cx = (x + mItemWidth / 2).toFloat()

        val itemsWidth = scheduleItemRadius * items.size * 2 + (itemSpacing * (items.size - 1))
        val itemsStart = cx - (itemsWidth / 2)

        if (calendar.isCurrentDay) {
            val cy = (y + mItemHeight / 2).toFloat()
            canvas.drawCircle(
                cx,
                cy,
                radius,
                currentBackgroundPaint
            )
        }

        items.forEachIndexed { i, item ->
            itemPaint.color = item.color
            val itemStart = itemsStart + (i * scheduleItemRadius * 2) + (itemSpacing * i)
            canvas.drawCircle(
                itemStart + scheduleItemRadius,
                y + mTextBaseLine + itemPadding,
                scheduleItemRadius,
                itemPaint
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

        val cx = (x + mItemWidth / 2).toFloat()

        if (calendar.isCurrentDay && !hasScheme) {
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

    data class ScheduleItem(@ColorInt val color: Int) {
        companion object {

            fun createItemsFromJson(data: JSONArray): List<ScheduleItem> {
                if (data.length() == 0) {
                    return emptyList()
                }
                return (0 until data.length()).map {
                    ScheduleItem(data.getInt(it))
                }
            }

        }
    }

}