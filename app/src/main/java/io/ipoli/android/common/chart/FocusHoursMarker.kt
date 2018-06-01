package io.ipoli.android.common.chart

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import io.ipoli.android.R
import io.ipoli.android.common.datetime.Duration
import io.ipoli.android.common.datetime.Minute
import io.ipoli.android.common.text.DurationFormatter
import io.ipoli.android.common.view.invisible
import io.ipoli.android.common.view.visible
import java.math.RoundingMode
import java.text.DecimalFormat

class FocusHoursMarker(context: Context) :
    MarkerView(context, R.layout.view_chart_marker) {

    private val chartMarker: TextView = findViewById(R.id.chartMarker)

    @Suppress("UNCHECKED_CAST")
    override fun refreshContent(e: Entry, highlight: Highlight) {
        if (e.data == null) {
            chartMarker.invisible()
        } else {
            chartMarker.visible()
            chartMarker.text =
                DurationFormatter.formatNarrow((e.data as Duration<Minute>).intValue)
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -(height * 1.5f))

    }
}

class AwesomenessScoreMarker(context: Context) :
    MarkerView(context, R.layout.view_chart_marker) {

    private val chartMarker: TextView = findViewById(R.id.chartMarker)

    private val df = DecimalFormat("#.##").apply {
        roundingMode = RoundingMode.UP
    }

    override fun refreshContent(e: Entry, highlight: Highlight) {
        if (e.data == null) {
            chartMarker.invisible()
        } else {
            chartMarker.text = df.format(e.y)
            chartMarker.visible()
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -(height * 1.5f))

    }
}