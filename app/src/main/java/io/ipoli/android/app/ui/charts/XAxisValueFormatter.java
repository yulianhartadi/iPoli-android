package io.ipoli.android.app.ui.charts;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/25/17.
 */
public class XAxisValueFormatter implements IAxisValueFormatter {

    private final String[] labels;

    public XAxisValueFormatter(String[] labels) {
        this.labels = labels;
    }

    @Override
    public String getFormattedValue(float v, AxisBase axisBase) {
        int idx = (int) v;
        if (idx < 0 || idx >= labels.length) {
            return "";
        }
        return labels[idx];
    }
}
