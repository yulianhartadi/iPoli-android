package io.ipoli.android.quest.schedule.summary.view.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.text.TextUtils
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.MonthView
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.common.view.attrData
import org.json.JSONArray

class SelectionRectangle
    (
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

data class ScheduleItem(val name: String, val isCompleted: Boolean, @ColorInt val color: Int) {
    companion object {

        fun createItemsFromJson(data: JSONArray, context: Context): List<ScheduleItem> {
            if (data.length() == 0) {
                return emptyList()
            }
            return (0 until data.length()).map {
                val o = data.getJSONObject(it)
                val t = o.getString("type")
                when (t) {
                    "quest" -> {
                        val isCompleted = o.getBoolean("isCompleted")
                        val bc = if (isCompleted) {
                            R.color.md_grey_500
                        } else {
                            val c = AndroidColor.valueOf(o.getString("color"))
                            c.color500
                        }

                        val androidColor = ContextCompat.getColor(context, bc)
                        ScheduleItem(
                            name = o.getString("name"),
                            isCompleted = isCompleted,
                            color = androidColor
                        )
                    }
                    "event" -> {
                        val c = o.getInt("color")
                        ScheduleItem(
                            name = o.getString("name"),
                            isCompleted = false,
                            color = c
                        )
                    }
                    else -> throw IllegalStateException("Unknown schedule summary item type $t")
                }

            }
        }
    }
}

@Suppress("unused")
class ScheduleSummaryMonthView(context: Context) : MonthView(context) {

    private val selectedBorderPaint = Paint()
    private val currentDayBorderPaint = Paint()

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

        currentDayBorderPaint.style = Paint.Style.STROKE
        currentDayBorderPaint.strokeWidth = ViewUtils.dpToPx(1.5f, context)
        currentDayBorderPaint.color = context.attrData(R.attr.colorPrimary)

        dividerPaint.style = Paint.Style.STROKE
        dividerPaint.strokeWidth = ViewUtils.dpToPx(1f, context)
        dividerPaint.color = context.attrData(android.R.attr.listDivider)

        itemPaint.isAntiAlias = true
        itemPaint.style = Paint.Style.FILL
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

        val data = JSONArray(calendar.scheme)

        val items = ScheduleItem.createItemsFromJson(data, context)

        val cellStart = x + ViewUtils.dpToPx(1f, context)
        val cellEnd = cellStart + mItemWidth - ViewUtils.dpToPx(2f, context)

        canvas.drawLine(
            x.toFloat(),
            y.toFloat(),
            (x + mItemWidth).toFloat(),
            y.toFloat(),
            dividerPaint
        )

        val topOffset = y.toFloat() + mItemHeight / 4f

        val qHeight = mItemHeight / 4.5f

        items.forEachIndexed { index, scheduleItem ->

            if (index > 3) {
                return@forEachIndexed
            }

            drawQuestBackground(
                index,
                scheduleItem.color,
                cellStart,
                cellEnd,
                topOffset,
                qHeight,
                canvas,
                y.toFloat()
            )

            if (index < 3) {

                drawQuestName(
                    index,
                    scheduleItem,
                    cellStart,
                    cellEnd,
                    topOffset,
                    qHeight,
                    canvas
                )
            }
        }
    }

    private fun drawQuestBackground(
        index: Int,
        @ColorInt color: Int,
        cellStart: Float,
        cellEnd: Float,
        topOffset: Float,
        questHeight: Float,
        canvas: Canvas,
        y: Float,
        padding: Float = 1f
    ) {
        itemPaint.color = color
        val top = topOffset + questHeight * index + index * padding
        val bottom = topOffset + questHeight * (index + 1) + index * padding
        canvas.drawRect(
            cellStart,
            top,
            cellEnd,
            Math.min(bottom, y + mItemHeight - padding),
            itemPaint
        )
    }

    private fun drawQuestName(
        index: Int,
        scheduleItem: ScheduleItem,
        cellStart: Float,
        cellEnd: Float,
        topOffset: Float,
        questHeight: Float,
        canvas: Canvas,
        padding: Float = 1f
    ) {

        val textStart = cellStart + ViewUtils.dpToPx(2f, context)
        val textEnd = cellEnd - ViewUtils.dpToPx(2f, context)

        val drawnText =
            TextUtils.ellipsize(scheduleItem.name, whiteTextPaint, textEnd - textStart, TextUtils.TruncateAt.END)

        val b = Rect()
        whiteTextPaint.getTextBounds(drawnText.toString(), 0, drawnText.length, b)

        whiteTextPaint.isStrikeThruText = scheduleItem.isCompleted

        canvas.drawText(
            drawnText.toString(),
            textStart,
            topOffset + questHeight / 2 + b.height() / 2.5f + (questHeight * index) + (padding * index),
            whiteTextPaint
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
        val baselineY = mTextBaseLine + y - (mItemHeight / 2.65f)
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