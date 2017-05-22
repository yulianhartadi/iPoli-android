package io.ipoli.android.player.fragments;

import android.content.Context;
import android.graphics.Color;
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

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.ViewPortHandler;
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

    public static final int CHART_ANIMATION_DURATION = 500;
    public static final Easing.EasingOption DEFAULT_EASING_OPTION = Easing.EasingOption.EaseInQuart;

    @BindView(R.id.root_container)
    ViewGroup rootContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_collapsing_container)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.awesomeness_range_chart)
    LineChart awesomenessRangeChart;

    @BindView(R.id.awesomeness_vs_last_chart)
    BarChart awesomenessVsLastChart;

    @BindView(R.id.completed_quests_chart)
    LineChart completedQuestsChart;

    @BindView(R.id.time_spent_chart)
    LineChart timeSpentChart;

    @BindView(R.id.coins_earned_chart)
    LineChart coinsEarnedChart;

    @BindView(R.id.xp_earned_chart)
    LineChart xpEarnedChart;

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
            popupContent.setText(String.valueOf((int) e.getY()));
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
        awesomenessRangeChart.setVisibility(View.GONE);
        awesomenessVsLastChart.setVisibility(View.VISIBLE);
        setupAwesomenessRangeChart();
        setupAwesomenessVsLastChart();
        setupCompletedQuestsChart();
        setupTimeSpentChart();
        setupCoinsEarnedChart();
        setupXpEarnedChart();
        return view;
    }

    private void setupAwesomenessVsLastChart() {
        applyDefaultStyle(awesomenessVsLastChart);
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, 40));
        entries.add(new BarEntry(2, 51));
        entries.add(new BarEntry(3, 23));
        entries.add(new BarEntry(4, 44));
        entries.add(new BarEntry(5, 12));
        entries.add(new BarEntry(6, 87));
        entries.add(new BarEntry(7, 65));
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_red_A200));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
        BarData data = new BarData(dataSet);
        data.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return String.valueOf((int) v);
            }
        });
        awesomenessVsLastChart.setData(data);
        awesomenessVsLastChart.invalidate();
        awesomenessVsLastChart.animateY(CHART_ANIMATION_DURATION, DEFAULT_EASING_OPTION);
    }

    private void setupXpEarnedChart() {
        applyDefaultStyle(xpEarnedChart);

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 42, R.color.md_red_A400));
        entries.add(new Entry(2, 32, R.color.md_red_A400));
        entries.add(new Entry(3, 20, R.color.md_red_A400));
        entries.add(new Entry(4, 55, R.color.md_red_A400));
        entries.add(new Entry(5, 67, R.color.md_red_A400));
        LineDataSet thisWeekDataSet = new LineDataSet(entries, "This week");

        applyLineDataSetStyle(thisWeekDataSet, R.color.md_red_A200, R.color.md_red_A400);

        List<Entry> lastWeekEntries = new ArrayList<>();
        lastWeekEntries.add(new Entry(1, 12, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(2, 21, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(3, 38, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(4, 93, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(5, 64, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(6, 22, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(7, 12, R.color.md_blue_A400));
        LineDataSet lastWeekDataSet = new LineDataSet(lastWeekEntries, "Last week");
        applyLineDataSetStyle(lastWeekDataSet, R.color.md_blue_A200, R.color.md_blue_A400);

        XAxis xAxis = xpEarnedChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return "12 Feb";
            }
        });

        YAxis yAxis = xpEarnedChart.getAxisLeft();
        yAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return String.valueOf((int) v) + "%";
            }
        });


        xAxis.setLabelCount(lastWeekEntries.size(), true);
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(100);
        yAxis.setLabelCount(6, true);

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext());
        xpEarnedChart.setMarker(customMarkerView);
        xpEarnedChart.setDescription(null);
        xpEarnedChart.setDrawBorders(false);

        LineData lineData = new LineData(lastWeekDataSet, thisWeekDataSet);

        xpEarnedChart.setData(lineData);
        xpEarnedChart.invalidate();
    }

    private void setupCoinsEarnedChart() {
        applyDefaultStyle(coinsEarnedChart);

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 42, R.color.md_red_A400));
        entries.add(new Entry(2, 32, R.color.md_red_A400));
        entries.add(new Entry(3, 20, R.color.md_red_A400));
        entries.add(new Entry(4, 55, R.color.md_red_A400));
        entries.add(new Entry(5, 67, R.color.md_red_A400));
        LineDataSet thisWeekDataSet = new LineDataSet(entries, "This week");

        applyLineDataSetStyle(thisWeekDataSet, R.color.md_red_A200, R.color.md_red_A400);


        List<Entry> lastWeekEntries = new ArrayList<>();
        lastWeekEntries.add(new Entry(1, 12, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(2, 21, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(3, 38, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(4, 93, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(5, 64, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(6, 22, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(7, 12, R.color.md_blue_A400));
        LineDataSet lastWeekDataSet = new LineDataSet(lastWeekEntries, "Last week");
        applyLineDataSetStyle(lastWeekDataSet, R.color.md_blue_A200, R.color.md_blue_A400);

        XAxis xAxis = coinsEarnedChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return "12 Feb";
            }
        });

        YAxis yAxis = coinsEarnedChart.getAxisLeft();
        yAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return String.valueOf((int) v) + "%";
            }
        });


        xAxis.setLabelCount(lastWeekEntries.size(), true);
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(100);
        yAxis.setLabelCount(6, true);

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext());
        coinsEarnedChart.setMarker(customMarkerView);
        coinsEarnedChart.setDescription(null);
        coinsEarnedChart.setDrawBorders(false);

        LineData lineData = new LineData(lastWeekDataSet, thisWeekDataSet);

        coinsEarnedChart.setData(lineData);
        coinsEarnedChart.invalidate();
    }

    private void setupTimeSpentChart() {
        applyDefaultStyle(timeSpentChart);

        List<Entry> wellnessEntries = new ArrayList<>();
        wellnessEntries.add(new Entry(1, 4, R.color.md_green_700));
        wellnessEntries.add(new Entry(2, 9, R.color.md_green_700));
        wellnessEntries.add(new Entry(3, 12, R.color.md_green_700));
        wellnessEntries.add(new Entry(4, 4, R.color.md_green_700));
        wellnessEntries.add(new Entry(5, 8, R.color.md_green_700));
        wellnessEntries.add(new Entry(6, 1, R.color.md_green_700));
        wellnessEntries.add(new Entry(7, 3, R.color.md_green_700));
        LineDataSet wellnessDataSet = new LineDataSet(wellnessEntries, "Wellness");

        applyLineDataSetStyle(wellnessDataSet, R.color.md_green_500, R.color.md_green_700);

        List<Entry> learningEntries = new ArrayList<>();
        learningEntries.add(new Entry(1, 2, R.color.md_blue_A400));
        learningEntries.add(new Entry(2, 4, R.color.md_blue_A400));
        learningEntries.add(new Entry(3, 5, R.color.md_blue_A400));
        learningEntries.add(new Entry(4, 10, R.color.md_blue_A400));
        learningEntries.add(new Entry(5, 3, R.color.md_blue_A400));
        learningEntries.add(new Entry(6, 1, R.color.md_blue_A400));
        learningEntries.add(new Entry(7, 1, R.color.md_blue_A400));
        LineDataSet learningDataSet = new LineDataSet(learningEntries, "Learning");

        applyLineDataSetStyle(learningDataSet, R.color.md_blue_A200, R.color.md_blue_A400);

        List<Entry> workEntries = new ArrayList<>();
        workEntries.add(new Entry(1, 1, R.color.md_red_A400));
        workEntries.add(new Entry(2, 4, R.color.md_red_A400));
        workEntries.add(new Entry(3, 7, R.color.md_red_A400));
        workEntries.add(new Entry(4, 12, R.color.md_red_A400));
        workEntries.add(new Entry(5, 4, R.color.md_red_A400));
        workEntries.add(new Entry(6, 7, R.color.md_red_A400));
        workEntries.add(new Entry(7, 9, R.color.md_red_A400));
        LineDataSet workDataSet = new LineDataSet(workEntries, "Work");

        applyLineDataSetStyle(workDataSet, R.color.md_red_A200, R.color.md_red_A400);

        LineData lineData = new LineData(wellnessDataSet, learningDataSet, workDataSet);

        XAxis xAxis = timeSpentChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return "12 Feb";
            }
        });

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext());
        timeSpentChart.setMarker(customMarkerView);

        timeSpentChart.setDescription(null);
        timeSpentChart.setData(lineData);
        timeSpentChart.invalidate();
    }

    private void setupCompletedQuestsChart() {
        applyDefaultStyle(completedQuestsChart);

        List<Entry> wellnessEntries = new ArrayList<>();
        wellnessEntries.add(new Entry(1, 4, R.color.md_green_700));
        wellnessEntries.add(new Entry(2, 9, R.color.md_green_700));
        wellnessEntries.add(new Entry(3, 12, R.color.md_green_700));
        wellnessEntries.add(new Entry(4, 4, R.color.md_green_700));
        wellnessEntries.add(new Entry(5, 8, R.color.md_green_700));
        wellnessEntries.add(new Entry(6, 1, R.color.md_green_700));
        wellnessEntries.add(new Entry(7, 3, R.color.md_green_700));
        LineDataSet wellnessDataSet = new LineDataSet(wellnessEntries, "Wellness");

        applyLineDataSetStyle(wellnessDataSet, R.color.md_green_500, R.color.md_green_700);

        List<Entry> learningEntries = new ArrayList<>();
        learningEntries.add(new Entry(1, 2, R.color.md_blue_A400));
        learningEntries.add(new Entry(2, 4, R.color.md_blue_A400));
        learningEntries.add(new Entry(3, 5, R.color.md_blue_A400));
        learningEntries.add(new Entry(4, 10, R.color.md_blue_A400));
        learningEntries.add(new Entry(5, 3, R.color.md_blue_A400));
        learningEntries.add(new Entry(6, 1, R.color.md_blue_A400));
        learningEntries.add(new Entry(7, 1, R.color.md_blue_A400));
        LineDataSet learningDataSet = new LineDataSet(learningEntries, "Learning");

        applyLineDataSetStyle(learningDataSet, R.color.md_blue_A200, R.color.md_blue_A400);

        List<Entry> workEntries = new ArrayList<>();
        workEntries.add(new Entry(1, 1, R.color.md_red_A400));
        workEntries.add(new Entry(2, 4, R.color.md_red_A400));
        workEntries.add(new Entry(3, 7, R.color.md_red_A400));
        workEntries.add(new Entry(4, 12, R.color.md_red_A400));
        workEntries.add(new Entry(5, 4, R.color.md_red_A400));
        workEntries.add(new Entry(6, 7, R.color.md_red_A400));
        workEntries.add(new Entry(7, 9, R.color.md_red_A400));
        LineDataSet workDataSet = new LineDataSet(workEntries, "Work");

        applyLineDataSetStyle(workDataSet, R.color.md_red_A200, R.color.md_red_A400);

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

    private void setupAwesomenessRangeChart() {
        applyDefaultStyle(awesomenessRangeChart);

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 42, R.color.md_red_A400));
        entries.add(new Entry(2, 32, R.color.md_red_A400));
        entries.add(new Entry(3, 20, R.color.md_red_A400));
        entries.add(new Entry(4, 55, R.color.md_red_A400));
        entries.add(new Entry(5, 67, R.color.md_red_A400));
        LineDataSet thisWeekDataSet = new LineDataSet(entries, "This week");

        applyLineDataSetStyle(thisWeekDataSet, R.color.md_red_A200, R.color.md_red_A400);

        List<Entry> lastWeekEntries = new ArrayList<>();
        lastWeekEntries.add(new Entry(1, 12, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(2, 21, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(3, 38, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(4, 93, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(5, 64, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(6, 22, R.color.md_blue_A400));
        lastWeekEntries.add(new Entry(7, 12, R.color.md_blue_A400));
        LineDataSet lastWeekDataSet = new LineDataSet(lastWeekEntries, "Last week");
        applyLineDataSetStyle(lastWeekDataSet, R.color.md_blue_A200, R.color.md_blue_A400);

        XAxis xAxis = awesomenessRangeChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return "12 Feb";
            }
        });

        YAxis yAxis = awesomenessRangeChart.getAxisLeft();
        yAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return String.valueOf((int) v) + "%";
            }
        });

        xAxis.setLabelCount(lastWeekEntries.size(), true);
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(100);
        yAxis.setLabelCount(6, true);

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext());
        awesomenessRangeChart.setMarker(customMarkerView);
        awesomenessRangeChart.setDescription(null);
        awesomenessRangeChart.setDrawBorders(false);

        LineData lineData = new LineData(lastWeekDataSet, thisWeekDataSet);

        awesomenessRangeChart.setData(lineData);
        awesomenessRangeChart.invalidate();
