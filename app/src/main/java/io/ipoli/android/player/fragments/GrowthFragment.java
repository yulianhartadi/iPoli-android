package io.ipoli.android.player.fragments;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

    @BindView(R.id.root_container)
    ViewGroup rootContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_collapsing_container)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.awesomeness_chart)
    CombinedChart awesomenessChart;

    @BindView(R.id.completed_quests_chart)
    LineChart completedQuestsChart;

    @Inject
    Bus eventBus;

    private Unbinder unbinder;

    public class CustomMarkerView extends MarkerView {

        private TextView popupContent;

        public CustomMarkerView(Context context) {
            super(context, R.layout.chart_popup);
            popupContent = (TextView) findViewById(R.id.popup_content);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            @ColorRes int backgroundColor = (int) e.getData();
            GradientDrawable drawable = (GradientDrawable) popupContent.getBackground();
            drawable.setColor(ContextCompat.getColor(getContext(), backgroundColor));
            popupContent.setText(String.valueOf((int) e.getY())); // set the entry-value as the display text
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2f), -(getHeight() * 1.5f));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_growth, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);
        ((MainActivity) getActivity()).initToolbar(toolbar, R.string.growth);
        collapsingToolbarLayout.setTitleEnabled(false);
        setupAwesomenessChart();
        setupCompletedQuestsChart();
        return view;
    }

    private void setupCompletedQuestsChart() {
        applyDefaultStyle(completedQuestsChart);
//        List<Entry> totalEntries = new ArrayList<>();
//        totalEntries.add(new Entry(1, 12, R.color.colorAccentDark));
//        totalEntries.add(new Entry(2, 24, R.color.colorAccentDark));
//        totalEntries.add(new Entry(3, 19, R.color.colorAccentDark));
//        totalEntries.add(new Entry(4, 23, R.color.colorAccentDark));
//        totalEntries.add(new Entry(5, 12, R.color.colorAccentDark));
//        totalEntries.add(new Entry(6, 4, R.color.colorAccentDark));
//        totalEntries.add(new Entry(7, 5, R.color.colorAccentDark));
//        LineDataSet totalDataSet = new LineDataSet(totalEntries, "Total");
//        totalDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//        totalDataSet.setCircleRadius(4);
//        totalDataSet.setDrawCircleHole(false);
//        totalDataSet.setDrawValues(false);
//        totalDataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
//        totalDataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.colorAccentDark));
//        totalDataSet.setHighLightColor(ContextCompat.getColor(getContext(), R.color.colorAccentDark));

        List<Entry> wellnessEntries = new ArrayList<>();
        wellnessEntries.add(new Entry(1, 4, R.color.md_green_700));
        wellnessEntries.add(new Entry(2, 9, R.color.md_green_700));
        wellnessEntries.add(new Entry(3, 12, R.color.md_green_700));
        wellnessEntries.add(new Entry(4, 4, R.color.md_green_700));
        wellnessEntries.add(new Entry(5, 8, R.color.md_green_700));
        wellnessEntries.add(new Entry(6, 1, R.color.md_green_700));
        wellnessEntries.add(new Entry(7, 3, R.color.md_green_700));
        LineDataSet wellnessDataSet = new LineDataSet(wellnessEntries, "Wellness");
        wellnessDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//        wellnessDataSet.setCircleRadius(4);
        wellnessDataSet.setDrawCircleHole(false);
        wellnessDataSet.setDrawValues(false);
        wellnessDataSet.setLineWidth(1.5f);
        wellnessDataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_green_500));
        wellnessDataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.md_green_700));
        wellnessDataSet.setHighLightColor(ContextCompat.getColor(getContext(), R.color.md_green_700));
        wellnessDataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.md_green_300));
        wellnessDataSet.setDrawFilled(true);

        List<Entry> learningEntries = new ArrayList<>();
        learningEntries.add(new Entry(1, 2, R.color.md_blue_700));
        learningEntries.add(new Entry(2, 4, R.color.md_blue_700));
        learningEntries.add(new Entry(3, 5, R.color.md_blue_700));
        learningEntries.add(new Entry(4, 10, R.color.md_blue_700));
        learningEntries.add(new Entry(5, 3, R.color.md_blue_700));
        learningEntries.add(new Entry(6, 1, R.color.md_blue_700));
        learningEntries.add(new Entry(7, 1, R.color.md_blue_700));
        LineDataSet learningDataSet = new LineDataSet(learningEntries, "Learning");
        learningDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//        learningDataSet.setCircleRadius(4);
        learningDataSet.setDrawCircleHole(false);
//        learningDataSet.setDrawCircles(false);
        learningDataSet.setLineWidth(1.5f);
        learningDataSet.setDrawValues(false);
        learningDataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_blue_500));
        learningDataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.md_blue_700));
        learningDataSet.setHighLightColor(ContextCompat.getColor(getContext(), R.color.md_blue_700));
        learningDataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.md_blue_300));
        learningDataSet.setDrawFilled(true);

        List<Entry> workEntries = new ArrayList<>();
        workEntries.add(new Entry(1, 1, R.color.md_red_700));
        workEntries.add(new Entry(2, 4, R.color.md_red_700));
        workEntries.add(new Entry(3, 7, R.color.md_red_700));
        workEntries.add(new Entry(4, 12, R.color.md_red_700));
        workEntries.add(new Entry(5, 4, R.color.md_red_700));
        workEntries.add(new Entry(6, 7, R.color.md_red_700));
        workEntries.add(new Entry(7, 9, R.color.md_red_700));
        LineDataSet workDataSet = new LineDataSet(workEntries, "Work");
        workDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//        workDataSet.setCircleRadius(4);
        workDataSet.setDrawCircleHole(false);
