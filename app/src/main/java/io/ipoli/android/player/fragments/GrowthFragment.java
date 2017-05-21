package io.ipoli.android.player.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.help.HelpDialog;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/4/16.
 */
public class GrowthFragment extends BaseFragment {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.awesomeness_chart)
    LineChart awesomenessChart;

    @BindView(R.id.awesomeness_vs_week)
    TextView awesomenessVsWeek;

    @BindView(R.id.awesomeness_vs_month)
    TextView awesomenessVsMonth;

    @Inject
    Bus eventBus;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_growth, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);
        ((MainActivity) getActivity()).initToolbar(toolbar, R.string.growth);
        setupAwesomenessChart();

        SpannableString s = new SpannableString("+18%\nvs\nlast week");
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.md_green_500)), 0, 4, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        s.setSpan(new StyleSpan(Typeface.BOLD), 0, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.md_dark_text_54)), 5, s.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//        s.setSpan(new RelativeSizeSpan(0.8f), 5, s.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        awesomenessVsWeek.setText(s);
        return view;
    }

    private void setupAwesomenessChart() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 12));
        entries.add(new Entry(2, 24));
        entries.add(new Entry(3, 38));
        entries.add(new Entry(4, 55));
        entries.add(new Entry(5, 74));
        entries.add(new Entry(6, 80));
        entries.add(new Entry(7, 85));
        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setCircleColor(Color.TRANSPARENT);
//        dataSet.setFillAlpha(255);
        dataSet.setDrawFilled(true);
        dataSet.setLineWidth(3);
        dataSet.setDrawValues(false);
        awesomenessChart.setExtraBottomOffset(8);
//        dataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_green_500));
        dataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.md_green_500));
//        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
//        dataSet.setValueTextSize(10);

        LineData lineData = new LineData(dataSet);
        awesomenessChart.setDescription(null);
        awesomenessChart.getLegend().setEnabled(false);
        awesomenessChart.setDrawBorders(false);
        XAxis xAxis = awesomenessChart.getXAxis();
        xAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        xAxis.setTextSize(12);
        xAxis.setYOffset(12);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return "12 Feb";
            }
        });
        xAxis.setLabelRotationAngle(330);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        awesomenessChart.getAxisLeft().setGridColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        awesomenessChart.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return String.valueOf((int) v) + "%";
            }
        });
        awesomenessChart.getAxisLeft().setXOffset(12);
        awesomenessChart.getAxisRight().setEnabled(false);
//        awesomenessChart.setDrawGridBackground(true);
//        awesomenessChart.setGridBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_amber_700));
//        Paint paint = new Paint();
//        paint.setColor(ContextCompat.getColor(getContext(), R.color.md_amber_700));
//        awesomenessChart.setPaint(paint, PAINT_GRID_BACKGROUND);
//        awesomenessChart.
        awesomenessChart.setData(lineData);
        awesomenessChart.invalidate();

        xAxis.setLabelCount(entries.size(), true);
        awesomenessChart.getAxisLeft().setAxisMinimum(0);
        awesomenessChart.getAxisLeft().setAxisMaximum(100);
        awesomenessChart.getAxisLeft().setLabelCount(6, true);
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    protected void showHelpDialog() {
        HelpDialog.newInstance(R.layout.fragment_help_dialog_growth, R.string.help_dialog_growth_title, "growth").show(getActivity().getSupportFragmentManager());
    }
}