package io.ipoli.android.player.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
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

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

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
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.app.scheduling.PriorityEstimator;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

import static org.threeten.bp.temporal.ChronoUnit.DAYS;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/4/16.
 */
public class GrowthFragment extends BaseFragment implements AdapterView.OnItemSelectedListener {

    public static final int CHART_ANIMATION_DURATION = 500;
    public static final Easing.EasingOption DEFAULT_EASING_OPTION = Easing.EasingOption.EaseInQuart;

    @BindView(R.id.root_container)
    ViewGroup rootContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_spinner)
    Spinner spinner;

    @BindView(R.id.toolbar_collapsing_container)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.awesomeness_range_chart)
    LineChart awesomenessRangeChart;

    @BindView(R.id.awesomeness_vs_last_chart)
    BarChart awesomenessVsLastChart;

    @BindView(R.id.completed_quests_range_chart)
    LineChart completedQuestsRangeChart;

    @BindView(R.id.completed_quests_vs_last_chart)
    BarChart completedQuestsVsLastChart;

    @BindView(R.id.time_spent_range_chart)
    LineChart timeSpentRangeChart;

    @BindView(R.id.time_spent_vs_last_chart)
    BarChart timeSpentVsLastChart;

    @BindView(R.id.coins_earned_range_chart)
    LineChart coinsEarnedRangeChart;

    @BindView(R.id.coins_earned_vs_last_chart)
    BarChart coinsEarnedVsLastChart;

    @BindView(R.id.xp_earned_range_chart)
    LineChart xpEarnedRangeChart;

    @BindView(R.id.xp_earned_vs_last_chart)
    BarChart xpEarnedVsLastChart;

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

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
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);


            ArrayAdapter<String> adapter = new ArrayAdapter<>(actionBar.getThemedContext(),
                    R.layout.growth_spinner_item,
                    R.id.growth_interval,
                    getResources().getStringArray(R.array.growth_intervals));
            adapter.setDropDownViewResource(R.layout.growth_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.md_white), PorterDuff.Mode.SRC_ATOP);
            spinner.setSelection(0, false);
            spinner.setOnItemSelectedListener(this);
            ((MainActivity) getActivity()).actionBarDrawerToggle.syncState();
        }
        showCharts(0);
        return view;
    }

    private void showCharts(int position) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate startOfPrevWeek = today.minusWeeks(1).with(DayOfWeek.MONDAY);

        PriorityEstimator estimator = new PriorityEstimator();

        questPersistenceService.findAllBetween(startOfPrevWeek, today, new OnDataChangedListener<List<Quest>>() {
            @Override
            public void onDataChanged(List<Quest> quests) {
                int[] awesomenessPerDay = new int[7 + today.getDayOfWeek().getValue()];
                int[][] completedPerDay = new int[Category.values().length][7];
                int[][] timeSpentPerDay = new int[Category.values().length][7];
                int[] xpPerDay = new int[7 + today.getDayOfWeek().getValue()];
                int[] coinsPerDay = new int[7 + today.getDayOfWeek().getValue()];
                for (Quest q : quests) {
                    if (q.isCompleted()) {
                        int idx = (int) DAYS.between(startOfPrevWeek, q.getCompletedAtDate());
                        awesomenessPerDay[idx] += estimator.estimate(q);
                        coinsPerDay[idx] += q.getCoins();
                        xpPerDay[idx] += q.getExperience();
                        int thisWeekIdx = (int) DAYS.between(startOfWeek, q.getCompletedAtDate());
                        completedPerDay[q.getCategoryType().ordinal()][thisWeekIdx]++;
                        timeSpentPerDay[q.getCategoryType().ordinal()][thisWeekIdx] += q.getActualDuration();
                    }
                }
                String[] xLabels = new String[7];
                for (int i = 0; i < 7; i++) {
                    xLabels[i] = startOfWeek.plusDays(i).format(DateTimeFormatter.ofPattern("dd MMM"));
                }

                setupAwesomenessRangeChart(awesomenessPerDay, 7, "This week", "Last week", xLabels);
//        setupAwesomenessVsLastChart();
                setupCompletedQuestsPerCategoryRangeChart(completedPerDay, xLabels);
//        setupCompletedQuestsVsLastChart();
                setupTimeSpentRangeChart(timeSpentPerDay, xLabels);
//        setupTimeSpentVsLastChart();
                setupCoinsEarnedRangeChart(coinsPerDay, 7, "This week", "Last week", xLabels);
//        setupCoinsEarnedVsLastChart();
                setupXpEarnedRangeChart(xpPerDay, 7, "This week", "Last week", xLabels);
//        setupXpEarnedVsLastChart();
            }
        });
    }

    private void setupXpEarnedVsLastChart() {
        applyDefaultStyle(xpEarnedVsLastChart);
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, 40));
        entries.add(new BarEntry(2, 51));
        entries.add(new BarEntry(3, 23));
        entries.add(new BarEntry(4, 44));
        entries.add(new BarEntry(5, 12));
        entries.add(new BarEntry(6, 87));
        entries.add(new BarEntry(7, 65));
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_green_500));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
        BarData data = new BarData(dataSet);
        data.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return String.valueOf((int) v);
            }
        });
        xpEarnedVsLastChart.setData(data);
        xpEarnedVsLastChart.invalidate();
    }

    private void setupCoinsEarnedVsLastChart() {
        applyDefaultStyle(coinsEarnedVsLastChart);
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, 40));
        entries.add(new BarEntry(2, 51));
        entries.add(new BarEntry(3, 23));
        entries.add(new BarEntry(4, 44));
        entries.add(new BarEntry(5, 12));
        entries.add(new BarEntry(6, 87));
        entries.add(new BarEntry(7, 65));
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_green_500));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
        BarData data = new BarData(dataSet);
        data.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return String.valueOf((int) v);
            }
        });
        coinsEarnedVsLastChart.setData(data);
        coinsEarnedVsLastChart.invalidate();
    }

    private void setupTimeSpentVsLastChart() {
        applyDefaultStyle(timeSpentVsLastChart);
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, new float[]{5, 44, 55}));
        entries.add(new BarEntry(2, new float[]{40, 32, 55}));
        entries.add(new BarEntry(3, new float[]{12, 44, 55}));
        entries.add(new BarEntry(4, new float[]{40, 12, 75}));
        entries.add(new BarEntry(5, new float[]{33, 44, 55}));
        entries.add(new BarEntry(6, new float[]{47, 44, 12}));
        entries.add(new BarEntry(7, new float[]{9, 81, 55}));

