package io.ipoli.android.app.ui.charts;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/25/17.
 */
public class ChartMarkerView extends MarkerView {

    private TextView popupContent;

    public ChartMarkerView(Context context) {
        super(context, R.layout.chart_popup);
        popupContent = (TextView) findViewById(R.id.popup_content);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        popupContent.setText(String.valueOf((int) e.getY()));
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -(getHeight() * 1.5f));
    }
}
