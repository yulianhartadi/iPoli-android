package io.ipoli.android.common.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.AttrRes
import android.support.v4.content.ContextCompat
import android.support.v4.widget.TextViewCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.ipoli.android.R


class WheelItemPickerView : ScrollView {

    private var itemOffset = 0
    private lateinit var itemContainer: LinearLayout
    private val items = mutableListOf<String>()
    private val visibleItemCount = DEFAULT_SHOW_ITEM_COUNT

    private var selectedIndex = 2
    private var lastYPosition = 0

    private lateinit var autoScrollTask: Runnable

    private var measuredItemHeight = 0

    companion object {
        private const val SCROLL_DELAY_MILLIS = 50
        private const val DEFAULT_SHOW_ITEM_COUNT = 5
        private const val ITEM_HEIGHT = 60
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        updateItemViews(getItemIndex(t))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) {
            startAutoScroll()
        }
        return super.onTouchEvent(ev)
    }

    private fun init(context: Context) {
        this.isVerticalScrollBarEnabled = false
        itemContainer = LinearLayout(context)
        itemContainer.orientation = LinearLayout.VERTICAL
        this.addView(itemContainer)

        createScrollTask()
    }

    private fun createScrollTask() {
        autoScrollTask = Runnable {
            val nowY = scrollY
            if (lastYPosition - nowY == 0) { // stopped
                val remainder = lastYPosition % measuredItemHeight
                selectedIndex = getItemIndex(lastYPosition)
                if (remainder != 0) {
                    this@WheelItemPickerView.post {
                        if (remainder > measuredItemHeight / 2) {
                            this@WheelItemPickerView.smoothScrollTo(
                                0,
                                lastYPosition - remainder + measuredItemHeight
                            )
                        } else {
                            this@WheelItemPickerView.smoothScrollTo(0, lastYPosition - remainder)
                        }
                    }
                }
            } else {
                startAutoScroll()
            }
        }
    }

    fun setItems(list: List<String>) {
        setNewItems(list)
        createItemViews()
        updateItemViews(selectedIndex)
    }

    private fun setNewItems(list: List<String>) {
        items.clear()

        items.addAll(list)

        itemOffset = (visibleItemCount - 1) / 2
        // fill head and end
        for (i in 0 until itemOffset) {
            items.add(0, "")
            items.add("")
        }
    }

    private fun createItemViews() {
        itemContainer.removeAllViews()
        for (item in items) {
            itemContainer.addView(createView(item))
        }
    }

    private fun startAutoScroll() {
        lastYPosition = scrollY
        this.postDelayed(autoScrollTask, SCROLL_DELAY_MILLIS.toLong())
    }

    private fun createView(item: String): TextView {
        val numView = TextView(context)
        numView.height = dip2px(ITEM_HEIGHT.toFloat()).toInt()
        numView.text = item
        TextViewCompat.setTextAppearance(numView, android.R.style.TextAppearance_Material_Title)
        numView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        numView.gravity = Gravity.CENTER
        if (measuredItemHeight == 0) {
            measuredItemHeight = getViewMeasuredHeight(numView)
            itemContainer.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                measuredItemHeight * visibleItemCount
            )
            this.layoutParams =
                RelativeLayout.LayoutParams(
                    layoutParams.width,
                    measuredItemHeight * visibleItemCount
                )
        }
        return numView
    }


    private fun updateItemViews(selectedPosition: Int) {
        for (i in 0 until itemContainer.childCount) {
            val itemView = itemContainer.getChildAt(i) as TextView
            if (selectedPosition == i) {
                itemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                itemView.setTextColor(attrData(R.attr.colorAccent))
            } else {
                itemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                itemView.setTextColor(ContextCompat.getColor(context, R.color.md_dark_text_38))
            }
        }
    }

    private fun getItemIndex(verticalScrollY: Int): Int {
        val remainder = verticalScrollY % measuredItemHeight
        val divided = verticalScrollY / measuredItemHeight
        return if (remainder != 0 && remainder > measuredItemHeight / 2) {
            divided + itemOffset + 1
        } else divided + itemOffset
    }

    fun setSelectedItem(position: Int) {
        selectedIndex = position + itemOffset
        this.post { this@WheelItemPickerView.scrollTo(0, position * measuredItemHeight) }
    }

    val selectedItemIndex
        get() = selectedIndex - itemOffset


    private fun dip2px(dpValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dpValue * scale + 0.5f
    }

    fun attrData(@AttrRes attributeRes: Int) =
        TypedValue().let {
            context.theme.resolveAttribute(attributeRes, it, true)
            it.data
        }

    private fun getViewMeasuredHeight(view: View): Int {
        val width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val expandSpec =
            View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE shr 2, View.MeasureSpec.AT_MOST)
        view.measure(width, expandSpec)
        return view.measuredHeight
    }
}