//        entries.add(new BarEntry(1, new float[]{5, 44, 55, 3, 33, 12}));
//        entries.add(new BarEntry(2, new float[]{40, 32, 55, 8, 10, 12}));
//        entries.add(new BarEntry(3, new float[]{12, 44, 55, 15, 33, 12}));
//        entries.add(new BarEntry(4, new float[]{40, 12, 75, 32, 33, 12}));
//        entries.add(new BarEntry(5, new float[]{33, 44, 55, 60, 33, 12}));
//        entries.add(new BarEntry(6, new float[]{47, 44, 12, 44, 21, 12}));
//        entries.add(new BarEntry(7, new float[]{9, 81, 55, 60, 33, 12}));

        BarDataSet dataSet = new BarDataSet(entries, "");
//        dataSet.setColors(ContextCompat.getColor(getContext(), R.color.md_blue_A200), ContextCompat.getColor(getContext(), R.color.md_green_500), ContextCompat.getColor(getContext(), R.color.md_orange_500), ContextCompat.getColor(getContext(), R.color.md_red_A200),
//                ContextCompat.getColor(getContext(), R.color.md_purple_300), ContextCompat.getColor(getContext(), R.color.md_brown_500));
        dataSet.setColors(ContextCompat.getColor(getContext(), R.color.md_blue_A200), ContextCompat.getColor(getContext(), R.color.md_green_500), ContextCompat.getColor(getContext(), R.color.md_orange_500));
        dataSet.setDrawValues(false);
//        dataSet.setValueTextSize(12f);
//        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
        BarData data = new BarData(dataSet);
        data.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return String.valueOf((int) v);
            }
        });
        timeSpentVsLastChart.setData(data);
        timeSpentVsLastChart.invalidate();
    }

    private void setupCompletedQuestsVsLastChart() {
        applyDefaultStyle(completedQuestsVsLastChart);
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, new float[]{5, 44, 55}));
        entries.add(new BarEntry(2, new float[]{40, 32, 55}));
        entries.add(new BarEntry(3, new float[]{12, 44, 55}));
        entries.add(new BarEntry(4, new float[]{40, 12, 75}));
        entries.add(new BarEntry(5, new float[]{33, 44, 55}));
        entries.add(new BarEntry(6, new float[]{47, 44, 12}));
        entries.add(new BarEntry(7, new float[]{9, 81, 55}));

//        entries.add(new BarEntry(1, new float[]{5, 44, 55, 3, 33, 12}));
//        entries.add(new BarEntry(2, new float[]{40, 32, 55, 8, 10, 12}));
//        entries.add(new BarEntry(3, new float[]{12, 44, 55, 15, 33, 12}));
//        entries.add(new BarEntry(4, new float[]{40, 12, 75, 32, 33, 12}));
//        entries.add(new BarEntry(5, new float[]{33, 44, 55, 60, 33, 12}));
//        entries.add(new BarEntry(6, new float[]{47, 44, 12, 44, 21, 12}));
//        entries.add(new BarEntry(7, new float[]{9, 81, 55, 60, 33, 12}));

        BarDataSet dataSet = new BarDataSet(entries, "");
