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
import android.support.v4.util.Pair;
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
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
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
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.squareup.otto.Bus;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.TemporalAdjusters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import io.ipoli.android.app.utils.DateUtils;
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
    public static final int THIS_WEEK = 0;
    public static final int THIS_MONTH = 1;
    public static final int LAST_7_DAYS = 2;
    public static final int LAST_4_WEEKS = 3;
    public static final int LAST_3_MONTHS = 4;
    public static final String X_AXIS_DAY_FORMAT = "d MMM";
    public static final String X_AXIS_MONTH = "MMM";

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
    private List<Chart<?>> rangeCharts;
    private List<Chart<?>> vsCharts;
    private PriorityEstimator priorityEstimator;

    private Map<Category, Boolean> selectedCompleted = new LinkedHashMap<>();
    private Map<Category, Boolean> selectedTimeSpent = new LinkedHashMap<>();

    public class GrowthMarkerView extends MarkerView {

        private TextView popupContent;

        public GrowthMarkerView(Context context) {
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
        App.getAppComponent(getContext()).inject(this);
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_growth, container, false);
        unbinder = ButterKnife.bind(this, view);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        priorityEstimator = new PriorityEstimator();
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

        rangeCharts = new ArrayList<>();
        rangeCharts.add(awesomenessRangeChart);
        rangeCharts.add(completedQuestsRangeChart);
        rangeCharts.add(timeSpentRangeChart);
        rangeCharts.add(coinsEarnedRangeChart);
        rangeCharts.add(xpEarnedRangeChart);

        vsCharts = new ArrayList<>();
        vsCharts.add(awesomenessVsLastChart);
        vsCharts.add(completedQuestsVsLastChart);
        vsCharts.add(timeSpentVsLastChart);
        vsCharts.add(coinsEarnedVsLastChart);
        vsCharts.add(xpEarnedVsLastChart);

        selectedCompleted.put(Category.WELLNESS, true);
        selectedCompleted.put(Category.LEARNING, true);
        selectedCompleted.put(Category.WORK, true);
        selectedCompleted.put(Category.PERSONAL, false);
        selectedCompleted.put(Category.FUN, false);
        selectedCompleted.put(Category.CHORES, false);

        selectedTimeSpent.put(Category.WELLNESS, true);
        selectedTimeSpent.put(Category.LEARNING, true);
        selectedTimeSpent.put(Category.WORK, true);
        selectedTimeSpent.put(Category.PERSONAL, false);
        selectedTimeSpent.put(Category.FUN, false);
        selectedTimeSpent.put(Category.CHORES, false);

        showCharts(THIS_WEEK);
        return view;
    }

    private void showRangeCharts() {
        for (Chart<?> chart : rangeCharts) {
            chart.setVisibility(View.VISIBLE);
        }
        for (Chart<?> chart : vsCharts) {
            chart.setVisibility(View.GONE);
        }
    }

    private void showVsCharts() {
        for (Chart<?> chart : vsCharts) {
            chart.setVisibility(View.VISIBLE);
        }
        for (Chart<?> chart : rangeCharts) {
            chart.setVisibility(View.GONE);
        }
    }

    private void showCharts(int position) {
        LocalDate today = LocalDate.now();
        switch (position) {
            case THIS_WEEK:
                showRangeCharts();
                showThisWeekCharts(today);
                break;
            case THIS_MONTH:
                showRangeCharts();
                showThisMonthCharts(today);
                break;
            case LAST_7_DAYS:
                showVsCharts();
                showLast7DaysCharts(today);
                break;
            case LAST_4_WEEKS:
                showVsCharts();
                showLast4WeeksCharts(today);
                break;
            case LAST_3_MONTHS:
                showVsCharts();
                showLast3MonthsCharts(today);
                break;
        }
    }

    private void showLast3MonthsCharts(LocalDate today) {
        LocalDate startDay = today.minusMonths(2).withDayOfMonth(1);
        List<Pair<Long, Long>> monthRanges = new ArrayList<>();
        final int periodLength = 3;
        for (int i = 0; i < periodLength; i++) {
            LocalDate curStart = startDay.plusMonths(i);
            monthRanges.add(new Pair<>(DateUtils.toMillis(curStart), DateUtils.toMillis(curStart.with(TemporalAdjusters.lastDayOfMonth()))));
        }
        questPersistenceService.findAllBetween(startDay, today, new OnDataChangedListener<List<Quest>>() {
            @Override
            public void onDataChanged(List<Quest> quests) {
                int[] awesomenessData = new int[periodLength];
                int[][] completedData = new int[Category.values().length][periodLength];
                int[][] timeSpentData = new int[Category.values().length][periodLength];
                int[] xpData = new int[periodLength];
                int[] coinsData = new int[periodLength];
                for (Quest q : quests) {
                    if (q.isCompleted()) {
                        Long completedAt = q.getCompletedAt();

                        int idx = -1;
                        for (int i = 0; i < periodLength; i++) {
                            Pair<Long, Long> range = monthRanges.get(i);
                            if (completedAt >= range.first && completedAt <= range.second) {
                                idx = i;
                            }
                        }
                        awesomenessData[idx] += getAwesomenessForQuest(q);
                        coinsData[idx] += q.getCoins();
                        xpData[idx] += q.getExperience();
                        completedData[q.getCategoryType().ordinal()][idx]++;
                        timeSpentData[q.getCategoryType().ordinal()][idx] += q.getActualDuration();
                    }
                }

                String[] xLabels = new String[periodLength];
                for (int i = 0; i < periodLength; i++) {
                    xLabels[i] = startDay.plusMonths(i).format(DateTimeFormatter.ofPattern(X_AXIS_MONTH));
                }

                setupAwesomenessVsLastChart(awesomenessData, xLabels);
                setupCompletedQuestsVsLastChart(completedData, xLabels);
                setupTimeSpentVsLastChart(timeSpentData, xLabels);
                setupCoinsEarnedVsLastChart(coinsData, xLabels);
                setupXpEarnedVsLastChart(xpData, xLabels);
            }
        });
    }

    private void showLast4WeeksCharts(LocalDate today) {
        LocalDate startDay = today.minusWeeks(3).with(DayOfWeek.MONDAY);
        List<Pair<Long, Long>> weekRanges = new ArrayList<>();
        final int periodLength = 4;
        for (int i = 0; i < periodLength; i++) {
            LocalDate curStart = startDay.plusWeeks(i);
            weekRanges.add(new Pair<>(DateUtils.toMillis(curStart), DateUtils.toMillis(curStart.with(DayOfWeek.SUNDAY))));
        }
        questPersistenceService.findAllBetween(startDay, today, new OnDataChangedListener<List<Quest>>() {
            @Override
            public void onDataChanged(List<Quest> quests) {
                int[] awesomenessPerWeek = new int[periodLength];
                int[][] completedPerWeek = new int[Category.values().length][periodLength];
                int[][] timeSpentPerWeek = new int[Category.values().length][periodLength];
                int[] xpPerWeek = new int[periodLength];
                int[] coinsPerWeek = new int[periodLength];
                for (Quest q : quests) {
                    if (q.isCompleted()) {
                        Long completedAt = q.getCompletedAt();

                        int idx = -1;
                        for (int i = 0; i < periodLength; i++) {
                            Pair<Long, Long> range = weekRanges.get(i);
                            if (completedAt >= range.first && completedAt <= range.second) {
                                idx = i;
                            }
                        }
                        awesomenessPerWeek[idx] += getAwesomenessForQuest(q);
                        coinsPerWeek[idx] += q.getCoins();
                        xpPerWeek[idx] += q.getExperience();
                        completedPerWeek[q.getCategoryType().ordinal()][idx]++;
                        timeSpentPerWeek[q.getCategoryType().ordinal()][idx] += q.getActualDuration();
                    }
                }
                String[] xLabels = new String[periodLength];
                for (int i = 0; i < periodLength; i++) {
                    LocalDate weekStart = startDay.plusWeeks(i);
                    LocalDate weekEnd = weekStart.plusDays(6);
                    String weekStartText = weekStart.format(DateTimeFormatter.ofPattern(X_AXIS_DAY_FORMAT));
                    String weekEndText = weekEnd.format(DateTimeFormatter.ofPattern(X_AXIS_DAY_FORMAT));
                    String label = weekStartText + " - " + weekEndText;
                    xLabels[i] = label;
                }

                setupAwesomenessVsLastChart(awesomenessPerWeek, xLabels);
                setupCompletedQuestsVsLastChart(completedPerWeek, xLabels);
                setupTimeSpentVsLastChart(timeSpentPerWeek, xLabels);
                setupCoinsEarnedVsLastChart(coinsPerWeek, xLabels);
                setupXpEarnedVsLastChart(xpPerWeek, xLabels);
            }
        });
    }

    private void showThisMonthCharts(LocalDate today) {
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate startOfPrevMonth = today.minusMonths(1).withDayOfMonth(1);
        int daysInPrevMonth = startOfPrevMonth.lengthOfMonth();
        int daysInCurrentMonth = startOfMonth.lengthOfMonth();

        questPersistenceService.findAllBetween(startOfPrevMonth, today, new OnDataChangedListener<List<Quest>>() {
            @Override
            public void onDataChanged(List<Quest> quests) {
                int[] awesomenessPerDay = new int[daysInPrevMonth + today.getDayOfMonth()];
                int[][] completedPerDay = new int[Category.values().length][daysInCurrentMonth];
                int[][] timeSpentPerDay = new int[Category.values().length][daysInCurrentMonth];
                int[] xpPerDay = new int[daysInPrevMonth + today.getDayOfMonth()];
                int[] coinsPerDay = new int[daysInPrevMonth + today.getDayOfMonth()];
                for (Quest q : quests) {
                    if (q.isCompleted()) {
                        int idx = (int) DAYS.between(startOfPrevMonth, q.getCompletedAtDate());
                        awesomenessPerDay[idx] += getAwesomenessForQuest(q);
                        coinsPerDay[idx] += q.getCoins();
                        xpPerDay[idx] += q.getExperience();
                        int thisMonthIdx = (int) DAYS.between(startOfMonth, q.getCompletedAtDate());
                        completedPerDay[q.getCategoryType().ordinal()][thisMonthIdx]++;
                        timeSpentPerDay[q.getCategoryType().ordinal()][thisMonthIdx] += q.getActualDuration();
                    }
                }
                String[] xLabels = new String[daysInCurrentMonth];
                for (int i = 0; i < daysInCurrentMonth; i++) {
                    xLabels[i] = startOfMonth.plusDays(i).format(DateTimeFormatter.ofPattern("dd MMM"));
                }

                boolean drawHandles = false;
                setupAwesomenessRangeChart(awesomenessPerDay, daysInPrevMonth, "This month", "Last month", xLabels, drawHandles);
                setupCompletedQuestsPerCategoryRangeChart(completedPerDay, xLabels, drawHandles);
                setupTimeSpentRangeChart(timeSpentPerDay, xLabels, drawHandles);
                setupCoinsEarnedRangeChart(coinsPerDay, daysInPrevMonth, "This month", "Last month", xLabels, drawHandles);
                setupXpEarnedRangeChart(xpPerDay, daysInPrevMonth, "This month", "Last month", xLabels, drawHandles);
            }
        });
    }

    private void showLast7DaysCharts(LocalDate today) {

        LocalDate startDay = today.minusDays(6);

        questPersistenceService.findAllBetween(startDay, today, new OnDataChangedListener<List<Quest>>() {
            @Override
            public void onDataChanged(List<Quest> quests) {
                int[] awesomenessPerDay = new int[7];
                int[][] completedPerDay = new int[Category.values().length][7];
                int[][] timeSpentPerDay = new int[Category.values().length][7];
                int[] xpPerDay = new int[7];
                int[] coinsPerDay = new int[7];
                for (Quest q : quests) {
                    if (q.isCompleted()) {
                        int idx = (int) DAYS.between(startDay, q.getCompletedAtDate());
                        awesomenessPerDay[idx] += getAwesomenessForQuest(q);
                        coinsPerDay[idx] += q.getCoins();
                        xpPerDay[idx] += q.getExperience();
                        completedPerDay[q.getCategoryType().ordinal()][idx]++;
                        timeSpentPerDay[q.getCategoryType().ordinal()][idx] += q.getActualDuration();
                    }
                }
                String[] xLabels = new String[7];
                for (int i = 0; i < 7; i++) {
                    xLabels[i] = startDay.plusDays(i).format(DateTimeFormatter.ofPattern(X_AXIS_DAY_FORMAT));
                }

                setupAwesomenessVsLastChart(awesomenessPerDay, xLabels);
                setupCompletedQuestsVsLastChart(completedPerDay, xLabels);
                setupTimeSpentVsLastChart(timeSpentPerDay, xLabels);
                setupCoinsEarnedVsLastChart(coinsPerDay, xLabels);
                setupXpEarnedVsLastChart(xpPerDay, xLabels);
            }
        });
    }

    private void showThisWeekCharts(final LocalDate today) {
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate startOfPrevWeek = today.minusWeeks(1).with(DayOfWeek.MONDAY);

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
                        awesomenessPerDay[idx] += getAwesomenessForQuest(q);
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

                boolean drawHandles = true;
                setupAwesomenessRangeChart(awesomenessPerDay, 7, "This week", "Last week", xLabels, drawHandles);
                setupCompletedQuestsPerCategoryRangeChart(completedPerDay, xLabels, drawHandles);
                setupTimeSpentRangeChart(timeSpentPerDay, xLabels, drawHandles);
                setupCoinsEarnedRangeChart(coinsPerDay, 7, "This week", "Last week", xLabels, drawHandles);
                setupXpEarnedRangeChart(xpPerDay, 7, "This week", "Last week", xLabels, drawHandles);
            }
        });
    }

    private int getAwesomenessForQuest(Quest quest) {
        return Math.max(0, priorityEstimator.estimate(quest));
    }

    private void setupXpEarnedVsLastChart(int[] data, String[] xLabels) {
        applyDefaultStyle(xpEarnedVsLastChart);
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            entries.add(new BarEntry(i, data[i]));
        }
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_green_500));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
        BarData barData = new BarData(dataSet);
        barData.setValueFormatter((v, entry, i, viewPortHandler) -> String.valueOf((int) v));
        xpEarnedVsLastChart.getXAxis().setValueFormatter((v, axisBase) -> {
            int idx = (int) v;
            return xLabels[idx];
        });
        xpEarnedVsLastChart.getXAxis().setLabelCount(xLabels.length);
        xpEarnedVsLastChart.setData(barData);
        xpEarnedVsLastChart.invalidate();
    }

    private void setupCoinsEarnedVsLastChart(int[] data, String[] xLabels) {
        applyDefaultStyle(coinsEarnedVsLastChart);
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            entries.add(new BarEntry(i, data[i]));
        }
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_green_500));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
        BarData barData = new BarData(dataSet);
        barData.setValueFormatter((v, entry, i, viewPortHandler) -> String.valueOf((int) v));
        coinsEarnedVsLastChart.getXAxis().setValueFormatter((v, axisBase) -> {
            int idx = (int) v;
            return xLabels[idx];
        });
        coinsEarnedVsLastChart.getXAxis().setLabelCount(xLabels.length);
        coinsEarnedVsLastChart.setData(barData);
        coinsEarnedVsLastChart.invalidate();
    }

    private void setupTimeSpentVsLastChart(int[][] data, String[] xLabels) {
        applyDefaultStyle(timeSpentVsLastChart);
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < data[0].length; i++) {
            float[] vals = new float[data.length];
            for (int j = 0; j < vals.length; j++) {
                vals[j] = data[j][i];
            }
            entries.add(new BarEntry(i, vals));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(ContextCompat.getColor(getContext(), R.color.md_blue_A200), ContextCompat.getColor(getContext(), R.color.md_green_500), ContextCompat.getColor(getContext(), R.color.md_orange_500));
        dataSet.setDrawValues(false);
        BarData barData = new BarData(dataSet);
        barData.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return String.valueOf((int) v);
            }
        });
        timeSpentVsLastChart.getXAxis().setValueFormatter((v, axisBase) -> {
            int idx = (int) v;
            return xLabels[idx];
        });
        timeSpentVsLastChart.getXAxis().setLabelCount(xLabels.length);
        timeSpentVsLastChart.setData(barData);
        timeSpentVsLastChart.invalidate();
    }

    private void setupCompletedQuestsVsLastChart(int[][] data, String[] xLabels) {
        applyDefaultStyle(completedQuestsVsLastChart);
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < data[0].length; i++) {
            float[] vals = new float[data.length];
            for (int j = 0; j < vals.length; j++) {
                vals[j] = data[j][i];
            }
            entries.add(new BarEntry(i, vals));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(ContextCompat.getColor(getContext(), R.color.md_blue_A200), ContextCompat.getColor(getContext(), R.color.md_green_500), ContextCompat.getColor(getContext(), R.color.md_orange_500));
        dataSet.setDrawValues(false);
        BarData barData = new BarData(dataSet);
        barData.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return String.valueOf((int) v);
            }
        });
        completedQuestsVsLastChart.getXAxis().setValueFormatter((v, axisBase) -> {
            int idx = (int) v;
            return xLabels[idx];
        });
        completedQuestsVsLastChart.getXAxis().setLabelCount(xLabels.length);
        completedQuestsVsLastChart.setData(barData);
        completedQuestsVsLastChart.invalidate();
    }

    private void setupAwesomenessVsLastChart(int[] awesomenessPerDay, String[] xLabels) {
        applyDefaultStyle(awesomenessVsLastChart);
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < awesomenessPerDay.length; i++) {
            entries.add(new BarEntry(i, awesomenessPerDay[i]));
        }
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_green_500));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
        BarData data = new BarData(dataSet);
        data.setValueFormatter((v, entry, i, viewPortHandler) -> String.valueOf((int) v));
        awesomenessVsLastChart.getXAxis().setValueFormatter((v, axisBase) -> {
            int idx = (int) v;
            return xLabels[idx];
        });
        awesomenessVsLastChart.getXAxis().setLabelCount(xLabels.length);
        awesomenessVsLastChart.setData(data);
        awesomenessVsLastChart.invalidate();
        awesomenessVsLastChart.animateY(CHART_ANIMATION_DURATION, DEFAULT_EASING_OPTION);
    }

    private void setupXpEarnedRangeChart(int[] data, int range, String currentRangeLabel, String prevRangeLabel, String[] xLabels, boolean drawHandles) {
        applyDefaultStyle(xpEarnedRangeChart);

        LineData lineData = createThisVsLastWeekLineData(data, range, currentRangeLabel, prevRangeLabel, drawHandles);

        XAxis xAxis = xpEarnedRangeChart.getXAxis();
        xAxis.setValueFormatter((v, axisBase) -> {
            int idx = (int) v;
            return xLabels[idx];
        });

        YAxis yAxis = xpEarnedRangeChart.getAxisLeft();
        yAxis.setAxisMinimum(0);

        GrowthMarkerView growthMarkerView = new GrowthMarkerView(getContext());
        xpEarnedRangeChart.setMarker(growthMarkerView);
        xpEarnedRangeChart.setDescription(null);
        xpEarnedRangeChart.setDrawBorders(false);

        xpEarnedRangeChart.setData(lineData);
        xpEarnedRangeChart.invalidate();
    }

    private void setupCoinsEarnedRangeChart(int[] data, int range, String currentRangeLabel, String prevRangeLabel, String[] xLabels, boolean drawHandles) {
        applyDefaultStyle(coinsEarnedRangeChart);

        LineData lineData = createThisVsLastWeekLineData(data, range, currentRangeLabel, prevRangeLabel, drawHandles);

        XAxis xAxis = coinsEarnedRangeChart.getXAxis();
        xAxis.setValueFormatter((v, axisBase) -> {
            int idx = (int) v;
            return xLabels[idx];
        });

        YAxis yAxis = coinsEarnedRangeChart.getAxisLeft();

        yAxis.setAxisMinimum(0);

        GrowthMarkerView growthMarkerView = new GrowthMarkerView(getContext());
        coinsEarnedRangeChart.setMarker(growthMarkerView);
        coinsEarnedRangeChart.setDescription(null);
        coinsEarnedRangeChart.setDrawBorders(false);

        coinsEarnedRangeChart.setData(lineData);
        coinsEarnedRangeChart.invalidate();
    }

    @NonNull
    private LineData createThisVsLastWeekLineData(int[] data, int prevRangeLength, String currentRangeLabel, String prevRangeLabel, boolean drawHandles) {
        List<Entry> entries = new ArrayList<>();
        for (int i = prevRangeLength; i < data.length; i++) {
            entries.add(new Entry(i - prevRangeLength, data[i], R.color.md_red_A400));
        }
        LineDataSet thisWeekDataSet = new LineDataSet(entries, currentRangeLabel);

        applyLineDataSetStyle(thisWeekDataSet, R.color.md_red_A200, R.color.md_red_A400, drawHandles);

        List<Entry> lastWeekEntries = new ArrayList<>();
        for (int i = 0; i < prevRangeLength; i++) {
            lastWeekEntries.add(new Entry(i, data[i], R.color.md_blue_A400));
        }
        LineDataSet lastWeekDataSet = new LineDataSet(lastWeekEntries, prevRangeLabel);
        applyLineDataSetStyle(lastWeekDataSet, R.color.md_blue_A200, R.color.md_blue_A400, drawHandles);

        return new LineData(lastWeekDataSet, thisWeekDataSet);
    }

    private void setupTimeSpentRangeChart(int[][] data, String[] xLabels, boolean drawHandles) {
        applyDefaultStyle(timeSpentRangeChart);

        LineData lineData = createCategoryLineData(data, drawHandles, selectedTimeSpent);

        XAxis xAxis = timeSpentRangeChart.getXAxis();
        xAxis.setValueFormatter((v, axisBase) -> {
            int idx = (int) v;
            return xLabels[idx];
        });

        GrowthMarkerView growthMarkerView = new GrowthMarkerView(getContext());
        timeSpentRangeChart.setMarker(growthMarkerView);

        timeSpentRangeChart.setDescription(null);
        timeSpentRangeChart.setData(lineData);
        timeSpentRangeChart.invalidate();
    }

    private void setupCompletedQuestsPerCategoryRangeChart(int[][] data, String[] xLabels, boolean drawHandles) {
        applyDefaultStyle(completedQuestsRangeChart);

        LineData lineData = createCategoryLineData(data, drawHandles, selectedCompleted);

        XAxis xAxis = completedQuestsRangeChart.getXAxis();
        xAxis.setValueFormatter((v, axisBase) -> {
            int idx = (int) v;
            return xLabels[idx];
        });

        GrowthMarkerView growthMarkerView = new GrowthMarkerView(getContext());
        completedQuestsRangeChart.setMarker(growthMarkerView);

        completedQuestsRangeChart.setDescription(null);
        completedQuestsRangeChart.setData(lineData);
        completedQuestsRangeChart.invalidate();
    }

    @NonNull
    private LineData createCategoryLineData(int[][] data, boolean drawHandles, Map<Category, Boolean> visibleCategories) {

        LineData lineData = new LineData();

        if (visibleCategories.get(Category.WELLNESS)) {
            LineDataSet wellnessDataSet = createLineDataSetForCategory(data, Category.WELLNESS, drawHandles, R.color.md_green_500, R.color.md_green_700);
            lineData.addDataSet(wellnessDataSet);
        }

        if (visibleCategories.get(Category.LEARNING)) {
            LineDataSet learningDataSet = createLineDataSetForCategory(data, Category.LEARNING, drawHandles, R.color.md_blue_A200, R.color.md_blue_A400);
            lineData.addDataSet(learningDataSet);
        }

        if (visibleCategories.get(Category.WORK)) {
            LineDataSet workDataSet = createLineDataSetForCategory(data, Category.WORK, drawHandles, R.color.md_red_A200, R.color.md_red_A400);
            lineData.addDataSet(workDataSet);
        }

        if (visibleCategories.get(Category.PERSONAL)) {
            LineDataSet personalDataSet = createLineDataSetForCategory(data, Category.PERSONAL, drawHandles, R.color.md_orange_A200, R.color.md_orange_A400);
            lineData.addDataSet(personalDataSet);
        }

        if (visibleCategories.get(Category.FUN)) {
            LineDataSet funDataSet = createLineDataSetForCategory(data, Category.FUN, drawHandles, R.color.md_purple_300, R.color.md_purple_500);
            lineData.addDataSet(funDataSet);
        }

        if (visibleCategories.get(Category.CHORES)) {
            LineDataSet choresDataSet = createLineDataSetForCategory(data, Category.CHORES, drawHandles, R.color.md_brown_300, R.color.md_brown_500);
            lineData.addDataSet(choresDataSet);
        }

        return lineData;
    }

    @NonNull
    private LineDataSet createLineDataSetForCategory(int[][] data, Category category, boolean drawHandles, @ColorRes int color, @ColorRes int highlightColor) {
        int[] categoryData = data[category.ordinal()];
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < categoryData.length; i++) {
            entries.add(new Entry(i, categoryData[i], highlightColor));
        }
        LineDataSet dataSet = new LineDataSet(entries, getString(Category.getNameRes(category)));

        applyLineDataSetStyle(dataSet, color, highlightColor, drawHandles);
        return dataSet;
    }

    private void setupAwesomenessRangeChart(int[] data, int prevRangeLength, String currentRangeLabel, String prevRangeLabel, String[] xLabels, boolean drawHandles) {
        applyDefaultStyle(awesomenessRangeChart);

        LineData lineData = createThisVsLastWeekLineData(data, prevRangeLength, currentRangeLabel, prevRangeLabel, drawHandles);

        XAxis xAxis = awesomenessRangeChart.getXAxis();
        xAxis.setValueFormatter((v, axisBase) -> {
            int idx = (int) v;
            return xLabels[idx];
        });

        YAxis yAxis = awesomenessRangeChart.getAxisLeft();

        yAxis.setAxisMinimum(0);

        GrowthMarkerView growthMarkerView = new GrowthMarkerView(getContext());
        awesomenessRangeChart.setMarker(growthMarkerView);
        awesomenessRangeChart.setDescription(null);
        awesomenessRangeChart.setDrawBorders(false);

        awesomenessRangeChart.setData(lineData);
        awesomenessRangeChart.invalidate();
        awesomenessRangeChart.animateX(CHART_ANIMATION_DURATION, DEFAULT_EASING_OPTION);
    }

    private void applyLineDataSetStyle(LineDataSet dataSet, @ColorRes int color, @ColorRes int highlightColor, boolean drawHandles) {
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        if (drawHandles) {
            dataSet.setDrawCircleHole(true);
        } else {
            dataSet.setDrawCircleHole(false);
            dataSet.setDrawCircles(false);
        }
        dataSet.setCircleHoleRadius(3f);
        dataSet.setCircleColorHole(Color.WHITE);
        dataSet.setCircleColor(ContextCompat.getColor(getContext(), color));
        dataSet.setCircleRadius(6f);
        dataSet.setLineWidth(2f);
        dataSet.setHighLightColor(ContextCompat.getColor(getContext(), highlightColor));
        dataSet.setDrawValues(false);
        dataSet.setColor(ContextCompat.getColor(getContext(), color));
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