//        awesomenessRangeChart.animateX(500, Easing.EasingOption.EaseInOutQuart);
        awesomenessRangeChart.animateX(CHART_ANIMATION_DURATION, DEFAULT_EASING_OPTION);
    }

    private void applyLineDataSetStyle(LineDataSet lastWeekDataSet, @ColorRes int color, @ColorRes int highlightColor) {
        lastWeekDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lastWeekDataSet.setDrawCircleHole(true);
        lastWeekDataSet.setCircleHoleRadius(3f);
        lastWeekDataSet.setCircleColorHole(Color.WHITE);
        lastWeekDataSet.setCircleColor(ContextCompat.getColor(getContext(), color));
        lastWeekDataSet.setCircleRadius(6f);
        lastWeekDataSet.setLineWidth(2f);
        lastWeekDataSet.setHighLightColor(ContextCompat.getColor(getContext(), highlightColor));
        lastWeekDataSet.setDrawValues(false);
        lastWeekDataSet.setColor(ContextCompat.getColor(getContext(), color));
    }

    private void applyDefaultStyle(LineChart chart) {
        chart.setDescription(null);
        chart.setDrawBorders(false);
        chart.setExtraBottomOffset(8);
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawAxisLine(false);
//        xAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        xAxis.setYOffset(12f);
        xAxis.setLabelRotationAngle(330);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setTextSize(12f);
        yAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        yAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        yAxis.setXOffset(12f);
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisMinimum(0);

        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setYOffset(8f);
        legend.setTextSize(12f);
    }

    private void applyDefaultStyle(BarChart chart) {
        chart.setDescription(null);
        chart.setDrawBorders(false);
        chart.setExtraBottomOffset(8);
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawAxisLine(false);
//        xAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        xAxis.setYOffset(12f);
        xAxis.setLabelRotationAngle(330);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setTextSize(12f);
        yAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        yAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        yAxis.setXOffset(12f);
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisMinimum(0);

        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);
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