//        dataSet.setColors(ContextCompat.getColor(getContext(), R.color.md_blue_A200), ContextCompat.getColor(getContext(), R.color.md_green_500), ContextCompat.getColor(getContext(), R.color.md_orange_500), ContextCompat.getColor(getContext(), R.color.md_red_A200),
//                ContextCompat.getColor(getContext(), R.color.md_purple_300), ContextCompat.getColor(getContext(), R.color.md_brown_500));
        dataSet.setColors(ContextCompat.getColor(getContext(), R.color.md_blue_A200), ContextCompat.getColor(getContext(), R.color.md_green_500), ContextCompat.getColor(getContext(), R.color.md_orange_500));
        dataSet.setDrawValues(false);
//        dataSet.setValueTextSize(12f);
//        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
        BarData data = new BarData(dataSet);
        data.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return String.valueOf((int) v);
            }
        });
        completedQuestsVsLastChart.setData(data);
        completedQuestsVsLastChart.invalidate();
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
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_green_500));
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

    private void setupXpEarnedRangeChart(int[] data, int range, String currentRangeLabel, String prevRangeLabel, String[] xLabels) {
        applyDefaultStyle(xpEarnedRangeChart);

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

        XAxis xAxis = xpEarnedRangeChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return "12 Feb";
            }
        });

        YAxis yAxis = xpEarnedRangeChart.getAxisLeft();
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
        xpEarnedRangeChart.setMarker(customMarkerView);
        xpEarnedRangeChart.setDescription(null);
        xpEarnedRangeChart.setDrawBorders(false);

        LineData lineData = new LineData(lastWeekDataSet, thisWeekDataSet);

        xpEarnedRangeChart.setData(lineData);
        xpEarnedRangeChart.invalidate();
    }

    private void setupCoinsEarnedRangeChart(int[] data, int range, String currentRangeLabel, String prevRangeLabel, String[] xLabels) {
        applyDefaultStyle(coinsEarnedRangeChart);

        LineData lineData = createThisVsLastWeekLineData(data, range, currentRangeLabel, prevRangeLabel);

        XAxis xAxis = coinsEarnedRangeChart.getXAxis();
        xAxis.setValueFormatter((v, axisBase) -> {
            int idx = (int) v;
            return xLabels[idx];
        });

        YAxis yAxis = coinsEarnedRangeChart.getAxisLeft();

        xAxis.setLabelCount(range, true);
        yAxis.setAxisMinimum(0);

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext());
        coinsEarnedRangeChart.setMarker(customMarkerView);
        coinsEarnedRangeChart.setDescription(null);
        coinsEarnedRangeChart.setDrawBorders(false);

        coinsEarnedRangeChart.setData(lineData);
        coinsEarnedRangeChart.invalidate();