//        workDataSet.setDrawCircles(false);
        workDataSet.setLineWidth(1.5f);
        workDataSet.setDrawValues(false);
        workDataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_red_500));
        workDataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.md_red_700));
        workDataSet.setHighLightColor(ContextCompat.getColor(getContext(), R.color.md_red_700));
        workDataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.md_red_300));
        workDataSet.setDrawFilled(true);

        LineData lineData = new LineData(wellnessDataSet, learningDataSet, workDataSet);

        XAxis xAxis = completedQuestsChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return "12 Feb";
            }
        });

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext());
        completedQuestsChart.setMarker(customMarkerView);

        completedQuestsChart.setDescription(null);
        completedQuestsChart.setData(lineData);
        completedQuestsChart.invalidate();
    }

    private void setupAwesomenessChart() {
        applyDefaultStyle(awesomenessChart);

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 20, R.color.md_green_700));
        entries.add(new Entry(2, 32, R.color.md_green_700));
        entries.add(new Entry(3, 42, R.color.md_green_700));
        entries.add(new Entry(4, 55, R.color.md_green_700));
        entries.add(new Entry(5, 74, R.color.md_green_700));
        entries.add(new Entry(6, 80, R.color.md_green_700));
        entries.add(new Entry(7, 85, R.color.md_green_700));
        LineDataSet lineDataSet = new LineDataSet(entries, "");
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.md_green_700));
//        lineDataSet.setDrawFilled(true);
        lineDataSet.setLineWidth(1.5f);
        lineDataSet.setHighLightColor(ContextCompat.getColor(getContext(), R.color.md_green_700));
        lineDataSet.setDrawValues(false);

        lineDataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_green_500));
//        lineDataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.md_green_300));


        awesomenessChart.setDescription(null);
        awesomenessChart.getLegend().setEnabled(false);
        awesomenessChart.setDrawBorders(false);
        XAxis xAxis = awesomenessChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return "12 Feb";
            }
        });

        YAxis yAxis = awesomenessChart.getAxisLeft();
        yAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return String.valueOf((int) v) + "%";
            }
        });


        xAxis.setLabelCount(entries.size(), true);
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(100);
        yAxis.setLabelCount(6, true);

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext());
        awesomenessChart.setMarker(customMarkerView);

        List<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1, 15, R.color.md_blue_500));
        barEntries.add(new BarEntry(2, 12, R.color.md_blue_500));
        barEntries.add(new BarEntry(3, 21, R.color.md_blue_500));
        barEntries.add(new BarEntry(4, 40, R.color.md_blue_500));
        barEntries.add(new BarEntry(5, 64, R.color.md_blue_500));
        barEntries.add(new BarEntry(6, 18, R.color.md_blue_500));
        barEntries.add(new BarEntry(7, 21, R.color.md_blue_500));
        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setDrawValues(false);
        barDataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_blue_500));
        BarData perDayThisWeek = new BarData(barDataSet);
        perDayThisWeek.setBarWidth(perDayThisWeek.getBarWidth() / 1.5f);

        List<Entry> lastWeekEntries = new ArrayList<>();
        lastWeekEntries.add(new Entry(1, 12, R.color.md_orange_700));
        lastWeekEntries.add(new Entry(2, 21, R.color.md_orange_700));
        lastWeekEntries.add(new Entry(3, 38, R.color.md_orange_700));
        lastWeekEntries.add(new Entry(4, 42, R.color.md_orange_700));
        lastWeekEntries.add(new Entry(5, 64, R.color.md_orange_700));
        lastWeekEntries.add(new Entry(6, 72, R.color.md_orange_700));
        lastWeekEntries.add(new Entry(7, 89, R.color.md_orange_700));
        LineDataSet lastWeekDataSet = new LineDataSet(lastWeekEntries, "");
        lastWeekDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lastWeekDataSet.setDrawCircleHole(false);
        lastWeekDataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.md_orange_500));
        lastWeekDataSet.setLineWidth(1.5f);
        lastWeekDataSet.setHighLightColor(ContextCompat.getColor(getContext(), R.color.md_orange_700));
        lastWeekDataSet.setDrawValues(false);

        lastWeekDataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_orange_500));

        LineData lineData = new LineData(lineDataSet, lastWeekDataSet);

        CombinedData data = new CombinedData();
        data.setData(lineData);
        data.setData(perDayThisWeek);

        awesomenessChart.setData(data);
        awesomenessChart.invalidate();
    }

    private void applyDefaultStyle(CombinedChart chart) {
        chart.setDrawBorders(false);
        chart.setExtraBottomOffset(8);
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        xAxis.setTextSize(12);
        xAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        xAxis.setYOffset(12);
        xAxis.setLabelRotationAngle(330);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setTextSize(12);
        yAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        yAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        yAxis.setXOffset(12);
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisMinimum(0);
        chart.getAxisRight().setEnabled(false);
    }

    private void applyDefaultStyle(LineChart chart) {
        chart.setDrawBorders(false);
        chart.setExtraBottomOffset(8);
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        xAxis.setTextSize(12);
        xAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        xAxis.setYOffset(12);
        xAxis.setLabelRotationAngle(330);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setTextSize(12);
        yAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        yAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        yAxis.setXOffset(12);
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisMinimum(0);
        chart.getAxisRight().setEnabled(false);
    }

    @OnClick(R.id.filter_completed_quests)
    public void onFilterCompletedQuestsClick(View view) {
        LayoutInflater inflater = (LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.category_filter, (ViewGroup) getActivity().findViewById(R.id.filters_container));
        PopupWindow pw = new PopupWindow(layout, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        // display the popup in the center
        pw.showAsDropDown(view);
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