//        awesomenessRangeChart.setData(lineData);
//        awesomenessRangeChart.invalidate();
//        awesomenessRangeChart.animateX(CHART_ANIMATION_DURATION, DEFAULT_EASING_OPTION);
    }

    @NonNull
    private LineData createThisVsLastWeekLineData(int[] data, int range, String currentRangeLabel, String prevRangeLabel) {
        List<Entry> entries = new ArrayList<>();
        for (int i = range; i < data.length; i++) {
            entries.add(new Entry(i - range, data[i], R.color.md_red_A400));
        }
        LineDataSet thisWeekDataSet = new LineDataSet(entries, currentRangeLabel);

        applyLineDataSetStyle(thisWeekDataSet, R.color.md_red_A200, R.color.md_red_A400);

        List<Entry> lastWeekEntries = new ArrayList<>();
        for (int i = 0; i < range; i++) {
            lastWeekEntries.add(new Entry(i, data[i], R.color.md_blue_A400));
        }
        LineDataSet lastWeekDataSet = new LineDataSet(lastWeekEntries, prevRangeLabel);
        applyLineDataSetStyle(lastWeekDataSet, R.color.md_blue_A200, R.color.md_blue_A400);

        return new LineData(lastWeekDataSet, thisWeekDataSet);
    }

    private void setupTimeSpentRangeChart(int[][] data, String[] xLabels) {
        applyDefaultStyle(timeSpentRangeChart);

        LineData lineData = createCategoryLineData(data);

        XAxis xAxis = timeSpentRangeChart.getXAxis();
        xAxis.setValueFormatter((v, axisBase) -> {
            int idx = (int) v;
            return xLabels[idx];
        });

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext());
        timeSpentRangeChart.setMarker(customMarkerView);

        timeSpentRangeChart.setDescription(null);
        timeSpentRangeChart.setData(lineData);
        timeSpentRangeChart.invalidate();
    }

    private void setupCompletedQuestsPerCategoryRangeChart(int[][] data, String[] xLabels) {
        applyDefaultStyle(completedQuestsRangeChart);

        LineData lineData = createCategoryLineData(data);

        XAxis xAxis = completedQuestsRangeChart.getXAxis();
        xAxis.setValueFormatter((v, axisBase) -> {
            int idx = (int) v;
            return xLabels[idx];
        });

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext());
        completedQuestsRangeChart.setMarker(customMarkerView);

        completedQuestsRangeChart.setDescription(null);
        completedQuestsRangeChart.setData(lineData);
        completedQuestsRangeChart.invalidate();
    }

    @NonNull
    private LineData createCategoryLineData(int[][] data) {
        int[] wellnessData = data[Category.WELLNESS.ordinal()];
        List<Entry> wellnessEntries = new ArrayList<>();
        for (int i = 0; i < wellnessData.length; i++) {
            wellnessEntries.add(new Entry(i, wellnessData[i], R.color.md_green_700));
        }
        LineDataSet wellnessDataSet = new LineDataSet(wellnessEntries, "Wellness");

        applyLineDataSetStyle(wellnessDataSet, R.color.md_green_500, R.color.md_green_700);

        int[] learningData = data[Category.LEARNING.ordinal()];
        List<Entry> learningEntries = new ArrayList<>();
        for (int i = 0; i < learningData.length; i++) {
            learningEntries.add(new Entry(i, learningData[i], R.color.md_blue_A400));
        }
        LineDataSet learningDataSet = new LineDataSet(learningEntries, "Learning");

        applyLineDataSetStyle(learningDataSet, R.color.md_blue_A200, R.color.md_blue_A400);

        int[] workData = data[Category.WORK.ordinal()];
        List<Entry> workEntries = new ArrayList<>();
        for (int i = 0; i < workData.length; i++) {
            workEntries.add(new Entry(i, workData[i], R.color.md_red_A400));
        }
        LineDataSet workDataSet = new LineDataSet(workEntries, "Work");

        applyLineDataSetStyle(workDataSet, R.color.md_red_A200, R.color.md_red_A400);

        int[] personalData = data[Category.PERSONAL.ordinal()];
        List<Entry> personalEntries = new ArrayList<>();
        for (int i = 0; i < personalData.length; i++) {
            personalEntries.add(new Entry(i, personalData[i], R.color.md_orange_A400));
        }
        LineDataSet personalDataSet = new LineDataSet(personalEntries, "Personal");

        applyLineDataSetStyle(personalDataSet, R.color.md_orange_A200, R.color.md_orange_A400);

        int[] funData = data[Category.FUN.ordinal()];
        List<Entry> funEntries = new ArrayList<>();
        for (int i = 0; i < funData.length; i++) {
            funEntries.add(new Entry(i, funData[i], R.color.md_purple_500));
        }
        LineDataSet funDataSet = new LineDataSet(funEntries, "Fun");

        applyLineDataSetStyle(funDataSet, R.color.md_purple_300, R.color.md_purple_500);

        int[] choresData = data[Category.CHORES.ordinal()];
        List<Entry> choresEntries = new ArrayList<>();
        for (int i = 0; i < choresData.length; i++) {
            choresEntries.add(new Entry(i, choresData[i], R.color.md_brown_500));
        }
        LineDataSet choresDataSet = new LineDataSet(choresEntries, "Chores");

        applyLineDataSetStyle(choresDataSet, R.color.md_brown_300, R.color.md_brown_500);

        return new LineData(wellnessDataSet, learningDataSet, workDataSet, personalDataSet, funDataSet, choresDataSet);
    }

    private void setupAwesomenessRangeChart(int[] data, int range, String currentRangeLabel, String prevRangeLabel, String[] xLabels) {
        applyDefaultStyle(awesomenessRangeChart);

        LineData lineData = createThisVsLastWeekLineData(data, range, currentRangeLabel, prevRangeLabel);

        XAxis xAxis = awesomenessRangeChart.getXAxis();
        xAxis.setValueFormatter((v, axisBase) -> {
            int idx = (int) v;
            return xLabels[idx];
        });

        YAxis yAxis = awesomenessRangeChart.getAxisLeft();

        xAxis.setLabelCount(range, true);
        yAxis.setAxisMinimum(0);

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext());
        awesomenessRangeChart.setMarker(customMarkerView);
        awesomenessRangeChart.setDescription(null);
        awesomenessRangeChart.setDrawBorders(false);

        awesomenessRangeChart.setData(lineData);
        awesomenessRangeChart.invalidate();
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
        yAxis.setValueFormatter((v, axisBase) -> String.valueOf((int) v));

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


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        showCharts(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}